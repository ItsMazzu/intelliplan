package com.planner.common.context;

import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioContextService {

    private final String devUsuarioIdConfigurado;
    private final UsuarioRepository usuarioRepository;

    public UsuarioContextService(
            @Value("${app.dev.usuario-id:}") String devUsuarioIdConfigurado,
            UsuarioRepository usuarioRepository) {
        this.devUsuarioIdConfigurado = devUsuarioIdConfigurado;
        this.usuarioRepository = usuarioRepository;
    }

    public Long obterUsuarioIdAtual() {
        Optional<Long> devUsuarioId = obterDevUsuarioIdConfigurado();
        if (devUsuarioId.isEmpty()) {
            throw new BusinessRuleException(
                    "Identificador de usuário não configurado em 'app.dev.usuario-id'. Configure um ID válido em application.properties.");
        }
        if (!usuarioRepository.existsById(devUsuarioId.get())) {
            throw new ResourceNotFoundException(
                    "Usuário configurado em 'app.dev.usuario-id' (" + devUsuarioId.get() + ") não encontrado no banco de dados.");
        }
        return devUsuarioId.get();
    }

    public Optional<Long> obterUsuarioIdOpcional() {
        Optional<Long> devUsuarioId = obterDevUsuarioIdConfigurado();
        if (devUsuarioId.isEmpty() || !usuarioRepository.existsById(devUsuarioId.get())) {
            return Optional.empty();
        }
        return devUsuarioId;
    }

    private Optional<Long> obterDevUsuarioIdConfigurado() {
        if (devUsuarioIdConfigurado == null || devUsuarioIdConfigurado.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(devUsuarioIdConfigurado.trim()));
        } catch (NumberFormatException ex) {
            throw new BusinessRuleException(
                    "Identificador de usuário inválido em 'app.dev.usuario-id'. Configure um valor numérico.");
        }
    }
}
