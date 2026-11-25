package com.planner.dashboard.dto;

public record DiaCalendarioDTO(
        int numero,
        boolean hoje,
        boolean temEvento,
        boolean temFeriado,
        boolean temPontoFacultativo,
        String nomeFeriado,
        String categoriasFeriado,
        String classesCategoriaFeriado
) {
}
