package com.example.appointment_service.service.validacoes;

import com.example.appointment_service.dto.AppointmentRequestDTO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
public class ValidadorPermissaoPaciente implements ValidadorAgendamentoDeConsulta {
    @Override
    public void validar(AppointmentRequestDTO dados) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null){
            throw new AccessDeniedException("Falha de segurança: Nenhum usuário autenticado no contexto.");
        }

        boolean isPaciente = auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_PACIENTE"));

        if (isPaciente) {
            String userId = auth.getName();
            if (!userId.equals(dados.patientId().toString())) {
                throw new AccessDeniedException("Você não pode realizar agendamentos em nome de outro paciente.");
            }
        }
    }
}
