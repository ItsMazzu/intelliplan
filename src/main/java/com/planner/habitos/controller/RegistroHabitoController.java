package com.planner.habitos.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.habitos.dto.RegistroHabitoRequestDTO;
import com.planner.habitos.dto.RegistroHabitoResponseDTO;
import com.planner.habitos.service.RegistroHabitoService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Registros de Hábitos", description = "Operações para registro e acompanhamento de ocorrências de hábitos")
public class RegistroHabitoController {

    private final RegistroHabitoService registroHabitoService;
    private final UsuarioContextService usuarioContextService;

    public RegistroHabitoController(RegistroHabitoService registroHabitoService,
                                   UsuarioContextService usuarioContextService) {
        this.registroHabitoService = registroHabitoService;
        this.usuarioContextService = usuarioContextService;
    }

    @PostMapping("/registros-habito")
    @Operation(summary = "Registrar ocorrência de hábito", description = "Registra a realização de um hábito para uma data específica")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ocorrência registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Hábito inativo, data futura ou dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Hábito informado não encontrado"),
            @ApiResponse(responseCode = "409", description = "Já existe um registro para o hábito na data informada")
    })
    public ResponseEntity<RegistroHabitoResponseDTO> registrar(@Valid @RequestBody RegistroHabitoRequestDTO dto) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        RegistroHabitoResponseDTO response = registroHabitoService.registrar(usuarioId, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/registros-habito/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/registros-habito")
    @Operation(summary = "Listar registros de um hábito", description = "Lista as ocorrências registradas para um determinado hábito, com filtro opcional por período")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registros listados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de período inválidos"),
            @ApiResponse(responseCode = "404", description = "Hábito informado não encontrado")
    })
    public ResponseEntity<List<RegistroHabitoResponseDTO>> listar(
            @Parameter(description = "Identificador do hábito") @RequestParam Long habitoId,
            @Parameter(description = "Data inicial para busca por período")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final para busca por período")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        List<RegistroHabitoResponseDTO> response = (inicio != null || fim != null)
                ? registroHabitoService.listarPorPeriodo(usuarioId, habitoId, inicio, fim)
                : registroHabitoService.listarPorHabito(usuarioId, habitoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/habitos/{habitoId}/registros")
    @Operation(summary = "Listar registros por rota aninhada", description = "Rota alternativa aninhada para listar ocorrências de um hábito específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registros listados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de período inválidos"),
            @ApiResponse(responseCode = "404", description = "Hábito informado não encontrado")
    })
    public ResponseEntity<List<RegistroHabitoResponseDTO>> listarPorRotaAninhada(
            @Parameter(description = "Identificador do hábito") @PathVariable Long habitoId,
            @Parameter(description = "Data inicial para busca por período")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final para busca por período")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        List<RegistroHabitoResponseDTO> response = (inicio != null || fim != null)
                ? registroHabitoService.listarPorPeriodo(usuarioId, habitoId, inicio, fim)
                : registroHabitoService.listarPorHabito(usuarioId, habitoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/registros-habito/{id}")
    @Operation(summary = "Buscar registro por ID", description = "Retorna os detalhes de uma ocorrência específica de hábito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    public ResponseEntity<RegistroHabitoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador único do registro") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        return ResponseEntity.ok(registroHabitoService.buscarPorId(usuarioId, id));
    }

    @DeleteMapping("/registros-habito/{id}")
    @Operation(summary = "Excluir registro", description = "Remove uma ocorrência de hábito registrada")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador único do registro") @PathVariable Long id) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        registroHabitoService.excluir(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
