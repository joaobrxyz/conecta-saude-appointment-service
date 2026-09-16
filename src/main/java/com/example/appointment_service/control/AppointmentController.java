package com.example.appointment_service.control;

import com.example.appointment_service.dto.AppointmentRequestDTO;
import com.example.appointment_service.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    @Autowired
    private AppointmentService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCAO', 'PACIENTE'")
    public ResponseEntity<AppointmentRequestDTO> agendarConsulta(@RequestBody AppointmentRequestDTO request) {
        AppointmentRequestDTO response = service.agendarConsulta(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
