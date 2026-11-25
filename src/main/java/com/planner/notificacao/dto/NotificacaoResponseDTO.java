package com.planner.notificacao.dto;

import com.planner.notificacao.entity.Notificacao;

import java.time.LocalDateTime;

public record NotificacaoResponseDTO(
        Long id,
        String titulo,
        String descricao,
        Notificacao.Tipo tipo,
        LocalDateTime criadaEm,
        LocalDateTime lidaEm,
        String urlDestino
) {
}
