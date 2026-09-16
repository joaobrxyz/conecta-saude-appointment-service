package com.example.appointment_service.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    TokenService tokenService;

    private final String SECRET_TESTE = "chave-super-secreta-para-testes-unitarios";
    private final String ISSUER = "conecta-saude";

    @BeforeEach
    void setUp() {
        // Injeta a chave secreta no @Value da classe TokenService
        ReflectionTestUtils.setField(tokenService, "secret", SECRET_TESTE);
    }

    // --- MÉTODOS AUXILIARES ---
    private String gerarTokenValido(String subject, String role) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(subject)
                .withClaim("role", role)
                .withExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .sign(Algorithm.HMAC256(SECRET_TESTE));
    }

    private String gerarTokenComChaveErrada(String subject) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(subject)
                .withExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .sign(Algorithm.HMAC256("chave-diferente-errada"));
    }

    private String gerarTokenExpirado(String subject) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(subject)
                .withExpiresAt(Instant.now().minus(15, ChronoUnit.MINUTES)) // Data no passado
                .sign(Algorithm.HMAC256(SECRET_TESTE));
    }

    @DisplayName("Quando validar token JWT")
    @Nested
    class ValidarToken {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um token válido gerado com a mesma secret e issuer")
            @Test
            void teste1() {
                // Dado
                String userId = UUID.randomUUID().toString();
                String token = gerarTokenValido(userId, "PACIENTE");

                // Quando
                String subjectRecuperado = tokenService.validateToken(token);

                // Então
                assertThat(subjectRecuperado).isNotNull();
                assertThat(subjectRecuperado).isEqualTo(userId);
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado um token assinado com uma secret diferente (falsificado)")
            @Test
            void teste1() {
                // Dado
                String tokenFalsificado = gerarTokenComChaveErrada(UUID.randomUUID().toString());

                // Quando / Então
                assertThatThrownBy(() -> tokenService.validateToken(tokenFalsificado))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("Token JWT inválido ou expirado");
            }

            @DisplayName("Dado um token que já passou da data de expiração")
            @Test
            void teste2() {
                // Dado
                String tokenExpirado = gerarTokenExpirado(UUID.randomUUID().toString());

                // Quando / Então
                assertThatThrownBy(() -> tokenService.validateToken(tokenExpirado))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("Token JWT inválido ou expirado");
            }

            @DisplayName("Dado um token malformado (string aleatória)")
            @Test
            void teste3() {
                // Dado
                String tokenLixo = "eylixo.algumacoisa.errada";

                // Quando / Então
                assertThatThrownBy(() -> tokenService.validateToken(tokenLixo))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("Token JWT inválido ou expirado");
            }
        }
    }

    @DisplayName("Quando extrair a Role do token JWT")
    @Nested
    class ExtrairRole {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um token válido, deve retornar a claim 'role'")
            @Test
            void teste1() {
                // Dado
                String token = gerarTokenValido(UUID.randomUUID().toString(), "MEDICO");

                // Quando
                String role = tokenService.getRoleFromToken(token);

                // Então
                assertThat(role).isEqualTo("MEDICO");
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado um token malformado ou com assinatura inválida, deve lançar exceção do JWT")
            @Test
            void teste1() {
                // Dado
                String tokenInvalido = gerarTokenComChaveErrada(UUID.randomUUID().toString());

                // Quando / Então
                // Nota: O método getRoleFromToken não tem bloco try-catch, então ele joga a exceção nativa do Auth0
                assertThatThrownBy(() -> tokenService.getRoleFromToken(tokenInvalido))
                        .isInstanceOf(JWTVerificationException.class);
            }
        }
    }
}