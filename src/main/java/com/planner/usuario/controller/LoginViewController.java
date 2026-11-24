package com.planner.usuario.controller;

import com.planner.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginViewController {

    private final UsuarioRepository usuarioRepository;

    public LoginViewController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("primeiraExecucao", usuarioRepository.count() == 0);
        return "auth/login";
    }
}
