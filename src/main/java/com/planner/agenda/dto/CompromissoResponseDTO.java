package com.planner.agenda.dto;

import java.time.LocalDateTime;

public record CompromissoResponseDTO(
        Long id,
        String titulo,
        String descricao,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,
        String local,
        LocalDateTime criadoEm
) {}
