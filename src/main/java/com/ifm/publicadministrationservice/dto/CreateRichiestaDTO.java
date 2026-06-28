package com.ifm.publicadministrationservice.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRichiestaDTO {

    @NotBlank(message = "Nome richiedente è obbligatorio")
    private String nomeRichiedente;

    @NotBlank(message = "Cognome richiedente è obbligatorio")
    private String cognomeRichiedente;

    @NotBlank(message = "Email è obbligatoria")
    @Email(message = "Email deve essere valida")
    private String emailRichiedente;

    private String telefonoRichiedente;

    @NotBlank(message = "Oggetto è obbligatorio")
    private String oggetto;

    @NotBlank(message = "Descrizione è obbligatoria")
    private String descrizione;
}

