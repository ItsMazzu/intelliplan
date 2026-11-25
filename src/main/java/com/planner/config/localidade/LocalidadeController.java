package com.planner.config.localidade;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/localidades")
public class LocalidadeController {

    private final LocalidadeService localidadeService;

    public LocalidadeController(LocalidadeService localidadeService) {
        this.localidadeService = localidadeService;
    }

    @GetMapping("/estados")
    public List<EstadoDTO> listarEstados() {
        return localidadeService.listarEstados();
    }

    @GetMapping("/estados/{uf}/municipios")
    public List<MunicipioDTO> listarMunicipios(@PathVariable String uf) {
        return localidadeService.listarMunicipios(uf);
    }
}
