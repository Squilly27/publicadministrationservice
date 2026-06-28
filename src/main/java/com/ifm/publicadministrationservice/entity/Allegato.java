package com.ifm.publicadministrationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "allegato")
@Data
@EqualsAndHashCode(exclude = "richiesta")
@ToString(exclude = "richiesta")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allegato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "richiesta_id", nullable = false)
    private RichiestaAccessoAtti richiesta;

    @Column(name = "nome_file", nullable = false)
    private String nomeFile;

    @Column(name = "tipo_file", nullable = false)
    private String tipoFile;

    @Column(name = "dimensione", nullable = false)
    private Long dimensione;

    @Column(name = "path_file", nullable = false)
    private String pathFile;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload;

    @Column(name = "caricato_da", nullable = false)
    private String caricatoDa;

    @PrePersist
    protected void onCreate() {
        dataUpload = LocalDateTime.now();
    }
}

