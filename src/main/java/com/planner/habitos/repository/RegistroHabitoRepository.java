package com.planner.habitos.repository;

import com.planner.habitos.entity.RegistroHabito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RegistroHabitoRepository extends JpaRepository<RegistroHabito, Long> {

    List<RegistroHabito> findByHabitoId(Long habitoId);

    List<RegistroHabito> findByHabitoIdAndDataRegistroBetween(Long habitoId, LocalDate inicio, LocalDate fim);

    boolean existsByHabitoIdAndDataRegistro(Long habitoId, LocalDate dataRegistro);
}
