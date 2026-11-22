package com.planner.common.context;

import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UsuarioContextServiceTest {

    @Test
    void deveTratarPropriedadeAusenteOuVaziaComoNaoConfigurada() {
        for (String configuracao : new String[]{"", " "}) {
            UsuarioRepository repository = mock(UsuarioRepository.class);
            UsuarioContextService service = new UsuarioContextService(configuracao, repository);

            assertTrue(service.obterUsuarioIdOpcional().isEmpty());
            assertThrows(BusinessRuleException.class, service::obterUsuarioIdAtual);

            verifyNoInteractions(repository);
        }
    }

    @Test
    void deveResolverIdNumericoConfigurado() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        when(repository.existsById(42L)).thenReturn(true);
        UsuarioContextService service = new UsuarioContextService(" 42 ", repository);

        assertEquals(42L, service.obterUsuarioIdAtual());
        assertEquals(Optional.of(42L), service.obterUsuarioIdOpcional());

        verify(repository, org.mockito.Mockito.times(2)).existsById(42L);
    }

    @Test
    void deveRejeitarIdConfiguradoNaoNumerico() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        UsuarioContextService service = new UsuarioContextService("abc", repository);

        assertThrows(BusinessRuleException.class, service::obterUsuarioIdAtual);

        verifyNoInteractions(repository);
    }

    @Test
    void deveTratarIdNumericoInexistenteNoBanco() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        when(repository.existsById(42L)).thenReturn(false);
        UsuarioContextService service = new UsuarioContextService("42", repository);

        assertThrows(ResourceNotFoundException.class, service::obterUsuarioIdAtual);
        assertTrue(service.obterUsuarioIdOpcional().isEmpty());

        verify(repository, org.mockito.Mockito.times(2)).existsById(42L);
    }
}
