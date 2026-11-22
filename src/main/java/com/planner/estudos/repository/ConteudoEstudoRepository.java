package com.planner.estudos.repository;

import com.planner.estudos.entity.ConteudoEstudo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConteudoEstudoRepository extends JpaRepository<ConteudoEstudo, Long> {

    List<ConteudoEstudo> findByMateriaId(Long materiaId);

    List<ConteudoEstudo> findByMateriaIdIn(List<Long> materiaIds);

    List<ConteudoEstudo> findByMateriaIdAndStatus(Long materiaId, ConteudoEstudo.Status status);
}
