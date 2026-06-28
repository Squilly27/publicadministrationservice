package com.ifm.publicadministrationservice.repository;

import com.ifm.publicadministrationservice.entity.RichiestaAccessoAtti;
import com.ifm.publicadministrationservice.entity.StatoRichiesta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RichiestaAccessoAttiRepository extends JpaRepository<RichiestaAccessoAtti, Long> {
    Optional<RichiestaAccessoAtti> findByNumeroProtocollo(String numeroProtocollo);

    Page<RichiestaAccessoAtti> findByStato(StatoRichiesta stato, Pageable pageable);

    Page<RichiestaAccessoAtti> findByCognomeRichiedente(String cognomeRichiedente, Pageable pageable);

    Page<RichiestaAccessoAtti> findByNumeroProtocolloContaining(String numeroProtocollo, Pageable pageable);

    Page<RichiestaAccessoAtti> findByDataPresentazioneBetween(LocalDateTime inizio, LocalDateTime fine, Pageable pageable);
}

