package com.example.appointment_service.dto;

import java.util.UUID;

public record PatientResponseDTO(
        UUID id,
        String nome,
        String email,
        String cpf,
        String telefone
) {
}
