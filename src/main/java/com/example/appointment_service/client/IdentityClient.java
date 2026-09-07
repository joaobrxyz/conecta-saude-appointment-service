package com.example.appointment_service.client;

import com.example.appointment_service.client.config.FeignClientConfig;
import com.example.appointment_service.dto.DoctorResponseDTO;
import com.example.appointment_service.dto.PatientResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "identity-service",
        url = "${identity-service.url}",
        configuration = FeignClientConfig.class
)
public interface IdentityClient {
    @GetMapping("/doctors/{id}")
    DoctorResponseDTO getDoctorById(@PathVariable("id") UUID id);

    @GetMapping("/patients/{id}")
    PatientResponseDTO getPatientById(@PathVariable("id") UUID id);
}
