package com.example.appointment_service.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("conecta-saude")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (Exception exception) {
            throw new RuntimeException("Token JWT inválido ou expirado", exception);
        }
    }

    public String getRoleFromToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm)
                .withIssuer("conecta-saude")
                .build()
                .verify(token)
                .getClaim("role").asString();
    }
}
