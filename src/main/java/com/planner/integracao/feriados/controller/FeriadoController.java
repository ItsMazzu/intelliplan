package com.planner.integracao.feriados.controller;

import com.planner.integracao.feriados.dto.FeriadoDTO;
import com.planner.integracao.feriados.service.FeriadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feriados")
@Tag(name = "Feriados", description = "Consulta de feriados nacionais via BrasilAPI")
public class FeriadoController {

    private final FeriadoService feriadoService;

    public FeriadoController(FeriadoService feriadoService) {
        this.feriadoService = feriadoService;
    }

    @GetMapping("/{ano}")
    @Operation(summary = "Listar feriados nacionais por ano", description = "Retorna a lista de feriados nacionais para o ano informado a partir da BrasilAPI")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feriados consultados com sucesso")
    })
    public ResponseEntity<List<FeriadoDTO>> listarFeriados(
            @Parameter(description = "Ano com 4 dígitos para consulta (ex: 2026)") @PathVariable int ano) {
        return ResponseEntity.ok(feriadoService.listarFeriados(ano));
    }
}
