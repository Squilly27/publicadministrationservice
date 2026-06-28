package com.ifm.publicadministrationservice.controller;

import com.ifm.publicadministrationservice.dto.CambioStatoDTO;
import com.ifm.publicadministrationservice.dto.CreateRichiestaDTO;
import com.ifm.publicadministrationservice.dto.RichiestaAccessoAttiDTO;
import com.ifm.publicadministrationservice.entity.StatoRichiesta;
import com.ifm.publicadministrationservice.service.RichiestaAccessoAttiService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/richieste")
@Slf4j
public class RichiestaAccessoAttiController {

    @Autowired
    private RichiestaAccessoAttiService richiestaService;

    @PostMapping
    @PreAuthorize("hasRole('OPERATORE')")
    public ResponseEntity<RichiestaAccessoAttiDTO> createRichiesta(@Valid @RequestBody CreateRichiestaDTO dto) {
        try {
            RichiestaAccessoAttiDTO richiesta = richiestaService.createRichiesta(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(richiesta);
        } catch (Exception e) {
            log.error("Errore nella creazione della richiesta [{}]: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<RichiestaAccessoAttiDTO>> getAllRichieste(
            @RequestParam(required = false) Optional<StatoRichiesta> stato,
            @RequestParam(required = false) Optional<String> cognomeRichiedente,
            @RequestParam(required = false) Optional<String> numeroProtocollo,
            Pageable pageable) {
        try {
            Page<RichiestaAccessoAttiDTO> richieste = richiestaService.filterRichieste(
                    stato, cognomeRichiedente, numeroProtocollo, pageable);
            return ResponseEntity.ok(richieste);
        } catch (Exception e) {
            log.error("Errore nel recupero delle richieste [{}]: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RichiestaAccessoAttiDTO> getRichiestaById(@PathVariable Long id) {
        try {
            RichiestaAccessoAttiDTO richiesta = richiestaService.getRichiestaById(id);
            return ResponseEntity.ok(richiesta);
        } catch (Exception e) {
            log.error("Errore nel recupero della richiesta [{}]: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERATORE')")
    public ResponseEntity<RichiestaAccessoAttiDTO> updateRichiesta(
            @PathVariable Long id,
            @Valid @RequestBody CreateRichiestaDTO dto) {
        try {
            RichiestaAccessoAttiDTO richiesta = richiestaService.updateRichiesta(id, dto);
            return ResponseEntity.ok(richiesta);
        } catch (Exception e) {
            log.error("Errore nell'aggiornamento della richiesta [{}]: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{id}/cambio-stato")
    @PreAuthorize("hasRole('RESPONSABILE')")
    public ResponseEntity<RichiestaAccessoAttiDTO> cambiaStato(
            @PathVariable Long id,
            @Valid @RequestBody CambioStatoDTO dto) {
        try {
            RichiestaAccessoAttiDTO richiesta = richiestaService.cambiaStato(id, dto);
            return ResponseEntity.ok(richiesta);
        } catch (RuntimeException e) {
            log.error("Errore nel cambio di stato [{}]: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Errore inaspettato nel cambio di stato [{}]: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

