package com.planner.usuario.controller;

import com.planner.config.setup.InstallationConfigService;
import com.planner.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LoginViewControllerTest {

    @Test
    void deveIndicarInstalacaoPendenteNaPrimeiraExecucao() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        InstallationConfigService configService = mock(InstallationConfigService.class);
        when(configService.isInitialized()).thenReturn(false);
        when(usuarioRepository.count()).thenReturn(0L);
        LoginViewController controller = new LoginViewController(usuarioRepository, configService);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.login(model, null);

        assertEquals("auth/login", view);
        assertFalse((Boolean) model.get("instalacaoConcluida"));
    }

    @Test
    void deveIndicarInstalacaoConcluidaMesmoSemArquivoDeConfiguracao() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        InstallationConfigService configService = mock(InstallationConfigService.class);
        when(configService.isInitialized()).thenReturn(false);
        when(usuarioRepository.count()).thenReturn(1L);
        LoginViewController controller = new LoginViewController(usuarioRepository, configService);
        ExtendedModelMap model = new ExtendedModelMap();

        controller.login(model, null);

        assertTrue((Boolean) model.get("instalacaoConcluida"));
    }

    @Test
    void deveIndicarInstalacaoConcluidaPeloArquivoDeConfiguracao() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        InstallationConfigService configService = mock(InstallationConfigService.class);
        when(configService.isInitialized()).thenReturn(true);
        when(usuarioRepository.count()).thenReturn(0L);
        LoginViewController controller = new LoginViewController(usuarioRepository, configService);
        ExtendedModelMap model = new ExtendedModelMap();

        controller.login(model, null);

        assertTrue((Boolean) model.get("instalacaoConcluida"));
    }

    @Test
    void deveRedirecionarUsuarioAutenticadoParaODashboard() {
        LoginViewController controller = new LoginViewController(
                mock(UsuarioRepository.class),
                mock(InstallationConfigService.class));
        var authentication = new UsernamePasswordAuthenticationToken(
                "italo@example.com",
                "n/a",
                AuthorityUtils.createAuthorityList("ROLE_USER"));

        assertEquals("redirect:/dashboard", controller.login(new ExtendedModelMap(), authentication));
    }
}
