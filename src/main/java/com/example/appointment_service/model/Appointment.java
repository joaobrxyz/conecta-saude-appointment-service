package com.example.appointment_service.model;

import com.example.appointment_service.dto.AppointmentRequestDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "data_hora_consulta", nullable = false)
    private LocalDateTime dataHoraConsulta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status;

    @Column(name = "motivo_cancelamento")
    private String motivoCancelamento;

    public void agendar(AppointmentRequestDTO request) {
        this.doctorId = request.doctorId();
        this.patientId = request.patientId();
        this.dataHoraConsulta = request.dataHoraConsulta();
        this.status = StatusConsulta.AGENDADA;
    }
}
