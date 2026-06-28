package com.ifm.publicadministrationservice.dto;

import com.ifm.publicadministrationservice.entity.RuoloUtente;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUtenteDTO {

    @NotBlank(message = "Username è obbligatorio")
    private String username;

    @NotBlank(message = "Password è obbligatoria")
    private String password;

    @NotBlank(message = "Nome completo è obbligatorio")
    private String nomeCompleto;

    @NotBlank(message = "Email è obbligatoria")
    @Email(message = "Email deve essere valida")
    private String email;

    @NotNull(message = "Ruolo è obbligatorio")
    private RuoloUtente ruolo;
}

