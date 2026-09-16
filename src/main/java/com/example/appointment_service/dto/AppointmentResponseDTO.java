package com.example.appointment_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponseDTO(
        UUID id,
        UUID doctorId,
        UUID patientId,
        LocalDateTime dataHoraConsulta,
        String status
) {
}
