package com.planner.config.setup;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DatabaseConfigDTO(
        @NotBlank @Size(max = 255) @Pattern(regexp = "^[A-Za-z0-9.-]+$") String host,
        @Min(1) @Max(65535) int porta,
        @NotBlank @Size(max = 63) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String banco,
        @NotBlank @Size(max = 100) String usuario,
        @Size(max = 200) String senha
) {}
