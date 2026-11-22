package com.planner.estudos.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.estudos.dto.MateriaRequestDTO;
import com.planner.estudos.dto.MateriaResponseDTO;
import com.planner.estudos.service.MateriaService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/materias")
@Tag(name = "Matérias", description = "Operações para gerenciamento de disciplinas e matérias de estudo")
public class MateriaController {

    private final MateriaService materiaService;
    private final UsuarioContextService usuarioContextService;

    public MateriaController(MateriaService materiaService, UsuarioContextService usuarioContextService) {
        this.materiaService = materiaService;
        this.usuarioContextService = usuarioContextService;
    }

    @PostMapping
    @Operation(summary = "Criar matéria", description = "Registra uma nova matéria de estudos para o usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Matéria criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da matéria inválidos")
    })
    public ResponseEntity<MateriaResponseDTO> criar(@Valid @RequestBody MateriaRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        MateriaResponseDTO response = materiaService.criar(usuarioId, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar matérias", description = "Lista todas as matérias cadastradas pelo usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matérias listadas com sucesso")
    })
    public ResponseEntity<List<MateriaResponseDTO>> listar() {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(materiaService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar matéria por ID", description = "Retorna os detalhes de uma matéria específica do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matéria encontrada"),
            @ApiResponse(responseCode = "404", description = "Matéria não encontrada")
    })
    public ResponseEntity<MateriaResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único da matéria") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(materiaService.buscarPorId(usuarioId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar matéria", description = "Atualiza o nome e descrição de uma matéria existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matéria atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Matéria não encontrada")
    })
    public ResponseEntity<MateriaResponseDTO> atualizar(
            @Parameter(description = "Identificador único da matéria") @PathVariable Long id,
            @Valid @RequestBody MateriaRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(materiaService.atualizar(usuarioId, id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir matéria", description = "Remove uma matéria do usuário, desde que não possua conteúdos vinculados")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Matéria excluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Matéria possui conteúdos vinculados e não pode ser excluída"),
            @ApiResponse(responseCode = "404", description = "Matéria não encontrada")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador único da matéria") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        materiaService.excluir(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
