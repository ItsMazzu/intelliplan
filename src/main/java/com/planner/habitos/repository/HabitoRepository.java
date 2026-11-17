package com.planner.habitos.repository;

import com.planner.habitos.entity.Habito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HabitoRepository extends JpaRepository<Habito, Long> {

    List<Habito> findByUsuarioId(Long usuarioId);

    List<Habito> findByUsuarioIdAndAtivo(Long usuarioId, boolean ativo);
}
