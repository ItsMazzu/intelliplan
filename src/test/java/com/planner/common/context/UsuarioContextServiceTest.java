package com.planner.common.context;

import com.planner.common.exception.BusinessRuleException;
import com.planner.usuario.entity.Usuario;
import com.planner.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioContextServiceTest {

    @Test
    void deveRetornarVazioSemAutenticacao() {
        SecurityContextHolder.clearContext();
        UsuarioRepository repository = mock(UsuarioRepository.class);
        UsuarioContextService service = new UsuarioContextService(repository);

        assertTrue(service.obterUsuarioIdOpcional().isEmpty());
        assertThrows(BusinessRuleException.class, service::obterUsuarioIdAtual);
    }

    @Test
    void deveResolverUsuarioAutenticadoPeloEmail() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        Usuario usuario = new Usuario();
        usuario.setId(42L);
        usuario.setEmail("ana@example.com");
        when(repository.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "ANA@example.com",
                        "n/a",
                        AuthorityUtils.createAuthorityList("ROLE_USER")));
        UsuarioContextService service = new UsuarioContextService(repository);

        assertEquals(42L, service.obterUsuarioIdAtual());
        assertEquals(Optional.of(42L), service.obterUsuarioIdOpcional());
        SecurityContextHolder.clearContext();
    }
}
