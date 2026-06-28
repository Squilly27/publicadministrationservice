package com.ifm.publicadministrationservice.repository;

import com.ifm.publicadministrationservice.entity.StoricoStato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoricoStatoRepository extends JpaRepository<StoricoStato, Long> {
    List<StoricoStato> findByRichiestaIdOrderByDataCambioDesc(Long richiestaId);
}

