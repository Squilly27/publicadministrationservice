package com.ifm.publicadministrationservice.dto;

import com.ifm.publicadministrationservice.entity.RuoloUtente;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String nomeCompleto;
    private String email;
    private RuoloUtente ruolo;
}

