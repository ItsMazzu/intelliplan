package com.planner.config.localidade;

public record LocalizacaoInstalacaoDTO(
        String uf,
        String municipio,
        String codigoIbge
) {
    public boolean configurada() {
        return uf != null && !uf.isBlank()
                && municipio != null && !municipio.isBlank()
                && codigoIbge != null && !codigoIbge.isBlank();
    }

    public static LocalizacaoInstalacaoDTO vazia() {
        return new LocalizacaoInstalacaoDTO("", "", "");
    }
}
