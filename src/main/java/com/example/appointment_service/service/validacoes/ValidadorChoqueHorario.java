package com.example.appointment_service.service.validacoes;

import com.example.appointment_service.dto.AppointmentRequestDTO;
import com.example.appointment_service.model.StatusConsulta;
import com.example.appointment_service.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ValidadorChoqueHorario implements ValidadorAgendamentoDeConsulta {
    @Autowired
    private AppointmentRepository repository;

    @Override
    public void validar(AppointmentRequestDTO dados) {
        List<StatusConsulta> ativos = List.of(StatusConsulta.AGENDADA, StatusConsulta.EM_ESPERA, StatusConsulta.EM_ATENDIMENTO);
        boolean medicoOcupado = repository.existsByDoctorIdAndDataHoraConsultaAndStatusIn(
                dados.doctorId(), dados.dataHoraConsulta(), ativos
        );

        if (medicoOcupado) {
            throw new IllegalArgumentException("O médico já possui uma consulta agendada nesse horário.");
        }

        boolean pacienteOcupado = repository.existsByPatientIdAndDataHoraConsultaAndStatusIn(
                dados.patientId(), dados.dataHoraConsulta(), ativos
        );

        if (pacienteOcupado) {
            throw new IllegalArgumentException("O paciente já possui uma consulta agendada nesse horário.");
        }
    }
}
