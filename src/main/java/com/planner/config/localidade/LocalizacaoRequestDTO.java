package com.planner.config.localidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LocalizacaoRequestDTO(
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String uf,
        @NotBlank @Size(max = 200) String municipio,
        @NotBlank @Pattern(regexp = "\\d{7}") String codigoIbge
) {
}
