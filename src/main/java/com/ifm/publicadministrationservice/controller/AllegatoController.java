package com.ifm.publicadministrationservice.controller;

import com.ifm.publicadministrationservice.dto.AllegatoDTO;
import com.ifm.publicadministrationservice.entity.Allegato;
import com.ifm.publicadministrationservice.service.AllegatoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/richieste/{richiestaId}/allegati")
@Slf4j
public class AllegatoController {

    @Autowired
    private AllegatoService allegatoService;

    /**
     * Carica un nuovo allegato associandolo alla richiesta indicata.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATORE')")
    public ResponseEntity<AllegatoDTO> uploadAllegato(
            @PathVariable Long richiestaId,
            @RequestParam("file") MultipartFile file) {
        try {
            AllegatoDTO allegato = allegatoService.uploadAllegato(richiestaId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(allegato);
        } catch (IOException e) {
            log.error("Errore nel caricamento dell'allegato: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Errore inaspettato nel caricamento dell'allegato: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Restituisce tutti gli allegati collegati a una richiesta.
     */
    @GetMapping
    public ResponseEntity<List<AllegatoDTO>> getAllegati(@PathVariable Long richiestaId) {
        try {
            List<AllegatoDTO> allegati = allegatoService.getAllegati(richiestaId);
            return ResponseEntity.ok(allegati);
        } catch (Exception e) {
            log.error("Errore nel recupero degli allegati: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Scarica il file dell'allegato specificato.
     */
    @GetMapping("/{allegatoId}/download")
    public ResponseEntity<Resource> downloadAllegato(@PathVariable Long allegatoId) {
        try {
            Allegato allegato = allegatoService.getAllegato(allegatoId);
            Path filePath = Paths.get(allegato.getPathFile());
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(allegato.getTipoFile()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + allegato.getNomeFile() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Errore nel download dell'allegato: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina l'allegato selezionato sia dal database sia dal filesystem.
     */
    @DeleteMapping("/{allegatoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATORE')")
    public ResponseEntity<Void> deleteAllegato(@PathVariable Long allegatoId) {
        try {
            allegatoService.deleteAllegato(allegatoId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            log.error("Errore nella cancellazione dell'allegato: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Errore inaspettato nella cancellazione dell'allegato: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

