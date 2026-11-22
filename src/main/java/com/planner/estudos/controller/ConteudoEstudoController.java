package com.planner.estudos.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.estudos.dto.ConteudoEstudoRequestDTO;
import com.planner.estudos.dto.ConteudoEstudoResponseDTO;
import com.planner.estudos.entity.ConteudoEstudo;
import com.planner.estudos.service.ConteudoEstudoService;
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
@RequestMapping("/api/v1/conteudos-estudo")
@Tag(name = "Conteúdos de Estudo", description = "Operações para gerenciamento de tópicos e conteúdos vinculados a matérias")
public class ConteudoEstudoController {

    private final ConteudoEstudoService conteudoEstudoService;
    private final UsuarioContextService usuarioContextService;

    public ConteudoEstudoController(ConteudoEstudoService conteudoEstudoService,
                                  UsuarioContextService usuarioContextService) {
        this.conteudoEstudoService = conteudoEstudoService;
        this.usuarioContextService = usuarioContextService;
    }

    @PostMapping
    @Operation(summary = "Criar conteúdo de estudo", description = "Registra um novo conteúdo vinculado a uma matéria do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conteúdo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados do conteúdo inválidos"),
            @ApiResponse(responseCode = "404", description = "Matéria informada não encontrada")
    })
    public ResponseEntity<ConteudoEstudoResponseDTO> criar(@Valid @RequestBody ConteudoEstudoRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        ConteudoEstudoResponseDTO response = conteudoEstudoService.criar(usuarioId, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar conteúdos por matéria", description = "Lista os conteúdos vinculados a uma matéria específica, com filtro opcional por status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conteúdos listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Matéria informada não encontrada")
    })
    public ResponseEntity<List<ConteudoEstudoResponseDTO>> listarPorMateria(
            @Parameter(description = "Identificador da matéria associada") @RequestParam Long materiaId,
            @Parameter(description = "Filtro opcional por status do conteúdo") @RequestParam(required = false) ConteudoEstudo.Status status) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        List<ConteudoEstudoResponseDTO> response = (status != null)
                ? conteudoEstudoService.listarPorMateriaEStatus(usuarioId, materiaId, status)
                : conteudoEstudoService.listarPorMateria(usuarioId, materiaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conteúdo por ID", description = "Retorna os detalhes de um conteúdo de estudo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conteúdo encontrado"),
            @ApiResponse(responseCode = "404", description = "Conteúdo não encontrado")
    })
    public ResponseEntity<ConteudoEstudoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único do conteúdo") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(conteudoEstudoService.buscarPorId(usuarioId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conteúdo", description = "Atualiza título, descrição, status ou matéria vinculada do conteúdo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conteúdo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Conteúdo ou nova matéria informada não encontrada")
    })
    public ResponseEntity<ConteudoEstudoResponseDTO> atualizar(
            @Parameter(description = "Identificador único do conteúdo") @PathVariable Long id,
            @Valid @RequestBody ConteudoEstudoRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(conteudoEstudoService.atualizar(usuarioId, id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir conteúdo", description = "Remove um conteúdo de estudo do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conteúdo excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conteúdo não encontrado")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador único do conteúdo") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        conteudoEstudoService.excluir(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
