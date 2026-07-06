package com.ifm.publicadministrationservice.security;

import com.ifm.publicadministrationservice.entity.RuoloUtente;
import com.ifm.publicadministrationservice.entity.Utente;
import com.ifm.publicadministrationservice.repository.UtenteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UtenteRepository utenteRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    // Verifica che ruolo e credenziali vengano mappati in UserDetails.
    @Test
    void loadUserByUsernameShouldMapRoleToGrantedAuthority() {
        Utente utente = Utente.builder()
                .username("mario")
                .password("hashed-password")
                .ruolo(RuoloUtente.RESPONSABILE)
                .attivo(true)
                .build();

        when(utenteRepository.findByUsername("mario")).thenReturn(Optional.of(utente));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("mario");

        assertEquals("mario", userDetails.getUsername());
        assertEquals("hashed-password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RESPONSABILE")));
    }

    // Verifica che un utente assente generi UsernameNotFoundException.
    @Test
    void loadUserByUsernameShouldThrowWhenMissing() {
        when(utenteRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("missing"));
    }
}
