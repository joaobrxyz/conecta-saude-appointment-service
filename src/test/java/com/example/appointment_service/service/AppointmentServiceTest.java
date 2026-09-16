package com.example.appointment_service.service;

import com.example.appointment_service.client.IdentityClient;
import com.example.appointment_service.dto.AppointmentRequestDTO;
import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.repository.AppointmentRepository;
import com.example.appointment_service.service.validacoes.ValidadorAgendamentoDeConsulta;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @InjectMocks
    AppointmentService appointmentService;

    @Mock
    AppointmentRepository repository;

    @Mock
    IdentityClient identityClient;

    // Criamos um validador "falso" para representar a lista inteira de regras
    @Mock
    ValidadorAgendamentoDeConsulta validadorMock;

    @BeforeEach
    void setUp() {
        // Injeta a lista de validadores manualmente dentro do AppointmentService
        ReflectionTestUtils.setField(appointmentService, "validadores", List.of(validadorMock));
    }

    // --- MÉTODOS AUXILIARES ---
    private AppointmentRequestDTO criarRequestDTO() {
        return new AppointmentRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now().plusDays(2)
        );
    }

    @DisplayName("Quando agendar uma consulta")
    @Nested
    class AgendarConsulta {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um request válido, com médico e paciente existentes e aprovação dos validadores")
            @Test
            void teste1() {
                // Dado
                var dto = criarRequestDTO();

                // Simula que o repositório retorna a própria entidade que recebeu para salvar
                // (Isso evita NullPointerException na hora de devolver o DTO final)
                when(repository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // Quando
                AppointmentRequestDTO atual = appointmentService.agendarConsulta(dto);

                // Então
                assertThat(atual).isNotNull();

                // Garante que fez as consultas no Feign
                verify(identityClient, times(1)).getDoctorById(dto.doctorId());
                verify(identityClient, times(1)).getPatientById(dto.patientId());

                // Garante que rodou as validações de regra de negócio
                verify(validadorMock, times(1)).validar(dto);

                // Garante que salvou no banco de dados
                verify(repository, times(1)).save(any(Appointment.class));
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado que o médico não é encontrado no Identity Service (Erro 404 do Feign)")
            @Test
            void teste1() {
                // Dado
                var dto = criarRequestDTO();
                // Ensina o mock a lançar a exceção do Feign quando buscarem esse médico
                when(identityClient.getDoctorById(dto.doctorId())).thenThrow(mock(FeignException.NotFound.class));

                // Quando / Então
                assertThatThrownBy(() -> appointmentService.agendarConsulta(dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Médico ou paciente não encontrado.");

                // Verifica que as etapas seguintes NÃO foram executadas (Short-circuit)
                verify(identityClient, never()).getPatientById(any());
                verify(validadorMock, never()).validar(any());
                verify(repository, never()).save(any());
            }

            @DisplayName("Dado que o paciente não é encontrado no Identity Service (Erro 404 do Feign)")
            @Test
            void teste2() {
                // Dado
                var dto = criarRequestDTO();
                // Médico passa (não fazemos nada), mas paciente falha
                when(identityClient.getPatientById(dto.patientId())).thenThrow(mock(FeignException.NotFound.class));

                // Quando / Então
                assertThatThrownBy(() -> appointmentService.agendarConsulta(dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Médico ou paciente não encontrado.");

                // Verifica que não tentou validar nem salvar
                verify(validadorMock, never()).validar(any());
                verify(repository, never()).save(any());
            }

            @DisplayName("Dado que um dos validadores reprova o agendamento (Ex: Choque de horário)")
            @Test
            void teste3() {
                // Dado
                var dto = criarRequestDTO();

                // Simula o validador lançando uma exceção durante o loop
                doThrow(new IllegalArgumentException("O médico já possui uma consulta agendada nesse horário."))
                        .when(validadorMock).validar(dto);

                // Quando / Então
                assertThatThrownBy(() -> appointmentService.agendarConsulta(dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("O médico já possui uma consulta agendada nesse horário.");

                // Garante que a operação foi cancelada antes de bater no banco de dados
                verify(repository, never()).save(any());
            }
        }
    }
}