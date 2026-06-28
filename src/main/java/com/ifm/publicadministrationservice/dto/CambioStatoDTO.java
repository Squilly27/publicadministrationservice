package com.ifm.publicadministrationservice.dto;

import com.ifm.publicadministrationservice.entity.StatoRichiesta;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioStatoDTO {

    @NotNull(message = "Stato nuovo è obbligatorio")
    private StatoRichiesta statoNuovo;

    private String nota;
}

