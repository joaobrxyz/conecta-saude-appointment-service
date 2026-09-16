package com.example.appointment_service.service.validacoes;

import com.example.appointment_service.dto.AppointmentRequestDTO;

public interface ValidadorAgendamentoDeConsulta {
    void validar(AppointmentRequestDTO dados);
}
