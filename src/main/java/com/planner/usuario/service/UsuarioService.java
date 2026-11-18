package com.planner.usuario.service;

import com.planner.common.exception.DuplicateResourceException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.usuario.dto.UsuarioRequestDTO;
import com.planner.usuario.dto.UsuarioResponseDTO;
import com.planner.usuario.entity.Usuario;
import com.planner.usuario.repository.UsuarioRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        String emailNormalizado = normalizarEmail(dto.email());
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new DuplicateResourceException("E-mail já cadastrado.");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        try {
            return toResponseDTO(usuarioRepository.saveAndFlush(usuario));
        } catch (DataIntegrityViolationException ex) {
            if (isEmailUniqueViolation(ex)) {
                throw new DuplicateResourceException("E-mail já cadastrado.");
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email) {
        String emailNormalizado = normalizarEmail(email);
        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        return toResponseDTO(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    private String normalizarEmail(String email) {
        return email != null ? email.trim().toLowerCase(Locale.ROOT) : "";
    }

    private boolean isEmailUniqueViolation(DataIntegrityViolationException ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof ConstraintViolationException cve) {
                String name = cve.getConstraintName();
                if (name != null) {
                    String lower = name.toLowerCase(Locale.ROOT);
                    if (lower.contains("uk_usuarios_email") || lower.contains("usuarios_email_key") || lower.contains("email")) {
                        return true;
                    }
                }
            }
            if (current.getMessage() != null) {
                String msg = current.getMessage().toLowerCase(Locale.ROOT);
                if (msg.contains("uk_usuarios_email") || msg.contains("usuarios_email_key") || (msg.contains("usuarios") && msg.contains("email"))) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCriadoEm()
        );
    }
}
