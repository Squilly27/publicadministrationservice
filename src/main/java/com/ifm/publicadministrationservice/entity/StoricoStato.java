package com.ifm.publicadministrationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "storico_stato")
@Data
@EqualsAndHashCode(exclude = "richiesta")
@ToString(exclude = "richiesta")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoricoStato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "richiesta_id", nullable = false)
    private RichiestaAccessoAtti richiesta;

    @Column(name = "stato_precedente")
    @Enumerated(EnumType.STRING)
    private StatoRichiesta statoPrecedente;

    @Column(name = "stato_nuovo", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatoRichiesta statoNuovo;

    @Column(name = "data_cambio", nullable = false)
    private LocalDateTime dataCambio;

    @Column(name = "nota", columnDefinition = "TEXT")
    private String nota;

    @Column(name = "utente_cambio", nullable = false)
    private String utenteCambio;

    @PrePersist
    protected void onCreate() {
        dataCambio = LocalDateTime.now();
    }
}

