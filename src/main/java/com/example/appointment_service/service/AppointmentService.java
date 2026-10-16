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
import java.util.UUID;
import java.time.LocalDateTime;
import com.example.appointment_service.dto.AppointmentResponseDTO;
import com.example.appointment_service.model.StatusConsulta;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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
        // Validate ownership before making calls to other services.
        validadores.forEach(validador -> validador.validar(request));
        try {
            identityClient.getDoctorById(request.doctorId());
            identityClient.getPatientById(request.patientId());
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("Médico ou paciente não encontrado.");
        }


        Appointment appointment = new Appointment();
        appointment.agendar(request);

        Appointment saved = repository.save(appointment);

        return new AppointmentRequestDTO(saved);
    }

    public List<AppointmentResponseDTO> listar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<Appointment> rows;
        if (hasRole(auth, "PACIENTE")) rows = repository.findAllByPatientIdOrderByDataHoraConsultaAsc(UUID.fromString(auth.getName()));
        else if (hasRole(auth, "MEDICO")) rows = repository.findAllByDoctorIdOrderByDataHoraConsultaAsc(UUID.fromString(auth.getName()));
        else rows = repository.findAllByOrderByDataHoraConsultaAsc();
        return rows.stream().map(this::detalhes).toList();
    }

    public Appointment buscar(UUID id) {
        Appointment appointment = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta não encontrada."));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if ((hasRole(auth, "PACIENTE") && !appointment.getPatientId().toString().equals(auth.getName()))
                || (hasRole(auth, "MEDICO") && !appointment.getDoctorId().toString().equals(auth.getName()))) {
            throw new AccessDeniedException("Você não tem acesso a esta consulta.");
        }
        return appointment;
    }

    public AppointmentResponseDTO detalhes(Appointment appointment) {
        return new AppointmentResponseDTO(appointment.getId(), appointment.getDoctorId(), appointment.getPatientId(), appointment.getDataHoraConsulta(), appointment.getStatus().name());
    }

    @Transactional
    public AppointmentResponseDTO atualizarStatus(UUID id, StatusConsulta status) {
        Appointment appointment = buscar(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        StatusConsulta current = appointment.getStatus();
        boolean reception = hasRole(auth, "RECEPCAO") || hasRole(auth, "ADMIN");
        boolean doctor = hasRole(auth, "MEDICO");
        boolean allowed = switch (status) {
            case CANCELADA -> (reception || hasRole(auth, "PACIENTE")) && (current == StatusConsulta.AGENDADA || current == StatusConsulta.EM_ESPERA);
            case EM_ESPERA -> reception && current == StatusConsulta.AGENDADA;
            case EM_ATENDIMENTO -> doctor && (current == StatusConsulta.AGENDADA || current == StatusConsulta.EM_ESPERA);
            case REALIZADA -> doctor && current == StatusConsulta.EM_ATENDIMENTO;
            case NAO_COMPARECEU -> reception && current == StatusConsulta.AGENDADA && appointment.getDataHoraConsulta().isBefore(LocalDateTime.now());
            default -> false;
        };
        if (!allowed) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mudança de status não permitida para esta consulta e perfil.");
        appointment.setStatus(status);
        return detalhes(repository.save(appointment));
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
