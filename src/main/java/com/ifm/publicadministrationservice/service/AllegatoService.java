package com.ifm.publicadministrationservice.service;

import com.ifm.publicadministrationservice.dto.AllegatoDTO;
import com.ifm.publicadministrationservice.entity.Allegato;
import com.ifm.publicadministrationservice.entity.RichiestaAccessoAtti;
import com.ifm.publicadministrationservice.repository.AllegatoRepository;
import com.ifm.publicadministrationservice.repository.RichiestaAccessoAttiRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AllegatoService {

    @Autowired
    private AllegatoRepository allegatoRepository;

    @Autowired
    private RichiestaAccessoAttiRepository richiestaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${allegati.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Salva un allegato su filesystem e database associandolo alla richiesta.
     */
    @Transactional
    public AllegatoDTO uploadAllegato(Long richiestaId, MultipartFile file) throws IOException {
        RichiestaAccessoAtti richiesta = richiestaRepository.findById(richiestaId)
                .orElseThrow(() -> new RuntimeException("Richiesta non trovata"));

        // Crea la directory se non esiste
        Path uploadPath = Paths.get(uploadDir, richiestaId.toString());
        Files.createDirectories(uploadPath);

        // Genera un nome file unico
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Salva il file
        Files.copy(file.getInputStream(), filePath);

        String username = getUsername();

        // Crea l'entità Allegato
        Allegato allegato = Allegato.builder()
                .richiesta(richiesta)
                .nomeFile(file.getOriginalFilename())
                .tipoFile(file.getContentType())
                .dimensione(file.getSize())
                .pathFile(filePath.toString())
                .caricatoDa(username)
                .build();

        allegato = allegatoRepository.save(allegato);
        log.info("Allegato caricato: {} per la richiesta {}", file.getOriginalFilename(), richiestaId);

        return modelMapper.map(allegato, AllegatoDTO.class);
    }

    /**
     * Restituisce tutti gli allegati associati a una richiesta.
     */
    public List<AllegatoDTO> getAllegati(Long richiestaId) {
        return allegatoRepository.findByRichiestaId(richiestaId).stream()
                .map(allegato -> modelMapper.map(allegato, AllegatoDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Recupera un allegato tramite identificativo o solleva un errore se non esiste.
     */
    public Allegato getAllegato(Long allegatoId) {
        return allegatoRepository.findById(allegatoId)
                .orElseThrow(() -> new RuntimeException("Allegato non trovato"));
    }

    /**
     * Elimina il file dal filesystem e la relativa riga dal database.
     */
    @Transactional
    public void deleteAllegato(Long allegatoId) throws IOException {
        Allegato allegato = getAllegato(allegatoId);
        Path filePath = Paths.get(allegato.getPathFile());

        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        allegatoRepository.deleteById(allegatoId);
        log.info("Allegato cancellato: {}", allegatoId);
    }

    /**
     * Recupera il nome utente corrente dall'autenticazione Spring Security.
     */
    private String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}

