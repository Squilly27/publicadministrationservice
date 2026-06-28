package com.ifm.publicadministrationservice.service;

import com.ifm.publicadministrationservice.dto.CambioStatoDTO;
import com.ifm.publicadministrationservice.dto.CreateRichiestaDTO;
import com.ifm.publicadministrationservice.dto.RichiestaAccessoAttiDTO;
import com.ifm.publicadministrationservice.dto.AllegatoDTO;
import com.ifm.publicadministrationservice.dto.StoricoStatoDTO;
import com.ifm.publicadministrationservice.entity.RichiestaAccessoAtti;
import com.ifm.publicadministrationservice.entity.StatoRichiesta;
import com.ifm.publicadministrationservice.entity.StoricoStato;
import com.ifm.publicadministrationservice.repository.RichiestaAccessoAttiRepository;
import com.ifm.publicadministrationservice.repository.StoricoStatoRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RichiestaAccessoAttiService {

    @Autowired
    private RichiestaAccessoAttiRepository richiestaRepository;

    @Autowired
    private StoricoStatoRepository storicoRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final java.util.Map<StatoRichiesta, java.util.List<StatoRichiesta>> TRANSIZIONI_VALIDE =
            java.util.Map.ofEntries(
                java.util.Map.entry(StatoRichiesta.PRESENTATA,
                        java.util.List.of(StatoRichiesta.IN_ISTRUTTORIA)),
                java.util.Map.entry(StatoRichiesta.IN_ISTRUTTORIA,
                        java.util.List.of(StatoRichiesta.RICHIESTA_INTEGRAZIONE, StatoRichiesta.ACCOLTA, StatoRichiesta.RESPINTA)),
                java.util.Map.entry(StatoRichiesta.RICHIESTA_INTEGRAZIONE,
                        java.util.List.of(StatoRichiesta.IN_ISTRUTTORIA, StatoRichiesta.RESPINTA)),
                java.util.Map.entry(StatoRichiesta.ACCOLTA,
                        java.util.List.of(StatoRichiesta.CHIUSA)),
                java.util.Map.entry(StatoRichiesta.RESPINTA,
                        java.util.List.of(StatoRichiesta.CHIUSA))
            );

    @Transactional
    public RichiestaAccessoAttiDTO createRichiesta(CreateRichiestaDTO dto) {
        String numeroProtocollo = generaNumeroProtocollo();
        String username = getUsername();

        RichiestaAccessoAtti richiesta = RichiestaAccessoAtti.builder()
                .numeroProtocollo(numeroProtocollo)
                .stato(StatoRichiesta.PRESENTATA)
                .nomeRichiedente(dto.getNomeRichiedente())
                .cognomeRichiedente(dto.getCognomeRichiedente())
                .emailRichiedente(dto.getEmailRichiedente())
                .telefonoRichiedente(dto.getTelefonoRichiedente())
                .oggetto(dto.getOggetto())
                .descrizione(dto.getDescrizione())
                .creatoDa(username)
                .build();

        richiesta = richiestaRepository.save(richiesta);

        // Crea lo storico dello stato iniziale
        StoricoStato storico = StoricoStato.builder()
                .richiesta(richiesta)
                .statoPrecedente(null)
                .statoNuovo(StatoRichiesta.PRESENTATA)
                .nota("Pratica presentata")
                .utenteCambio(username)
                .build();

        storicoRepository.save(storico);

        log.info("Richiesta creata con numero protocollo: {}", numeroProtocollo);
        return toDto(richiesta);
    }

    @Transactional
    public RichiestaAccessoAttiDTO updateRichiesta(Long id, CreateRichiestaDTO dto) {
        RichiestaAccessoAtti richiesta = richiestaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Richiesta non trovata"));

        String username = getUsername();

        richiesta.setNomeRichiedente(dto.getNomeRichiedente());
        richiesta.setCognomeRichiedente(dto.getCognomeRichiedente());
        richiesta.setEmailRichiedente(dto.getEmailRichiedente());
        richiesta.setTelefonoRichiedente(dto.getTelefonoRichiedente());
        richiesta.setOggetto(dto.getOggetto());
        richiesta.setDescrizione(dto.getDescrizione());
        richiesta.setModificatoDa(username);

        richiesta = richiestaRepository.save(richiesta);
        log.info("Richiesta aggiornata: {}", id);
        return toDto(richiesta);
    }

    @Transactional(readOnly = true)
    public RichiestaAccessoAttiDTO getRichiestaById(Long id) {
        RichiestaAccessoAtti richiesta = richiestaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Richiesta non trovata"));
        return toDto(richiesta);
    }

    @Transactional(readOnly = true)
    public Page<RichiestaAccessoAttiDTO> getAllRichieste(Pageable pageable) {
        Page<RichiestaAccessoAtti> page = richiestaRepository.findAll(pageable);
        List<RichiestaAccessoAttiDTO> dtos = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<RichiestaAccessoAttiDTO> filterRichieste(
            Optional<StatoRichiesta> stato,
            Optional<String> cognomeRichiedente,
            Optional<String> numeroProtocollo,
            Pageable pageable) {

        Page<RichiestaAccessoAtti> page;

        if (stato.isPresent()) {
            page = richiestaRepository.findByStato(stato.get(), pageable);
        } else if (cognomeRichiedente.isPresent()) {
            page = richiestaRepository.findByCognomeRichiedente(cognomeRichiedente.get(), pageable);
        } else if (numeroProtocollo.isPresent()) {
            page = richiestaRepository.findByNumeroProtocolloContaining(numeroProtocollo.get(), pageable);
        } else {
            page = richiestaRepository.findAll(pageable);
        }

        List<RichiestaAccessoAttiDTO> dtos = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Transactional
    public RichiestaAccessoAttiDTO cambiaStato(Long id, CambioStatoDTO dto) {
        RichiestaAccessoAtti richiesta = richiestaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Richiesta non trovata"));

        StatoRichiesta statoNuovo = dto.getStatoNuovo();
        StatoRichiesta statoPrecedente = richiesta.getStato();

        // Valida la transizione di stato
        if (!isTransizioneValida(statoPrecedente, statoNuovo)) {
            throw new RuntimeException("Transizione di stato non consentita da " + statoPrecedente + " a " + statoNuovo);
        }

        String username = getUsername();

        // Aggiorna lo stato
        richiesta.setStato(statoNuovo);
        richiesta.setModificatoDa(username);
        richiesta = richiestaRepository.save(richiesta);

        // Registra il cambio di stato nello storico
        StoricoStato storico = StoricoStato.builder()
                .richiesta(richiesta)
                .statoPrecedente(statoPrecedente)
                .statoNuovo(statoNuovo)
                .nota(dto.getNota())
                .utenteCambio(username)
                .build();

        storicoRepository.save(storico);

        log.info("Stato della richiesta {} cambiato da {} a {}", id, statoPrecedente, statoNuovo);
        return toDto(richiesta);
    }

    private RichiestaAccessoAttiDTO toDto(RichiestaAccessoAtti richiesta) {
        RichiestaAccessoAttiDTO dto = RichiestaAccessoAttiDTO.builder()
                .id(richiesta.getId())
                .numeroProtocollo(richiesta.getNumeroProtocollo())
                .stato(richiesta.getStato())
                .nomeRichiedente(richiesta.getNomeRichiedente())
                .cognomeRichiedente(richiesta.getCognomeRichiedente())
                .emailRichiedente(richiesta.getEmailRichiedente())
                .telefonoRichiedente(richiesta.getTelefonoRichiedente())
                .oggetto(richiesta.getOggetto())
                .descrizione(richiesta.getDescrizione())
                .dataPresentazione(richiesta.getDataPresentazione())
                .dataModifica(richiesta.getDataModifica())
                .note(richiesta.getNote())
                .creatoDa(richiesta.getCreatoDa())
                .modificatoDa(richiesta.getModificatoDa())
                .build();

        dto.setAllegati(Optional.ofNullable(richiesta.getAllegati()).orElse(Collections.emptySet()).stream()
                .map(allegato -> modelMapper.map(allegato, AllegatoDTO.class))
                .collect(Collectors.toList()));

        dto.setStorico(Optional.ofNullable(richiesta.getStorico()).orElse(Collections.emptySet()).stream()
                .map(storico -> modelMapper.map(storico, StoricoStatoDTO.class))
                .collect(Collectors.toList()));

        return dto;
    }

    private boolean isTransizioneValida(StatoRichiesta da, StatoRichiesta a) {
        return TRANSIZIONI_VALIDE.getOrDefault(da, java.util.List.of()).contains(a);
    }

    private String generaNumeroProtocollo() {
        return "PROT-" + System.currentTimeMillis();
    }

    private String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}


