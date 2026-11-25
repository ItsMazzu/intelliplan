package com.planner.projeto.dto;

import com.planner.projeto.entity.Projeto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ProjetoRequestDTO(
        @NotBlank @Size(max = 200) String nome,
        String descricao,
        @NotNull Projeto.Status status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataConclusao
) {}
