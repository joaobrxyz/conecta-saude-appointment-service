package com.example.appointment_service.service.validacoes;

import com.example.appointment_service.dto.AppointmentRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ValidadorPermissaoPacienteTest {

    @InjectMocks
    ValidadorPermissaoPaciente validador;

    // Garante que o usuário logado de um teste não suje o teste seguinte!
    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    // --- MÉTODOS AUXILIARES ---
    private AppointmentRequestDTO criarRequestDTO(UUID patientId) {
        return new AppointmentRequestDTO(
                UUID.randomUUID(),
                patientId,
                LocalDateTime.now().plusDays(2)
        );
    }

    private void simularUsuarioLogado(String role, String userId) {
        var authorities = List.of(new SimpleGrantedAuthority(role));
        var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @DisplayName("Quando validar a permissão do paciente para agendar")
    @Nested
    class Validar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um usuário logado com ROLE_PACIENTE agendando para ele mesmo")
            @Test
            void teste1() {
                // Dado
                UUID pacienteId = UUID.randomUUID();
                var dto = criarRequestDTO(pacienteId);

                // Simula o contexto do Spring Security
                simularUsuarioLogado("ROLE_PACIENTE", pacienteId.toString());

                // Quando / Então
                assertThatCode(() -> validador.validar(dto)).doesNotThrowAnyException();
            }

            @DisplayName("Dado um usuário logado com ROLE_RECEPCAO (ou ADMIN) agendando para um paciente qualquer")
            @Test
            void teste2() {
                // Dado
                UUID pacienteIdDaConsulta = UUID.randomUUID();
                var dto = criarRequestDTO(pacienteIdDaConsulta);

                // Simula uma recepcionista logada
                String idRecepcionista = UUID.randomUUID().toString();
                simularUsuarioLogado("ROLE_RECEPCAO", idRecepcionista);

                // Quando / Então
                assertThatCode(() -> validador.validar(dto)).doesNotThrowAnyException();
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado que não existe nenhum usuário autenticado no contexto (auth == null)")
            @Test
            void teste1() {
                // Dado
                var dto = criarRequestDTO(UUID.randomUUID());
                SecurityContextHolder.clearContext(); // Garante que é nulo

                // Quando / Então
                assertThatThrownBy(() -> validador.validar(dto))
                        .isInstanceOf(AccessDeniedException.class)
                        .hasMessage("Falha de segurança: Nenhum usuário autenticado no contexto.");
            }

            @DisplayName("Dado um usuário logado com ROLE_PACIENTE tentando agendar para o ID de outro paciente")
            @Test
            void teste2() {
                // Dado
                UUID pacienteIdDoCorpoDaRequisicao = UUID.randomUUID();
                var dto = criarRequestDTO(pacienteIdDoCorpoDaRequisicao);

                // Simula um paciente diferente (mal intencionado) logado
                String pacienteIdDoToken = UUID.randomUUID().toString();
                simularUsuarioLogado("ROLE_PACIENTE", pacienteIdDoToken);

                // Quando / Então
                assertThatThrownBy(() -> validador.validar(dto))
                        .isInstanceOf(AccessDeniedException.class)
                        .hasMessage("Você não pode realizar agendamentos em nome de outro paciente.");
            }
        }
    }
}