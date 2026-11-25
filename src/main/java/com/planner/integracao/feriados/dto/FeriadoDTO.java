package com.planner.integracao.feriados.dto;

import java.time.LocalDate;
import java.util.List;

public record FeriadoDTO(
        LocalDate date,
        String name,
        List<FeriadoIncidenciaDTO> incidencias
) {
    public FeriadoDTO {
        incidencias = incidencias == null ? List.of() : List.copyOf(incidencias);
    }

    public String descricaoCategorias() {
        return incidencias.stream()
                .map(incidencia -> incidencia.categoria().descricao())
                .distinct()
                .reduce((primeira, segunda) -> primeira + ", " + segunda)
                .orElse("");
    }

    public String categoriasCss() {
        return incidencias.stream()
                .map(incidencia -> incidencia.categoria().name().toLowerCase(java.util.Locale.ROOT))
                .distinct()
                .reduce((primeira, segunda) -> primeira + " " + segunda)
                .orElse("");
    }

    public boolean somentePontoFacultativo() {
        return !incidencias.isEmpty()
                && incidencias.stream().allMatch(incidencia ->
                incidencia.categoria() == FeriadoCategoria.PONTO_FACULTATIVO);
    }

    public String rotuloCalendario() {
        return somentePontoFacultativo() ? "Ponto facultativo" : "Feriado";
    }

    public String classeCalendario() {
        return somentePontoFacultativo() ? "is-facultative" : "is-holiday";
    }
}
