package com.planner.common.context;

import com.planner.usuario.dto.UsuarioResponseDTO;
import com.planner.usuario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UsuarioPerfilModelAdviceTest {

    @Test
    void deveExibirNomeNormalizadoEIniciaisDaContaAutenticada() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        when(usuarioService.buscarPorEmail("italo@example.com"))
                .thenReturn(new UsuarioResponseDTO(1L, "  Ítalo   Silva Santos  ", "italo@example.com", null));
        UsuarioPerfilModelAdvice advice = new UsuarioPerfilModelAdvice(usuarioService);
        ExtendedModelMap model = new ExtendedModelMap();
        var authentication = new UsernamePasswordAuthenticationToken(
                "italo@example.com",
                "n/a",
                AuthorityUtils.createAuthorityList("ROLE_USER"));

        advice.adicionarPerfilUsuario(model, authentication);

        assertEquals("Ítalo Silva Santos", model.get("perfilUsuarioNome"));
        assertEquals("ÍS", model.get("perfilUsuarioIniciais"));
        verify(usuarioService).buscarPorEmail("italo@example.com");
    }

    @Test
    void deveUsarFallbackNeutroParaNomeVazio() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        when(usuarioService.buscarPorEmail("italo@example.com"))
                .thenReturn(new UsuarioResponseDTO(1L, " \t ", "italo@example.com", null));
        UsuarioPerfilModelAdvice advice = new UsuarioPerfilModelAdvice(usuarioService);
        ExtendedModelMap model = new ExtendedModelMap();
        var authentication = new UsernamePasswordAuthenticationToken(
                "italo@example.com",
                "n/a",
                AuthorityUtils.createAuthorityList("ROLE_USER"));

        advice.adicionarPerfilUsuario(model, authentication);

        assertEquals("Usuário", model.get("perfilUsuarioNome"));
        assertEquals("U", model.get("perfilUsuarioIniciais"));
    }

    @Test
    void naoBuscaPerfilSemAutenticacao() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        UsuarioPerfilModelAdvice advice = new UsuarioPerfilModelAdvice(usuarioService);

        advice.adicionarPerfilUsuario(new ExtendedModelMap(), null);

        verifyNoInteractions(usuarioService);
    }
}
