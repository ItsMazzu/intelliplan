package com.planner.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaDTO(
        @NotBlank String senhaAtual,
        @NotBlank @Size(min = 8, max = 100) String novaSenha,
        @NotBlank @Size(min = 8, max = 100) String confirmarNovaSenha
) {}
