package com.ifm.publicadministrationservice.repository;

import com.ifm.publicadministrationservice.entity.Allegato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllegatoRepository extends JpaRepository<Allegato, Long> {
    List<Allegato> findByRichiestaId(Long richiestaId);
}

