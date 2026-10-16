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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.example.appointment_service.dto.AppointmentResponseDTO;
import com.example.appointment_service.model.StatusConsulta;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    @Autowired
    private AppointmentService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCAO', 'PACIENTE')")
    public ResponseEntity<AppointmentRequestDTO> agendarConsulta(@RequestBody @Valid AppointmentRequestDTO request) {
        AppointmentRequestDTO response = service.agendarConsulta(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCAO', 'PACIENTE', 'MEDICO')")
    public List<AppointmentResponseDTO> listar() { return service.listar(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCAO', 'PACIENTE', 'MEDICO')")
    public AppointmentResponseDTO buscar(@PathVariable UUID id) { return service.detalhes(service.buscar(id)); }

    public record StatusRequest(@NotNull StatusConsulta status) {}

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCAO', 'PACIENTE', 'MEDICO')")
    public AppointmentResponseDTO atualizarStatus(@PathVariable UUID id, @RequestBody @Valid StatusRequest request) {
        return service.atualizarStatus(id, request.status());
    }
}
