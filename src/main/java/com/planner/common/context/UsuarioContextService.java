package com.planner.common.context;

import com.planner.common.exception.BusinessRuleException;
import com.planner.usuario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioContextService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioContextService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Long obterUsuarioIdAtual() {
        return obterUsuarioIdOpcional()
                .orElseThrow(() -> new BusinessRuleException("Usuário autenticado não encontrado."));
    }

    public Optional<Long> obterUsuarioIdOpcional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email.toLowerCase(java.util.Locale.ROOT).trim())
                .map(com.planner.usuario.entity.Usuario::getId);
    }
}
