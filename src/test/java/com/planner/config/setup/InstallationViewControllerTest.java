package com.planner.config.setup;

import com.planner.common.exception.DuplicateResourceException;
import com.planner.usuario.dto.UsuarioRequestDTO;
import com.planner.usuario.dto.UsuarioResponseDTO;
import com.planner.usuario.repository.UsuarioRepository;
import com.planner.usuario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InstallationViewControllerTest {

    @Test
    void deveMostrarSomenteCadastroQuandoInstalacaoJaFoiConcluida() {
        InstallationConfigService configService = mock(InstallationConfigService.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        UsuarioService usuarioService = mock(UsuarioService.class);
        when(configService.isInitialized()).thenReturn(true);
        InstallationViewController controller = new InstallationViewController(
                configService, usuarioRepository, usuarioService);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("config/setup", controller.setup(model));
        assertEquals(true, model.get("instalacaoConcluida"));
        org.junit.jupiter.api.Assertions.assertFalse(model.containsAttribute("databaseConfig"));
        org.junit.jupiter.api.Assertions.assertTrue(model.containsAttribute("contaPrincipal"));
        verifyNoInteractions(usuarioRepository, usuarioService);
    }

    @Test
    void devePermitirCriarOutroUsuarioSemAlterarConfiguracaoDoBanco() {
        InstallationConfigService configService = mock(InstallationConfigService.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        UsuarioService usuarioService = mock(UsuarioService.class);
        when(configService.isInitialized()).thenReturn(true);
        when(usuarioService.criar(new UsuarioRequestDTO(
                "Outra pessoa", "outra@example.com", "senha-segura")))
                .thenReturn(new UsuarioResponseDTO(2L, "Outra pessoa", "outra@example.com", null));
        InstallationViewController controller = new InstallationViewController(
                configService, usuarioRepository, usuarioService);
        ContaPrincipalDTO conta = new ContaPrincipalDTO(
                "Outra pessoa", "outra@example.com", "senha-segura", "senha-segura");

        String view = controller.criarConta(
                conta,
                new BeanPropertyBindingResult(conta, "contaPrincipal"),
                new RedirectAttributesModelMap());

        assertEquals("redirect:/login?cadastro", view);
        verify(usuarioService).criar(new UsuarioRequestDTO(
                "Outra pessoa", "outra@example.com", "senha-segura"));
        verify(configService, never()).markInitialized();
    }

    @Test
    void deveImpedirAlteracaoDoBancoDepoisDaInstalacao() {
        InstallationConfigService configService = mock(InstallationConfigService.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        InstallationViewController controller = new InstallationViewController(
                configService, usuarioRepository, mock(UsuarioService.class));
        when(configService.isInitialized()).thenReturn(true);

        String view = controller.testar(
                new DatabaseConfigDTO("localhost", 5432, "intelliplan", "user", "password"),
                new BeanPropertyBindingResult(
                        new DatabaseConfigDTO("localhost", 5432, "intelliplan", "user", "password"),
                        "databaseConfig"),
                new RedirectAttributesModelMap());

        assertEquals("redirect:/login", view);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveMarcarInstalacaoAoCriarContaInicial() {
        InstallationConfigService configService = mock(InstallationConfigService.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        UsuarioService usuarioService = mock(UsuarioService.class);
        when(configService.isInitialized()).thenReturn(false);
        when(usuarioRepository.count()).thenReturn(0L);
        InstallationViewController controller = new InstallationViewController(
                configService, usuarioRepository, usuarioService);
        ContaPrincipalDTO conta = new ContaPrincipalDTO(
                "Pessoa", "pessoa@example.com", "senha-segura", "senha-segura");

        String view = controller.criarConta(
                conta,
                new BeanPropertyBindingResult(conta, "contaPrincipal"),
                new RedirectAttributesModelMap());

        assertEquals("redirect:/login?instalacao", view);
        verify(configService).markInitialized();
    }

    @Test
    void deveManterCadastroNaTelaAoUsarEmailJaCadastrado() {
        InstallationConfigService configService = mock(InstallationConfigService.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        UsuarioService usuarioService = mock(UsuarioService.class);
        when(configService.isInitialized()).thenReturn(true);
        when(usuarioService.criar(new UsuarioRequestDTO(
                "Pessoa", "pessoa@example.com", "senha-segura")))
                .thenThrow(new DuplicateResourceException("E-mail já cadastrado."));
        InstallationViewController controller = new InstallationViewController(
                configService, usuarioRepository, usuarioService);
        ContaPrincipalDTO conta = new ContaPrincipalDTO(
                "Pessoa", "pessoa@example.com", "senha-segura", "senha-segura");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.criarConta(
                conta,
                new BeanPropertyBindingResult(conta, "contaPrincipal"),
                redirectAttributes);

        assertEquals("redirect:/setup", view);
        ContaPrincipalDTO contaRetornada =
                (ContaPrincipalDTO) redirectAttributes.getFlashAttributes().get("contaPrincipal");
        assertEquals("", contaRetornada.senha());
        assertEquals("", contaRetornada.confirmarSenha());
    }
}
