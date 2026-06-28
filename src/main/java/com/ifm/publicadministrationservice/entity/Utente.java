package com.ifm.publicadministrationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "utente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "ruolo", nullable = false)
    @Enumerated(EnumType.STRING)
    private RuoloUtente ruolo;

    @Column(name = "attivo", nullable = false)
    private Boolean attivo;

    @Column(name = "data_creazione", nullable = false)
    private LocalDateTime dataCreazione;

    @Column(name = "data_modifica")
    private LocalDateTime dataModifica;

    @PrePersist
    protected void onCreate() {
        dataCreazione = LocalDateTime.now();
        attivo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        dataModifica = LocalDateTime.now();
    }
}

