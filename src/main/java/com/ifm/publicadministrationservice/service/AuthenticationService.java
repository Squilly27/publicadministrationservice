package com.ifm.publicadministrationservice.service;

import com.ifm.publicadministrationservice.dto.CreateUtenteDTO;
import com.ifm.publicadministrationservice.dto.LoginRequest;
import com.ifm.publicadministrationservice.dto.LoginResponse;
import com.ifm.publicadministrationservice.entity.Utente;
import com.ifm.publicadministrationservice.repository.UtenteRepository;
import com.ifm.publicadministrationservice.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthenticationService {

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = tokenProvider.generateToken(authentication);

        Utente utente = utenteRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        log.info("Utente {} ha effettuato il login", request.getUsername());

        return LoginResponse.builder()
                .token(token)
                .userId(utente.getId())
                .username(utente.getUsername())
                .nomeCompleto(utente.getNomeCompleto())
                .email(utente.getEmail())
                .ruolo(utente.getRuolo())
                .build();
    }

    @Transactional
    public void register(CreateUtenteDTO dto) {
        if (utenteRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username già in uso");
        }

        if (utenteRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email già registrata");
        }

        Utente utente = Utente.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nomeCompleto(dto.getNomeCompleto())
                .email(dto.getEmail())
                .ruolo(dto.getRuolo())
                .attivo(true)
                .build();

        utenteRepository.save(utente);
        log.info("Nuovo utente registrato: {}", dto.getUsername());
    }
}

