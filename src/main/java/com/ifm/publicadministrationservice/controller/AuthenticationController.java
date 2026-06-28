package com.ifm.publicadministrationservice.controller;

import com.ifm.publicadministrationservice.dto.LoginRequest;
import com.ifm.publicadministrationservice.dto.LoginResponse;
import com.ifm.publicadministrationservice.dto.CreateUtenteDTO;
import com.ifm.publicadministrationservice.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authenticationService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Errore nel login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody CreateUtenteDTO dto) {
        try {
            authenticationService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Utente registrato con successo");
        } catch (RuntimeException e) {
            log.error("Errore nella registrazione: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

