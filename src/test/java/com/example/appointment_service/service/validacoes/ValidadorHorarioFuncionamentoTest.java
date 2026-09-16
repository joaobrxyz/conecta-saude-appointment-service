package com.example.appointment_service.service.validacoes;

import com.example.appointment_service.dto.AppointmentRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ValidadorHorarioFuncionamentoTest {

    @InjectMocks
    ValidadorHorarioFuncionamento validador;

    // --- MÉTODOS AUXILIARES ---
    private AppointmentRequestDTO criarRequestDTO(LocalDateTime dataHoraConsulta) {
        return new AppointmentRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                dataHoraConsulta
        );
    }

    @DisplayName("Quando validar o horário de funcionamento da clínica")
    @Nested
    class Validar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um horário comercial válido em um dia de semana (Segunda-feira às 10:00)")
            @Test
            void teste1() {
                // Dado: 19 de Outubro de 2026 é uma SEGUNDA-FEIRA
                var dataHora = LocalDateTime.of(2026, 10, 19, 10, 0);
                var dto = criarRequestDTO(dataHora);

                // Quando / Então
                assertThatCode(() -> validador.validar(dto)).doesNotThrowAnyException();
            }

            @DisplayName("Dado o último horário permitido do dia (17:30)")
            @Test
            void teste2() {
                // Dado
                var dataHora = LocalDateTime.of(2026, 10, 19, 17, 30);
                var dto = criarRequestDTO(dataHora);

                // Quando / Então
                assertThatCode(() -> validador.validar(dto)).doesNotThrowAnyException();
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado que a consulta está sendo agendada para um Domingo")
            @Test
            void teste1() {
                // Dado: 18 de Outubro de 2026 é um DOMINGO
                var dataHora = LocalDateTime.of(2026, 10, 18, 10, 0);
                var dto = criarRequestDTO(dataHora);

                // Quando / Então
                assertThatThrownBy(() -> validador.validar(dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Horário inválido. A clínica funciona de segunda a sábado, das 08h às 18h.");
            }

            @DisplayName("Dado que a consulta é antes do horário de abertura (ex: 07:59)")
            @Test
            void teste2() {
                // Dado
                var dataHora = LocalDateTime.of(2026, 10, 19, 7, 59);
                var dto = criarRequestDTO(dataHora);

                // Quando / Então
                assertThatThrownBy(() -> validador.validar(dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Horário inválido. A clínica funciona de segunda a sábado, das 08h às 18h.");
            }

            @DisplayName("Dado que a consulta é depois do último horário permitido (ex: 17:31)")
            @Test
            void teste3() {
                // Dado
                var dataHora = LocalDateTime.of(2026, 10, 19, 17, 31);
                var dto = criarRequestDTO(dataHora);

                // Quando / Então
                assertThatThrownBy(() -> validador.validar(dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Horário inválido. A clínica funciona de segunda a sábado, das 08h às 18h.");
            }
        }
    }
}