package com.planner.config;

import com.planner.common.context.UsuarioContextService;
import com.planner.usuario.dto.UsuarioResponseDTO;
import com.planner.usuario.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/configuracoes")
public class ConfiguracoesViewController {

    private final UsuarioContextService usuarioContextService;
    private final UsuarioService usuarioService;

    public ConfiguracoesViewController(UsuarioContextService usuarioContextService,
                                       UsuarioService usuarioService) {
        this.usuarioContextService = usuarioContextService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String configuracoes(Model model) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);
        model.addAttribute("usuarioNome", usuario.nome());
        model.addAttribute("usuarioEmail", usuario.email());
        model.addAttribute("usuarioIniciais", iniciais(usuario.nome()));
        model.addAttribute("usuarioCriadoEm", usuario.criadoEm());
        return "config/configuracoes";
    }

    private String iniciais(String nome) {
        if (nome == null || nome.isBlank()) return "U";
        String[] partes = nome.trim().split("\\s+");
        return partes.length == 1
                ? partes[0].substring(0, 1).toUpperCase()
                : (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase();
    }
}
