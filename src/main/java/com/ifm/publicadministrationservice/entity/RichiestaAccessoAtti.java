package com.ifm.publicadministrationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "richiesta_accesso_atti")
@Data
@EqualsAndHashCode(exclude = {"allegati", "storico"})
@ToString(exclude = {"allegati", "storico"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RichiestaAccessoAtti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_protocollo", unique = true, nullable = false)
    private String numeroProtocollo;

    @Column(name = "stato", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatoRichiesta stato;

    @Column(name = "nome_richiedente", nullable = false)
    private String nomeRichiedente;

    @Column(name = "cognome_richiedente", nullable = false)
    private String cognomeRichiedente;

    @Column(name = "email_richiedente", nullable = false)
    private String emailRichiedente;

    @Column(name = "telefono_richiedente")
    private String telefonoRichiedente;

    @Column(name = "oggetto", nullable = false)
    private String oggetto;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "data_presentazione", nullable = false)
    private LocalDateTime dataPresentazione;

    @Column(name = "data_modifica")
    private LocalDateTime dataModifica;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "creato_da", nullable = false)
    private String creatoDa;

    @Column(name = "modificato_da")
    private String modificatoDa;

    @Builder.Default
    @OneToMany(mappedBy = "richiesta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Allegato> allegati = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "richiesta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StoricoStato> storico = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        dataPresentazione = LocalDateTime.now();
        dataModifica = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataModifica = LocalDateTime.now();
    }
}

