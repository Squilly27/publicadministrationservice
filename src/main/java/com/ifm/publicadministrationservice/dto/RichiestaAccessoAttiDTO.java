package com.ifm.publicadministrationservice.dto;

import com.ifm.publicadministrationservice.entity.StatoRichiesta;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RichiestaAccessoAttiDTO {

    private Long id;
    private String numeroProtocollo;
    private StatoRichiesta stato;
    private String nomeRichiedente;
    private String cognomeRichiedente;
    private String emailRichiedente;
    private String telefonoRichiedente;
    private String oggetto;
    private String descrizione;
    private LocalDateTime dataPresentazione;
    private LocalDateTime dataModifica;
    private String note;
    private String creatoDa;
    private String modificatoDa;
    private List<AllegatoDTO> allegati;
    private List<StoricoStatoDTO> storico;
}

