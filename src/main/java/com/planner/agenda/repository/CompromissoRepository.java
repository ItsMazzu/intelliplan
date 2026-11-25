package com.planner.agenda.repository;

import com.planner.agenda.entity.Compromisso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CompromissoRepository extends JpaRepository<Compromisso, Long> {

    List<Compromisso> findByUsuarioId(Long usuarioId);

    @Query("""
            select compromisso
            from Compromisso compromisso
            where compromisso.usuario.id = :usuarioId
              and compromisso.dataHoraInicio between :inicio and :fim
            order by compromisso.dataHoraInicio
            """)
    List<Compromisso> findUpcomingByUsuarioId(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
            select compromisso
            from Compromisso compromisso
            where compromisso.usuario.id = :usuarioId
              and compromisso.dataHoraInicio <= :fim
              and (compromisso.dataHoraFim is null or compromisso.dataHoraFim >= :inicio)
            """)
    List<Compromisso> findByUsuarioIdAndDataHoraInicioBetween(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
