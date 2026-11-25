package com.planner.integracao.feriados.dto;

public record FeriadoIncidenciaDTO(
        FeriadoCategoria categoria,
        String uf,
        String codigoIbgeMunicipio,
        String fonte,
        String referenciaLegal
) {
}
