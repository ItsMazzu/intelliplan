package com.planner.common.context;

import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioContextService {

    private final String devUsuarioIdRaw;
    private final UsuarioRepository usuarioRepository;

    public UsuarioContextService(
            @Value("${app.dev.usuario-id:}") String devUsuarioIdRaw,
            UsuarioRepository usuarioRepository) {
        this.devUsuarioIdRaw = devUsuarioIdRaw;
        this.usuarioRepository = usuarioRepository;
    }

    public Long obterUsuarioIdAtual() {
        if (devUsuarioIdRaw == null || devUsuarioIdRaw.isBlank()) {
            throw new BusinessRuleException(
                    "Identificador de usuário não configurado em 'app.dev.usuario-id'. Configure um ID válido em application.properties.");
        }
        Long usuarioId;
        try {
            usuarioId = Long.parseLong(devUsuarioIdRaw.trim());
        } catch (NumberFormatException ex) {
            throw new BusinessRuleException(
                    "Identificador de usuário configurado em 'app.dev.usuario-id' é inválido: '" + devUsuarioIdRaw.trim() + "'. Deve ser um número inteiro.");
        }
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException(
                    "Usuário configurado em 'app.dev.usuario-id' (" + usuarioId + ") não encontrado no banco de dados.");
        }
        return usuarioId;
    }

    public Optional<Long> obterUsuarioIdOpcional() {
        if (devUsuarioIdRaw == null || devUsuarioIdRaw.isBlank()) {
            return Optional.empty();
        }
        try {
            Long usuarioId = Long.parseLong(devUsuarioIdRaw.trim());
            if (!usuarioRepository.existsById(usuarioId)) {
                return Optional.empty();
            }
            return Optional.of(usuarioId);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
