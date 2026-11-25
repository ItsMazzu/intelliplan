package com.planner.notificacao.dto;

import java.util.List;

public record NotificacaoListaResponseDTO(
        List<NotificacaoResponseDTO> notificacoes,
        long naoLidas
) {
}
