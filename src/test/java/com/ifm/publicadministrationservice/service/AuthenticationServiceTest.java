package com.ifm.publicadministrationservice.service;

import com.ifm.publicadministrationservice.dto.CreateUtenteDTO;
import com.ifm.publicadministrationservice.dto.LoginRequest;
import com.ifm.publicadministrationservice.dto.LoginResponse;
import com.ifm.publicadministrationservice.entity.RuoloUtente;
import com.ifm.publicadministrationservice.entity.Utente;
import com.ifm.publicadministrationservice.repository.UtenteRepository;
import com.ifm.publicadministrationservice.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    // Verifica che il login restituisca token e dati profilo corretti.
    @Test
    void loginShouldReturnTokenAndProfileData() {
        LoginRequest request = LoginRequest.builder()
                .username("mario")
                .password("password")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken("mario", "password");
        Utente utente = Utente.builder()
                .id(7L)
                .username("mario")
                .nomeCompleto("Mario Rossi")
                .email("mario@example.com")
                .ruolo(RuoloUtente.ADMIN)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(utenteRepository.findByUsername("mario")).thenReturn(Optional.of(utente));

        LoginResponse response = authenticationService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals(7L, response.getUserId());
        assertEquals("mario", response.getUsername());
        assertEquals("Mario Rossi", response.getNomeCompleto());
        assertEquals("mario@example.com", response.getEmail());
        assertEquals(RuoloUtente.ADMIN, response.getRuolo());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
    }

    // Verifica che la registrazione codifichi la password e salvi l'utente.
    @Test
    void registerShouldEncodePasswordAndSaveUser() {
        CreateUtenteDTO dto = CreateUtenteDTO.builder()
                .username("mario")
                .password("plain-password")
                .nomeCompleto("Mario Rossi")
                .email("mario@example.com")
                .ruolo(RuoloUtente.OPERATORE)
                .build();

        when(utenteRepository.findByUsername("mario")).thenReturn(Optional.empty());
        when(utenteRepository.findByEmail("mario@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        authenticationService.register(dto);

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(utenteRepository).save(captor.capture());
        assertEquals("mario", captor.getValue().getUsername());
        assertEquals("encoded-password", captor.getValue().getPassword());
        assertEquals("Mario Rossi", captor.getValue().getNomeCompleto());
        assertEquals("mario@example.com", captor.getValue().getEmail());
        assertEquals(RuoloUtente.OPERATORE, captor.getValue().getRuolo());
        assertTrue(captor.getValue().getAttivo());
    }

    // Verifica che un username già presente blocchi la registrazione.
    @Test
    void registerShouldRejectDuplicateUsername() {
        CreateUtenteDTO dto = CreateUtenteDTO.builder()
                .username("mario")
                .password("plain-password")
                .nomeCompleto("Mario Rossi")
                .email("mario@example.com")
                .ruolo(RuoloUtente.OPERATORE)
                .build();

        when(utenteRepository.findByUsername("mario")).thenReturn(Optional.of(new Utente()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authenticationService.register(dto));

        assertEquals("Username già in uso", ex.getMessage());
        verify(utenteRepository, never()).save(any(Utente.class));
    }
}
