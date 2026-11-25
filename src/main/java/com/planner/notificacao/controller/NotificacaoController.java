package com.planner.notificacao.controller;

import com.planner.notificacao.dto.NotificacaoListaResponseDTO;
import com.planner.notificacao.dto.NotificacaoResponseDTO;
import com.planner.notificacao.service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public ResponseEntity<NotificacaoListaResponseDTO> listar() {
        return ResponseEntity.ok(notificacaoService.listarDoUsuarioAtual());
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<NotificacaoListaResponseDTO> sincronizar() {
        return ResponseEntity.ok(notificacaoService.sincronizarEListarDoUsuarioAtual());
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<NotificacaoResponseDTO> marcarComoLida(@PathVariable Long id) {
        return ResponseEntity.ok(notificacaoService.marcarComoLida(id));
    }

    @PatchMapping("/lidas")
    public ResponseEntity<Void> marcarTodasComoLidas() {
        notificacaoService.marcarTodasComoLidas();
        return ResponseEntity.noContent().build();
    }
}
