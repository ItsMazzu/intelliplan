package com.planner.common.context;

import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioContextService {

    private final Long devUsuarioId;
    private final UsuarioRepository usuarioRepository;

    public UsuarioContextService(
            @Value("${app.dev.usuario-id:#{null}}") Long devUsuarioId,
            UsuarioRepository usuarioRepository) {
        this.devUsuarioId = devUsuarioId;
        this.usuarioRepository = usuarioRepository;
    }

    public Long obterUsuarioIdAtual() {
        if (devUsuarioId == null) {
            throw new BusinessRuleException(
                    "Identificador de usuário não configurado em 'app.dev.usuario-id'. Configure um ID válido em application.properties.");
        }
        if (!usuarioRepository.existsById(devUsuarioId)) {
            throw new ResourceNotFoundException(
                    "Usuário configurado em 'app.dev.usuario-id' (" + devUsuarioId + ") não encontrado no banco de dados.");
        }
        return devUsuarioId;
    }

    public Optional<Long> obterUsuarioIdOpcional() {
        if (devUsuarioId == null || !usuarioRepository.existsById(devUsuarioId)) {
            return Optional.empty();
        }
        return Optional.of(devUsuarioId);
    }
}
