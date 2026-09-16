package com.example.appointment_service.service;

import com.example.appointment_service.client.IdentityClient;
import com.example.appointment_service.dto.AppointmentRequestDTO;
import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.repository.AppointmentRepository;
import com.example.appointment_service.service.validacoes.ValidadorAgendamentoDeConsulta;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository repository;

    @Autowired
    private IdentityClient identityClient;

    @Autowired
    private List<ValidadorAgendamentoDeConsulta> validadores;

    @Transactional
    public AppointmentRequestDTO agendarConsulta(AppointmentRequestDTO request) {
        try {
            identityClient.getDoctorById(request.doctorId());
            identityClient.getPatientById(request.patientId());
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("Médico ou paciente não encontrado.");
        }

        validadores.forEach(validador -> validador.validar(request));

        Appointment appointment = new Appointment();
        appointment.agendar(request);

        Appointment saved = repository.save(appointment);

        return new AppointmentRequestDTO(saved);
    }
}
