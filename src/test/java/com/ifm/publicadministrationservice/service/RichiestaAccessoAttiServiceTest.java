package com.ifm.publicadministrationservice.service;

import com.ifm.publicadministrationservice.dto.CreateRichiestaDTO;
import com.ifm.publicadministrationservice.dto.RichiestaAccessoAttiDTO;
import com.ifm.publicadministrationservice.entity.RichiestaAccessoAtti;
import com.ifm.publicadministrationservice.entity.StatoRichiesta;
import com.ifm.publicadministrationservice.repository.RichiestaAccessoAttiRepository;
import com.ifm.publicadministrationservice.repository.StoricoStatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RichiestaAccessoAttiServiceTest {

    @Mock
    private RichiestaAccessoAttiRepository richiestaRepository;

    @Mock
    private StoricoStatoRepository storicoRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RichiestaAccessoAttiService richiestaService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    // Controlla che una richiesta venga creata e salvata con storico iniziale.
    @Test
    void testCreateRichiesta() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        CreateRichiestaDTO dto = CreateRichiestaDTO.builder()
                .nomeRichiedente("Giovanni")
                .cognomeRichiedente("Rossi")
                .emailRichiedente("giovanni@example.com")
                .telefonoRichiedente("3335551234")
                .oggetto("Test oggetto")
                .descrizione("Test descrizione")
                .build();

        RichiestaAccessoAtti richiesta = new RichiestaAccessoAtti();
        richiesta.setId(1L);
        richiesta.setStato(StatoRichiesta.PRESENTATA);

        when(richiestaRepository.save(any(RichiestaAccessoAtti.class))).thenReturn(richiesta);

        // Act
        RichiestaAccessoAttiDTO result = richiestaService.createRichiesta(dto);

        // Assert
        assertNotNull(result);
        verify(richiestaRepository, times(1)).save(any(RichiestaAccessoAtti.class));
        verify(storicoRepository, times(1)).save(any());
    }

    // Verifica il recupero di una richiesta esistente per id.
    @Test
    void testGetRichiestaById() {
        // Arrange
        Long id = 1L;
        RichiestaAccessoAtti richiesta = new RichiestaAccessoAtti();
        richiesta.setId(id);

        when(richiestaRepository.findById(id)).thenReturn(Optional.of(richiesta));

        // Act
        RichiestaAccessoAttiDTO result = richiestaService.getRichiestaById(id);

        // Assert
        assertNotNull(result);
        verify(richiestaRepository, times(1)).findById(id);
    }

    // Verifica che venga sollevata un'eccezione se la richiesta non esiste.
    @Test
    void testGetRichiestaByIdNotFound() {
        // Arrange
        Long id = 999L;
        when(richiestaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> richiestaService.getRichiestaById(id));
    }
}
