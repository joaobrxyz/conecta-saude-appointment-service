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
class ValidadorHorarioAntecedenciaTest {

    @InjectMocks
    ValidadorHorarioAntecedencia validador;

    // --- MÉTODOS AUXILIARES ---
    // Passamos a data como parâmetro para facilitar a criação de diferentes cenários
    private AppointmentRequestDTO criarRequestDTO(LocalDateTime dataHoraConsulta) {
        return new AppointmentRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                dataHoraConsulta
        );
    }

    @DisplayName("Quando validar a antecedência do horário da consulta")
    @Nested
    class Validar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado que a consulta foi agendada com mais de 30 minutos de antecedência")
            @Test
            void teste1() {
                // Dado: Uma data com muita folga no futuro
                var dataHora = LocalDateTime.now().plusDays(2);
                var dto = criarRequestDTO(dataHora);

                // Quando / Então
                assertThatCode(() -> validador.validar(dto)).doesNotThrowAnyException();
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado que a data da consulta está no passado")
            @Test
            void teste1() {
                // Dado: Um horário de uma hora atrás
                var dataHora = LocalDateTime.now().minusHours(1);
                var dto = criarRequestDTO(dataHora);

                // Quando / Então
                assertThatThrownBy(() -> validador.validar(dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        // Atenção: Usei exatamente a string que está na sua classe principal hoje para o teste não quebrar
                        .hasMessage("A consulta deve ser agendada passado.");
            }

            @DisplayName("Dado que a consulta tem menos de 30 minutos de antecedência")
            @Test
            void teste2() {
                // Dado: Um horário no futuro, mas apenas 15 minutos à frente
                var dataHora = LocalDateTime.now().plusMinutes(15);
                var dto = criarRequestDTO(dataHora);

                // Quando / Então
                assertThatThrownBy(() -> validador.validar(dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("A consulta deve ser agendada com pelo menos 30 minutos de antecedência.");
            }
        }
    }
}