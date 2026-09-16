package com.example.appointment_service.service.validacoes;

import com.example.appointment_service.dto.AppointmentRequestDTO;
import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.model.StatusConsulta;
import com.example.appointment_service.repository.AppointmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidadorChoqueHorarioTest {

    @InjectMocks
    ValidadorChoqueHorario validador;

    @Mock
    AppointmentRepository repository;

    // --- MÉTODOS AUXILIARES ---
    private AppointmentRequestDTO criarRequestDTO() {
        return new AppointmentRequestDTO(
                UUID.fromString("c11f8434-1111-4a3e-aa51-bff7ce7dd111"), // doctorId
                UUID.fromString("b34f8434-5dfb-4a3e-aa51-bff7ce7dd884"), // patientId
                LocalDateTime.of(2026, 10, 20, 10, 0)
        );
    }

    @DisplayName("Quando validar choque de horário")
    @Nested
    class Validar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado que nem médico nem paciente têm consultas no horário")
            @Test
            void teste1() {
                // Dado
                var dto = criarRequestDTO();
                when(repository.existsByDoctorIdAndDataHoraConsultaAndStatus(
                        dto.doctorId(), dto.dataHoraConsulta(), StatusConsulta.AGENDADA)).thenReturn(false);
                when(repository.existsByPatientIdAndDataHoraConsultaAndStatus(
                        dto.patientId(), dto.dataHoraConsulta(), StatusConsulta.AGENDADA)).thenReturn(false);

                // Quando / Então
                // Usamos assertThatCode do AssertJ para garantir que nenhuma exceção seja lançada
                assertThatCode(() -> validador.validar(dto)).doesNotThrowAnyException();
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado que o médico já possui uma consulta no horário")
            @Test
            void teste1() {
                // Dado
                var dto = criarRequestDTO();
                when(repository.existsByDoctorIdAndDataHoraConsultaAndStatus(
                        dto.doctorId(), dto.dataHoraConsulta(), StatusConsulta.AGENDADA)).thenReturn(true);

                // Quando / Então
                assertThatThrownBy(() -> validador.validar(dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("O médico já possui uma consulta agendada nesse horário.");

                // Verifica se a validação parou no médico e nem chegou a bater no banco para checar o paciente
                verify(repository, never()).existsByPatientIdAndDataHoraConsultaAndStatus(any(), any(), any());
            }

            @DisplayName("Dado que o paciente já possui uma consulta no horário")
            @Test
            void teste2() {
                // Dado
                var dto = criarRequestDTO();
                when(repository.existsByDoctorIdAndDataHoraConsultaAndStatus(
                        dto.doctorId(), dto.dataHoraConsulta(), StatusConsulta.AGENDADA)).thenReturn(false);
                when(repository.existsByPatientIdAndDataHoraConsultaAndStatus(
                        dto.patientId(), dto.dataHoraConsulta(), StatusConsulta.AGENDADA)).thenReturn(true);

                // Quando / Então
                assertThatThrownBy(() -> validador.validar(dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("O paciente já possui uma consulta agendada nesse horário.");
            }
        }
    }
}