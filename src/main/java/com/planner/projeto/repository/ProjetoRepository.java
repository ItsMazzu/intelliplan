package com.planner.projeto.repository;

import com.planner.projeto.entity.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    List<Projeto> findByUsuarioId(Long usuarioId);

    List<Projeto> findByUsuarioIdAndStatus(Long usuarioId, Projeto.Status status);

    List<Projeto> findByUsuarioIdAndStatusInAndDataConclusaoBetween(
            Long usuarioId,
            Collection<Projeto.Status> status,
            LocalDate inicio,
            LocalDate fim
    );
}
