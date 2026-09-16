package com.example.appointment_service.control;

import com.example.appointment_service.dto.AppointmentRequestDTO;
import com.example.appointment_service.service.AppointmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @InjectMocks
    AppointmentController controller;

    @Mock
    AppointmentService service;

    // --- MÉTODOS AUXILIARES ---
    private AppointmentRequestDTO criarRequestDTO() {
        return new AppointmentRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now().plusDays(2)
        );
    }

    @DisplayName("Quando receber requisição para agendar consulta")
    @Nested
    class AgendarConsulta {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um request válido, deve repassar ao service e retornar HTTP 201 (Created)")
            @Test
            void teste1() {
                // Dado
                var requestBody = criarRequestDTO();
                var responseDoService = criarRequestDTO(); // Simulando o retorno do service

                when(service.agendarConsulta(requestBody)).thenReturn(responseDoService);

                // Quando
                ResponseEntity<AppointmentRequestDTO> resposta = controller.agendarConsulta(requestBody);

                // Então
                assertThat(resposta).isNotNull();
                assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(resposta.getBody()).isEqualTo(responseDoService);

                verify(service, times(1)).agendarConsulta(requestBody);
            }
        }

        @DisplayName("Então deve lançar erro de validação (repassando a exceção)")
        @Nested
        class Falha {

            @DisplayName("Dado que o service lança uma exceção de regra de negócio, o controller deve deixar a exceção subir")
            @Test
            void teste1() {
                // Dado
                var requestBody = criarRequestDTO();

                // Simula o service barrando o agendamento (ex: choque de horário)
                doThrow(new IllegalArgumentException("O médico já possui uma consulta agendada nesse horário."))
                        .when(service).agendarConsulta(any(AppointmentRequestDTO.class));

                // Quando / Então
                // O controller não faz try-catch, ele delega o erro para o @ControllerAdvice do Spring capturar
                assertThatThrownBy(() -> controller.agendarConsulta(requestBody))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("O médico já possui uma consulta agendada nesse horário.");

                verify(service, times(1)).agendarConsulta(requestBody);
            }
        }
    }
}