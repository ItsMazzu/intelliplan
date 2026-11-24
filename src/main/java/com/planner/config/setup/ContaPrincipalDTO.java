package com.planner.config.setup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContaPrincipalDTO(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(min = 8, max = 100) String senha,
        @NotBlank @Size(min = 8, max = 100) String confirmarSenha
) {}
