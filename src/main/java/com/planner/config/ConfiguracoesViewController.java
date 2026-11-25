package com.planner.config;

import com.planner.common.context.UsuarioContextService;
import com.planner.common.exception.BusinessRuleException;
import com.planner.config.localidade.LocalidadeService;
import com.planner.config.localidade.LocalizacaoInstalacaoDTO;
import com.planner.config.localidade.LocalizacaoRequestDTO;
import com.planner.config.setup.InstallationConfigService;
import com.planner.integracao.feriados.service.FeriadoService;
import com.planner.usuario.dto.AlterarSenhaDTO;
import com.planner.usuario.dto.UsuarioResponseDTO;
import com.planner.usuario.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestClientException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/configuracoes")
public class ConfiguracoesViewController {

    private final UsuarioContextService usuarioContextService;
    private final UsuarioService usuarioService;
    private final InstallationConfigService installationConfigService;
    private final LocalidadeService localidadeService;
    private final FeriadoService feriadoService;

    public ConfiguracoesViewController(UsuarioContextService usuarioContextService,
                                       UsuarioService usuarioService,
                                       InstallationConfigService installationConfigService,
                                       LocalidadeService localidadeService,
                                       FeriadoService feriadoService) {
        this.usuarioContextService = usuarioContextService;
        this.usuarioService = usuarioService;
        this.installationConfigService = installationConfigService;
        this.localidadeService = localidadeService;
        this.feriadoService = feriadoService;
    }

    @GetMapping
    public String configuracoes(Model model) {
        Long usuarioId = usuarioContextService.obterUsuarioIdAtual();
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);
        model.addAttribute("usuarioNome", usuario.nome());
        model.addAttribute("usuarioEmail", usuario.email());
        model.addAttribute("usuarioIniciais", iniciais(usuario.nome()));
        model.addAttribute("usuarioCriadoEm", usuario.criadoEm());
        model.addAttribute("alterarSenha", new AlterarSenhaDTO("", "", ""));
        model.addAttribute("localizacao", installationConfigService.getLocalizacao());
        model.addAttribute("coberturaRegional", feriadoService.coberturaLocalAtual());
        return "config/configuracoes";
    }

    @PostMapping("/localizacao")
    public String atualizarLocalizacao(@Valid @ModelAttribute("localizacao") LocalizacaoRequestDTO dto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensagemErroLocalizacao",
                    "Selecione um estado e um município válidos.");
            return "redirect:/configuracoes#localizacao";
        }
        try {
            LocalizacaoInstalacaoDTO localizacao = localidadeService.validar(dto);
            installationConfigService.saveLocalizacao(localizacao);
            feriadoService.invalidarCache();
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("mensagemErroLocalizacao", ex.getMessage());
            return "redirect:/configuracoes#localizacao";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("mensagemErroLocalizacao",
                    "Não foi possível validar a localização agora. Tente novamente.");
            return "redirect:/configuracoes#localizacao";
        }
        redirectAttributes.addFlashAttribute("mensagemSucessoLocalizacao",
                "Localização atualizada com sucesso.");
        return "redirect:/configuracoes#localizacao";
    }

    @PostMapping("/senha")
    public String alterarSenha(@Valid @ModelAttribute("alterarSenha") AlterarSenhaDTO dto,
                               BindingResult bindingResult,
                               Authentication authentication,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        if (dto.novaSenha() != null && dto.confirmarNovaSenha() != null
                && !dto.novaSenha().equals(dto.confirmarNovaSenha())) {
            bindingResult.rejectValue("confirmarNovaSenha", "senha.diferente",
                    "A confirmação da nova senha não coincide.");
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Revise os dados informados e confirme a nova senha.");
            return "redirect:/configuracoes#senha";
        }

        try {
            usuarioService.alterarSenha(authentication.getName(), dto);
        } catch (com.planner.common.exception.BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/configuracoes#senha";
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/login?senhaAlterada";
    }

    private String iniciais(String nome) {
        if (nome == null || nome.isBlank()) return "U";
        String[] partes = nome.trim().split("\\s+");
        return partes.length == 1
                ? partes[0].substring(0, 1).toUpperCase()
                : (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase();
    }
}
