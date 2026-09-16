package com.example.appointment_service.service.validacoes;

import com.example.appointment_service.dto.AppointmentRequestDTO;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class ValidadorHorarioFuncionamento implements ValidadorAgendamentoDeConsulta {

    @Override
    public void validar(AppointmentRequestDTO dados) {
        LocalDateTime dataConsulta = dados.dataHoraConsulta();

        boolean domingo = dataConsulta.getDayOfWeek().equals(DayOfWeek.SUNDAY);

        LocalTime horaDaConsulta = dataConsulta.toLocalTime();

        LocalTime horarioAbertura = LocalTime.of(8, 0);
        LocalTime ultimoHorarioPermitido = LocalTime.of(17, 30);

        boolean antesDaAbertura = horaDaConsulta.isBefore(horarioAbertura);
        boolean depoisDoFechamento = horaDaConsulta.isAfter(ultimoHorarioPermitido);

        if (domingo || antesDaAbertura || depoisDoFechamento) {
            throw new IllegalArgumentException("Horário inválido. A clínica funciona de segunda a sábado, das 08h às 18h.");
        }
    }
}