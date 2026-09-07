package com.example.appointment_service.repository;

import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.model.StatusConsulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    boolean existsByDoctorIdAndDataHoraConsultaAndStatus(
            UUID doctorId,
            LocalDateTime dataHoraConsulta,
            StatusConsulta status
    );

    boolean existsByPatientIdAndDataHoraConsultaAndStatus(
            UUID patientId,
            LocalDateTime dataHoraConsulta,
            StatusConsulta status
    );
}
