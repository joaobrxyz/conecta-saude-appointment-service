package com.example.appointment_service.dto;

import com.example.appointment_service.model.Appointment;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record AppointmentRequestDTO(
        @NotNull UUID doctorId,
        @NotNull UUID patientId,
        @NotNull LocalDateTime dataHoraConsulta
) {
    public AppointmentRequestDTO(Appointment appointment) {
        this(
                appointment.getDoctorId(),
                appointment.getPatientId(),
                appointment.getDataHoraConsulta()
        );
    }
}
