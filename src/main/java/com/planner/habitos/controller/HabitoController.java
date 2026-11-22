package com.planner.habitos.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.habitos.dto.HabitoRequestDTO;
import com.planner.habitos.dto.HabitoResponseDTO;
import com.planner.habitos.service.HabitoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/habitos")
@Tag(name = "Hábitos", description = "Operações para gerenciamento e ativação de hábitos do usuário")
public class HabitoController {

    private final HabitoService habitoService;
    private final UsuarioContextService usuarioContextService;

    public HabitoController(HabitoService habitoService, UsuarioContextService usuarioContextService) {
        this.habitoService = habitoService;
        this.usuarioContextService = usuarioContextService;
    }

    @PostMapping
    @Operation(summary = "Criar hábito", description = "Cadastra um novo hábito ativo para o usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hábito criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados do hábito inválidos")
    })
    public ResponseEntity<HabitoResponseDTO> criar(@Valid @RequestBody HabitoRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        HabitoResponseDTO response = habitoService.criar(usuarioId, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar hábitos", description = "Lista os hábitos do usuário, permitindo filtrar apenas os hábitos ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hábitos listados com sucesso")
    })
    public ResponseEntity<List<HabitoResponseDTO>> listar(
            @Parameter(description = "Se verdadeiro, retorna apenas hábitos com status ativo")
            @RequestParam(required = false, defaultValue = "false") boolean apenasAtivos) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        List<HabitoResponseDTO> response = apenasAtivos
                ? habitoService.listarAtivos(usuarioId)
                : habitoService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar hábito por ID", description = "Retorna os detalhes de um hábito específico do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hábito encontrado"),
            @ApiResponse(responseCode = "404", description = "Hábito não encontrado")
    })
    public ResponseEntity<HabitoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único do hábito") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(habitoService.buscarPorId(usuarioId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar hábito", description = "Atualiza nome, descrição e frequência de um hábito existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hábito atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Hábito não encontrado")
    })
    public ResponseEntity<HabitoResponseDTO> atualizar(
            @Parameter(description = "Identificador único do hábito") @PathVariable Long id,
            @Valid @RequestBody HabitoRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(habitoService.atualizar(usuarioId, id, dto));
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativar hábito", description = "Altera o estado do hábito para ativo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hábito ativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Hábito já se encontra ativo"),
            @ApiResponse(responseCode = "404", description = "Hábito não encontrado")
    })
    public ResponseEntity<HabitoResponseDTO> ativar(
            @Parameter(description = "Identificador único do hábito") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(habitoService.ativar(usuarioId, id));
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar hábito", description = "Altera o estado do hábito para inativo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hábito desativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Hábito já se encontra inativo"),
            @ApiResponse(responseCode = "404", description = "Hábito não encontrado")
    })
    public ResponseEntity<HabitoResponseDTO> desativar(
            @Parameter(description = "Identificador único do hábito") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(habitoService.desativar(usuarioId, id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir hábito", description = "Remove um hábito do usuário, desde que não possua registros de ocorrência vinculados")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hábito excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Hábito possui registros vinculados e não pode ser excluído"),
            @ApiResponse(responseCode = "404", description = "Hábito não encontrado")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador único do hábito") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        habitoService.excluir(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
