package com.planner.projeto.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.projeto.dto.ProjetoRequestDTO;
import com.planner.projeto.dto.ProjetoResponseDTO;
import com.planner.projeto.entity.Projeto;
import com.planner.projeto.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/projetos")
@Tag(name = "Projetos", description = "Operações para gerenciamento de projetos do usuário")
public class ProjetoController {

    private final ProjetoService projetoService;
    private final UsuarioContextService usuarioContextService;

    public ProjetoController(ProjetoService projetoService, UsuarioContextService usuarioContextService) {
        this.projetoService = projetoService;
        this.usuarioContextService = usuarioContextService;
    }

    @PostMapping
    @Operation(summary = "Criar projeto", description = "Registra um novo projeto para o usuário atual")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados do projeto inválidos ou cronograma incoerente")
    })
    public ResponseEntity<ProjetoResponseDTO> criar(@Valid @RequestBody ProjetoRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        ProjetoResponseDTO response = projetoService.criar(usuarioId, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar projetos", description = "Lista os projetos do usuário atual, com opção de filtro por status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projetos listados com sucesso")
    })
    public ResponseEntity<List<ProjetoResponseDTO>> listar(
            @Parameter(description = "Filtro opcional por status do projeto") @RequestParam(required = false) Projeto.Status status) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        List<ProjetoResponseDTO> response = (status != null)
                ? projetoService.listarPorStatus(usuarioId, status)
                : projetoService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por ID", description = "Retorna os detalhes de um projeto específico do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto encontrado"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<ProjetoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único do projeto") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(projetoService.buscarPorId(usuarioId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar projeto", description = "Atualiza os dados de um projeto existente do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<ProjetoResponseDTO> atualizar(
            @Parameter(description = "Identificador único do projeto") @PathVariable Long id,
            @Valid @RequestBody ProjetoRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(projetoService.atualizar(usuarioId, id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir projeto", description = "Remove um projeto do usuário, desde que não possua tarefas vinculadas")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projeto excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Projeto possui tarefas vinculadas e não pode ser excluído"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador único do projeto") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        projetoService.excluir(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
