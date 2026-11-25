package com.planner.usuario.service;

import com.planner.common.exception.BusinessRuleException;
import com.planner.usuario.dto.AlterarSenhaDTO;
import com.planner.usuario.entity.Usuario;
import com.planner.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServicePasswordTest {

    @Test
    void devePersistirSomenteNovoHashQuandoDadosForemValidos() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        Usuario usuario = new Usuario();
        usuario.setSenha("hash-atual");
        when(repository.findByEmail("pessoa@example.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("senha-atual", "hash-atual")).thenReturn(true);
        when(encoder.encode("nova-senha-segura")).thenReturn("novo-hash");
        UsuarioService service = new UsuarioService(repository, encoder);

        service.alterarSenha("PESSOA@example.com ",
                new AlterarSenhaDTO("senha-atual", "nova-senha-segura", "nova-senha-segura"));

        assertEquals("novo-hash", usuario.getSenha());
        verify(repository).save(usuario);
    }

    @Test
    void deveRejeitarSenhaAtualIncorreta() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        Usuario usuario = new Usuario();
        usuario.setSenha("hash-atual");
        when(repository.findByEmail("pessoa@example.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("senha-incorreta", "hash-atual")).thenReturn(false);
        UsuarioService service = new UsuarioService(repository, encoder);

        assertThrows(BusinessRuleException.class, () ->
                service.alterarSenha("pessoa@example.com",
                        new AlterarSenhaDTO("senha-incorreta", "nova-senha-segura", "nova-senha-segura")));
        verify(repository, never()).save(usuario);
    }

    @Test
    void deveRejeitarConfirmacaoDivergente() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UsuarioService service = new UsuarioService(repository, encoder);

        assertThrows(BusinessRuleException.class, () ->
                service.alterarSenha("pessoa@example.com",
                        new AlterarSenhaDTO("senha-atual", "nova-senha-segura", "outra-senha")));
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
