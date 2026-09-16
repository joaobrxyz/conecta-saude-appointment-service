package com.example.appointment_service.service.validacoes;

import com.example.appointment_service.dto.AppointmentRequestDTO;
import com.example.appointment_service.model.StatusConsulta;
import com.example.appointment_service.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorChoqueHorario implements ValidadorAgendamentoDeConsulta {
    @Autowired
    private AppointmentRepository repository;

    @Override
    public void validar(AppointmentRequestDTO dados) {
        boolean medicoOcupado = repository.existsByDoctorIdAndDataHoraConsultaAndStatus(
                dados.doctorId(), dados.dataHoraConsulta(), StatusConsulta.AGENDADA
        );

        if (medicoOcupado) {
            throw new IllegalArgumentException("O médico já possui uma consulta agendada nesse horário.");
        }

        boolean pacienteOcupado = repository.existsByPatientIdAndDataHoraConsultaAndStatus(
                dados.patientId(), dados.dataHoraConsulta(), StatusConsulta.AGENDADA
        );

        if (pacienteOcupado) {
            throw new IllegalArgumentException("O paciente já possui uma consulta agendada nesse horário.");
        }
    }
}
