package com.ifm.publicadministrationservice.dto;

import com.ifm.publicadministrationservice.entity.StatoRichiesta;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoricoStatoDTO {

    private Long id;
    private StatoRichiesta statoPrecedente;
    private StatoRichiesta statoNuovo;
    private LocalDateTime dataCambio;
    private String nota;
    private String utenteCambio;
}

