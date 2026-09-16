package com.example.appointment_service.service.validacoes;

import com.example.appointment_service.dto.AppointmentRequestDTO;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class ValidadorHorarioAntecedencia implements ValidadorAgendamentoDeConsulta {

    @Override
    public void validar(AppointmentRequestDTO dados) {
        LocalDateTime dataConsulta = dados.dataHoraConsulta();
        LocalDateTime agora = LocalDateTime.now();

        if (dataConsulta.isBefore(agora)) {
            throw new IllegalArgumentException("A consulta deve ser agendada passado.");
        }

        long minutosDeDiferenca = Duration.between(agora, dataConsulta).toMinutes();
        if (minutosDeDiferenca < 30) {
            throw new IllegalArgumentException("A consulta deve ser agendada com pelo menos 30 minutos de antecedência.");
        }
    }
}
