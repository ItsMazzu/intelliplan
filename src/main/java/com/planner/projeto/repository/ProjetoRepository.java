package com.planner.projeto.repository;

import com.planner.projeto.entity.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    List<Projeto> findByUsuarioId(Long usuarioId);

    List<Projeto> findByUsuarioIdAndStatus(Long usuarioId, Projeto.Status status);
}
