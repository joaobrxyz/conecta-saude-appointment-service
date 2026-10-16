package com.example.appointment_service.service;

import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.model.StatusConsulta;
import com.example.appointment_service.repository.AppointmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentLifecycleTest {
    @Mock AppointmentRepository repository;
    @InjectMocks AppointmentService service;
    private final UUID patient = UUID.randomUUID();
    private final UUID doctor = UUID.randomUUID();
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    private void login(UUID id, String role) { SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(id.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)))); }
    private Appointment appointment(StatusConsulta status) {
        var appointment = new Appointment(); appointment.setId(UUID.randomUUID()); appointment.setPatientId(patient); appointment.setDoctorId(doctor); appointment.setStatus(status); appointment.setDataHoraConsulta(LocalDateTime.now().plusDays(1));
        when(repository.findById(appointment.getId())).thenReturn(Optional.of(appointment)); return appointment;
    }
    @Test void patientListIsScopedToTokenSubject() {
        login(patient, "PACIENTE"); when(repository.findAllByPatientIdOrderByDataHoraConsultaAsc(patient)).thenReturn(List.of()); assertThat(service.listar()).isEmpty(); verify(repository, never()).findAllByOrderByDataHoraConsultaAsc();
    }
    @Test void anotherPatientCannotReadAppointment() {
        login(UUID.randomUUID(), "PACIENTE"); var a = appointment(StatusConsulta.AGENDADA); assertThatThrownBy(() -> service.buscar(a.getId())).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
    @Test void patientCannotPerformCheckIn() {
        login(patient, "PACIENTE"); var a = appointment(StatusConsulta.AGENDADA); assertThatThrownBy(() -> service.atualizarStatus(a.getId(), StatusConsulta.EM_ESPERA)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class); verify(repository, never()).save(any());
    }
    @Test void receptionCanCheckIn() {
        login(UUID.randomUUID(), "RECEPCAO"); var a = appointment(StatusConsulta.AGENDADA); when(repository.save(a)).thenReturn(a); assertThat(service.atualizarStatus(a.getId(), StatusConsulta.EM_ESPERA).status()).isEqualTo("EM_ESPERA");
    }
    @Test void onlyAssignedDoctorCanStart() {
        login(UUID.randomUUID(), "MEDICO"); var a = appointment(StatusConsulta.EM_ESPERA); assertThatThrownBy(() -> service.atualizarStatus(a.getId(), StatusConsulta.EM_ATENDIMENTO)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
    @Test void doctorCanFinishStartedAppointment() {
        login(doctor, "MEDICO"); var a = appointment(StatusConsulta.EM_ATENDIMENTO); when(repository.save(a)).thenReturn(a); assertThat(service.atualizarStatus(a.getId(), StatusConsulta.REALIZADA).status()).isEqualTo("REALIZADA");
    }
    @Test void completedAppointmentCannotBeCancelled() {
        login(patient, "PACIENTE"); var a = appointment(StatusConsulta.REALIZADA); assertThatThrownBy(() -> service.atualizarStatus(a.getId(), StatusConsulta.CANCELADA)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class); verify(repository, never()).save(any());
    }
}
