package com.planner.usuario.controller;

import com.planner.config.setup.InstallationConfigService;
import com.planner.usuario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginViewController {

    private final UsuarioRepository usuarioRepository;
    private final InstallationConfigService installationConfigService;

    public LoginViewController(UsuarioRepository usuarioRepository,
                               InstallationConfigService installationConfigService) {
        this.usuarioRepository = usuarioRepository;
        this.installationConfigService = installationConfigService;
    }

    @GetMapping("/login")
    public String login(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/dashboard";
        }

        boolean instalacaoConcluida = installationConfigService.isInitialized() || usuarioRepository.count() > 0;
        model.addAttribute("instalacaoConcluida", instalacaoConcluida);
        return "auth/login";
    }
}
