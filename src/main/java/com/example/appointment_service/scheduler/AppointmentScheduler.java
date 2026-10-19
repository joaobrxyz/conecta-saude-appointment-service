package com.example.appointment_service.scheduler;

import com.example.appointment_service.repository.AppointmentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AppointmentScheduler {

    private static final Logger log = LoggerFactory.getLogger(AppointmentScheduler.class);
    private final AppointmentRepository repository;

    public AppointmentScheduler(AppointmentRepository repository) {
        this.repository = repository;
    }

    // Cron: Segundo 0, Minuto 0, Hora 8 (Roda todos os dias às 08:00 em ponto)
    @Scheduled(cron = "0 0 8 * * *")
    public void autoUpdateNoShows() {
        log.info("Verificando consultas abandonadas de dias anteriores...");

        // Pega exatamente a meia-noite do dia de HOJE (ex: se hoje é dia 17, ele pega 17/10/2026 00:00:00)
        LocalDateTime inicioDeHoje = LocalDate.now().atStartOfDay();

        // Como ele vai buscar < inicioDeHoje, qualquer consulta do dia 16 (mesmo as de 17:30) será pega!
        int updatedCount = repository.markPastAppointmentsAsNoShow(inicioDeHoje);

        if (updatedCount > 0) {
            log.info("{} consultas de dias anteriores foram atualizadas para NAO_COMPARECEU.", updatedCount);
        }
    }
}