package com.planner.common.context;

import com.planner.common.exception.BusinessRuleException;
import com.planner.usuario.entity.Usuario;
import com.planner.usuario.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class UsuarioContextService {

    private final HttpServletRequest request;
    private final UsuarioRepository usuarioRepository;

    public UsuarioContextService(HttpServletRequest request, UsuarioRepository usuarioRepository) {
        this.request = request;
        this.usuarioRepository = usuarioRepository;
    }

    public Long obterUsuarioIdAtual() {
        String headerUsuarioId = request.getHeader("X-Usuario-Id");
        if (headerUsuarioId != null && !headerUsuarioId.isBlank()) {
            try {
                Long id = Long.parseLong(headerUsuarioId.trim());
                if (!usuarioRepository.existsById(id)) {
                    throw new BusinessRuleException("Usuário informado no cabeçalho X-Usuario-Id não existe.");
                }
                return id;
            } catch (NumberFormatException e) {
                throw new BusinessRuleException("O cabeçalho X-Usuario-Id deve ser um número válido.");
            }
        }

        return usuarioRepository.findAll().stream()
                .findFirst()
                .map(Usuario::getId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Nenhum usuário encontrado no sistema. Cadastre um usuário via POST /api/v1/usuarios ou forneça o cabeçalho X-Usuario-Id."));
    }
}
