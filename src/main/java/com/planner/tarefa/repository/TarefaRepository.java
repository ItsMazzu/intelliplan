package com.planner.tarefa.repository;

import com.planner.tarefa.entity.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findByUsuarioId(Long usuarioId);

    List<Tarefa> findByUsuarioIdAndStatus(Long usuarioId, Tarefa.Status status);

    List<Tarefa> findByProjetoId(Long projetoId);

    List<Tarefa> findByUsuarioIdAndDataVencimentoBetween(Long usuarioId, LocalDate inicio, LocalDate fim);
}
