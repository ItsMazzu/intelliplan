package com.planner.common.context;

import com.planner.agenda.controller.AgendaViewController;
import com.planner.config.ConfiguracoesViewController;
import com.planner.dashboard.controller.DashboardController;
import com.planner.estudos.controller.EstudoViewController;
import com.planner.habitos.controller.HabitoViewController;
import com.planner.projeto.controller.ProjetoViewController;
import com.planner.tarefa.controller.TarefaViewController;
import com.planner.usuario.dto.UsuarioResponseDTO;
import com.planner.usuario.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;

@ControllerAdvice(assignableTypes = {
        AgendaViewController.class,
        ConfiguracoesViewController.class,
        DashboardController.class,
        EstudoViewController.class,
        HabitoViewController.class,
        ProjetoViewController.class,
        TarefaViewController.class
})
public class UsuarioPerfilModelAdvice {

    private final UsuarioService usuarioService;

    public UsuarioPerfilModelAdvice(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute
    public void adicionarPerfilUsuario(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return;
        }

        UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(authentication.getName());
        String nome = normalizarNome(usuario.nome());
        model.addAttribute("perfilUsuarioNome", nome);
        model.addAttribute("perfilUsuarioIniciais", iniciais(nome));
    }

    private String normalizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return "Usuário";
        }
        return nome.trim().replaceAll("\\s+", " ");
    }

    private String iniciais(String nome) {
        String[] partes = nome.split("\\s+");
        String primeira = primeiraLetra(partes[0]);
        if (partes.length == 1) {
            return primeira.toUpperCase(Locale.ROOT);
        }
        String ultima = primeiraLetra(partes[partes.length - 1]);
        return (primeira + ultima).toUpperCase(Locale.ROOT);
    }

    private String primeiraLetra(String palavra) {
        int primeiroCodigo = palavra.codePointAt(0);
        return new String(Character.toChars(primeiroCodigo));
    }
}
