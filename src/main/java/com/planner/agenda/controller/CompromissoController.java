package com.planner.agenda.controller;

import com.planner.agenda.dto.CompromissoRequestDTO;
import com.planner.agenda.dto.CompromissoResponseDTO;
import com.planner.agenda.service.CompromissoService;
import com.planner.common.context.UsuarioContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/compromissos")
@Tag(name = "Compromissos", description = "Operações para gerenciamento de compromissos e agenda do usuário")
public class CompromissoController {

    private final CompromissoService compromissoService;
    private final UsuarioContextService usuarioContextService;

    public CompromissoController(CompromissoService compromissoService,
                                 UsuarioContextService usuarioContextService) {
        this.compromissoService = compromissoService;
        this.usuarioContextService = usuarioContextService;
    }

    @PostMapping
    @Operation(summary = "Criar compromisso", description = "Registra um novo evento ou compromisso na agenda do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compromisso criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou horário de término anterior ao início")
    })
    public ResponseEntity<CompromissoResponseDTO> criar(@Valid @RequestBody CompromissoRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        CompromissoResponseDTO response = compromissoService.criar(usuarioId, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar compromissos", description = "Lista os compromissos do usuário, com opção de filtro por período")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compromissos listados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de período inválidos")
    })
    public ResponseEntity<List<CompromissoResponseDTO>> listar(
            @Parameter(description = "Data e hora inicial do período")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data e hora final do período")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        List<CompromissoResponseDTO> response = (inicio != null || fim != null)
                ? compromissoService.listarPorPeriodo(usuarioId, inicio, fim)
                : compromissoService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar compromisso por ID", description = "Retorna os detalhes de um compromisso específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compromisso encontrado"),
            @ApiResponse(responseCode = "404", description = "Compromisso não encontrado")
    })
    public ResponseEntity<CompromissoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único do compromisso") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(compromissoService.buscarPorId(usuarioId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar compromisso", description = "Atualiza os dados de um compromisso existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compromisso atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou horário incoerente"),
            @ApiResponse(responseCode = "404", description = "Compromisso não encontrado")
    })
    public ResponseEntity<CompromissoResponseDTO> atualizar(
            @Parameter(description = "Identificador único do compromisso") @PathVariable Long id,
            @Valid @RequestBody CompromissoRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(compromissoService.atualizar(usuarioId, id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir compromisso", description = "Remove um compromisso da agenda do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Compromisso excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Compromisso não encontrado")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador único do compromisso") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        compromissoService.excluir(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
