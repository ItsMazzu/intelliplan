package com.planner.habitos.repository;

import com.planner.habitos.entity.RegistroHabito;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RegistroHabitoRepository extends JpaRepository<RegistroHabito, Long> {

    List<RegistroHabito> findByHabitoId(Long habitoId);

    List<RegistroHabito> findByHabitoIdAndDataRegistroBetween(Long habitoId, LocalDate inicio, LocalDate fim);

    boolean existsByHabitoIdAndDataRegistro(Long habitoId, LocalDate dataRegistro);

    @Query("""
            select new com.planner.habitos.repository.RegistroHabitoDashboardProjection(
                registro.habito.id, registro.dataRegistro
            )
            from RegistroHabito registro
            where registro.habito.id in :habitoIds
              and registro.dataRegistro between :inicio and :fim
            """)
    List<RegistroHabitoDashboardProjection> findDashboardRegistros(
            @Param("habitoIds") List<Long> habitoIds,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}
