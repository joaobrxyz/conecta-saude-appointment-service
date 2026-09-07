package com.example.appointment_service.dto;

import java.util.UUID;

public record DoctorResponseDTO(
        UUID id,
        String nome,
        String email,
        String crm,
        String especialidade,
        String telefone
) {
}
