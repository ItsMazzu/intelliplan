package com.planner.tarefa.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.tarefa.dto.TarefaRequestDTO;
import com.planner.tarefa.dto.TarefaResponseDTO;
import com.planner.tarefa.entity.Tarefa;
import com.planner.tarefa.service.TarefaService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tarefas")
@Tag(name = "Tarefas", description = "Operações para gerenciamento de tarefas do usuário")
public class TarefaController {

    private final TarefaService tarefaService;
    private final UsuarioContextService usuarioContextService;

    public TarefaController(TarefaService tarefaService, UsuarioContextService usuarioContextService) {
        this.tarefaService = tarefaService;
        this.usuarioContextService = usuarioContextService;
    }

    @PostMapping
    @Operation(summary = "Criar tarefa", description = "Registra uma nova tarefa para o usuário, com associação opcional a um projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da tarefa inválidos"),
            @ApiResponse(responseCode = "404", description = "Projeto informado não encontrado")
    })
    public ResponseEntity<TarefaResponseDTO> criar(@Valid @RequestBody TarefaRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        TarefaResponseDTO response = tarefaService.criar(usuarioId, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar tarefas", description = "Lista as tarefas do usuário com filtros opcionais combináveis por status, projeto ou período de vencimento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefas listadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de período inválidos"),
            @ApiResponse(responseCode = "404", description = "Projeto informado não encontrado")
    })
    public ResponseEntity<List<TarefaResponseDTO>> listar(
            @Parameter(description = "Filtro opcional por status") @RequestParam(required = false) Tarefa.Status status,
            @Parameter(description = "Filtro opcional por projeto") @RequestParam(required = false) Long projetoId,
            @Parameter(description = "Data inicial para busca por vencimento")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final para busca por vencimento")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(tarefaService.listar(usuarioId, status, projetoId, inicio, fim));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tarefa por ID", description = "Retorna os detalhes de uma tarefa específica do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<TarefaResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único da tarefa") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(tarefaService.buscarPorId(usuarioId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tarefa", description = "Atualiza os dados de uma tarefa existente do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da tarefa inválidos"),
            @ApiResponse(responseCode = "404", description = "Tarefa ou projeto informado não encontrado")
    })
    public ResponseEntity<TarefaResponseDTO> atualizar(
            @Parameter(description = "Identificador único da tarefa") @PathVariable Long id,
            @Valid @RequestBody TarefaRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(tarefaService.atualizar(usuarioId, id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir tarefa", description = "Remove uma tarefa do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador único da tarefa") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        tarefaService.excluir(usuarioId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/concluir")
    @Operation(summary = "Alternar conclusão da tarefa", description = "Alterna o status da tarefa entre concluída e pendente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status da tarefa alternado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<TarefaResponseDTO> alternarConclusao(
            @Parameter(description = "Identificador único da tarefa") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(tarefaService.alternarConclusao(usuarioId, id));
    }
}
