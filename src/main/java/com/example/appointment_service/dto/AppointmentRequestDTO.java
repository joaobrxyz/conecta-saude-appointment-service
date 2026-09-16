package com.example.appointment_service.dto;

import com.example.appointment_service.model.Appointment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentRequestDTO(
        UUID doctorId,
        UUID patientId,
        LocalDateTime dataHoraConsulta
) {
    public AppointmentRequestDTO(Appointment appointment) {
        this(
                appointment.getDoctorId(),
                appointment.getPatientId(),
                appointment.getDataHoraConsulta()
        );
    }
}
