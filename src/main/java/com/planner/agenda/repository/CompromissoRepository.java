package com.planner.agenda.repository;

import com.planner.agenda.entity.Compromisso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CompromissoRepository extends JpaRepository<Compromisso, Long> {

    List<Compromisso> findByUsuarioId(Long usuarioId);

    List<Compromisso> findByUsuarioIdAndDataHoraInicioBetween(Long usuarioId, LocalDateTime inicio, LocalDateTime fim);
}
