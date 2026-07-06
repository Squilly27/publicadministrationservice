package com.ifm.publicadministrationservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String SECRET =
            "P6p58A1D2h4FV/SlrBl9oHGUsN9MJolqsEzXl7/T71o61J2FNe9R6b7DxeC59tRI/lOUTTIGL5LEIgsOTJvSiw====";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 60_000);
    }

    // Verifica che il token generato sia valido e contenga l'username.
    @Test
    void generateTokenShouldBeValidAndContainUsername() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("mario", "password");

        String token = jwtTokenProvider.generateToken(authentication);

        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("mario", jwtTokenProvider.getUsernameFromToken(token));
    }

    // Verifica che un token malformato venga rifiutato.
    @Test
    void validateTokenShouldRejectMalformedToken() {
        assertFalse(jwtTokenProvider.validateToken("not-a-jwt"));
    }
}
