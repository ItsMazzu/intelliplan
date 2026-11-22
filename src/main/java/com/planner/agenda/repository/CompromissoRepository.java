package com.planner.agenda.repository;

import com.planner.agenda.entity.Compromisso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CompromissoRepository extends JpaRepository<Compromisso, Long> {

    List<Compromisso> findByUsuarioId(Long usuarioId);

    List<Compromisso> findByUsuarioIdAndDataHoraInicioBetween(Long usuarioId, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
        SELECT c FROM Compromisso c
        WHERE c.usuario.id = :usuarioId
          AND c.dataHoraInicio <= :fim
          AND (c.dataHoraFim IS NULL OR c.dataHoraFim >= :inicio)
    """)
    List<Compromisso> findCompromissosNoPeriodo(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}
