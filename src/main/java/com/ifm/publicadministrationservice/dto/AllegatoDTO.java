package com.ifm.publicadministrationservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllegatoDTO {

    private Long id;
    private String nomeFile;
    private String tipoFile;
    private Long dimensione;
    private LocalDateTime dataUpload;
    private String caricatoDa;
}

