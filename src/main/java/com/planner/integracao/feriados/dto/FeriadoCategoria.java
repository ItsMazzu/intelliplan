package com.planner.integracao.feriados.dto;

public enum FeriadoCategoria {
    NACIONAL("Nacional"),
    ESTADUAL("Estadual"),
    MUNICIPAL("Municipal"),
    PONTO_FACULTATIVO("Ponto facultativo");

    private final String descricao;

    FeriadoCategoria(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
