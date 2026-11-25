package com.planner.config.setup;

import com.planner.common.exception.DuplicateResourceException;
import com.planner.common.exception.BusinessRuleException;
import com.planner.config.localidade.LocalidadeService;
import com.planner.config.localidade.LocalizacaoInstalacaoDTO;
import com.planner.config.localidade.LocalizacaoRequestDTO;
import com.planner.usuario.dto.UsuarioRequestDTO;
import com.planner.usuario.repository.UsuarioRepository;
import com.planner.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.RestClientException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/setup")
public class InstallationViewController {

    private final InstallationConfigService configService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final LocalidadeService localidadeService;

    public InstallationViewController(InstallationConfigService configService,
                                      UsuarioRepository usuarioRepository,
                                      UsuarioService usuarioService,
                                      LocalidadeService localidadeService) {
        this.configService = configService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.localidadeService = localidadeService;
    }

    @GetMapping
    public synchronized String setup(Model model) {
        boolean instalacaoConcluida = instalacaoConcluida();
        if (!model.containsAttribute("contaPrincipal")) {
            LocalizacaoInstalacaoDTO localizacao = configService.getLocalizacao();
            model.addAttribute("contaPrincipal", new ContaPrincipalDTO(
                    "", "", "", "", localizacao.uf(), localizacao.municipio(), localizacao.codigoIbge()));
        }
        if (!instalacaoConcluida && !model.containsAttribute("databaseConfig")) {
            model.addAttribute("databaseConfig", new DatabaseConfigDTO("localhost", 5432, "intelliplan", "", ""));
        }
        model.addAttribute("instalacaoConcluida", instalacaoConcluida);
        model.addAttribute("localizacaoObrigatoria", !instalacaoConcluida);
        model.addAttribute("localizacao", configService.getLocalizacao());
        return "config/setup";
    }

    @PostMapping("/testar")
    public synchronized String testar(@Valid @ModelAttribute("databaseConfig") DatabaseConfigDTO dto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        if (instalacaoConcluida()) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.databaseConfig", bindingResult);
            redirectAttributes.addFlashAttribute("databaseConfig", dto);
            return "redirect:/setup";
        }
        if (!configService.testConnection(dto)) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Não foi possível conectar ao PostgreSQL. Verifique os dados e confirme que o banco já existe.");
            redirectAttributes.addFlashAttribute("databaseConfig", dto);
            return "redirect:/setup";
        }
        configService.saveDatabaseConfig(dto);
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Conexão validada e configuração salva. Reinicie o IntelliPlan para aplicar a conexão.");
        redirectAttributes.addFlashAttribute("databaseConfig", dto);
        return "redirect:/setup";
    }

    @PostMapping("/criar-conta")
    public synchronized String criarConta(@Valid @ModelAttribute("contaPrincipal") ContaPrincipalDTO dto,
                                          BindingResult bindingResult,
                                          RedirectAttributes redirectAttributes) {
        boolean primeiroUsuario = !instalacaoConcluida();
        if (!dto.senha().equals(dto.confirmarSenha())) {
            bindingResult.rejectValue("confirmarSenha", "senha.diferente", "As senhas informadas não coincidem.");
        }
        LocalizacaoInstalacaoDTO localizacao = validarLocalizacao(dto, primeiroUsuario, bindingResult);
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Revise os dados informados e confirme as senhas.");
            redirectAttributes.addFlashAttribute("contaPrincipal", contaSemSenhas(dto));
            return "redirect:/setup";
        }
        try {
            usuarioService.criar(new UsuarioRequestDTO(dto.nome(), dto.email(), dto.senha()));
        } catch (DuplicateResourceException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            redirectAttributes.addFlashAttribute("contaPrincipal", contaSemSenhas(dto));
            return "redirect:/setup";
        }
        if (primeiroUsuario) {
            configService.saveLocalizacao(localizacao);
        }
        if (primeiroUsuario) {
            configService.markInitialized();
        }
        return primeiroUsuario ? "redirect:/login?instalacao" : "redirect:/login?cadastro";
    }

    private boolean instalacaoConcluida() {
        return configService.isInitialized() || usuarioRepository.count() > 0;
    }

    private ContaPrincipalDTO contaSemSenhas(ContaPrincipalDTO dto) {
        return new ContaPrincipalDTO(
                dto.nome(), dto.email(), "", "", dto.uf(), dto.municipio(), dto.codigoIbge());
    }

    private LocalizacaoInstalacaoDTO validarLocalizacao(
            ContaPrincipalDTO dto,
            boolean obrigatoria,
            BindingResult bindingResult) {
        boolean algumCampoInformado = valorInformado(dto.uf())
                || valorInformado(dto.municipio())
                || valorInformado(dto.codigoIbge());
        if (!algumCampoInformado) {
            if (obrigatoria) {
                bindingResult.reject("localizacao.obrigatoria", "Selecione o estado e o município da instalação.");
            }
            return null;
        }
        if (!valorInformado(dto.uf()) || !valorInformado(dto.municipio()) || !valorInformado(dto.codigoIbge())) {
            bindingResult.reject("localizacao.incompleta", "Selecione o estado e o município correspondente.");
            return null;
        }
        try {
            return localidadeService.validar(
                    new LocalizacaoRequestDTO(dto.uf(), dto.municipio(), dto.codigoIbge()));
        } catch (BusinessRuleException ex) {
            bindingResult.reject("localizacao.invalida", ex.getMessage());
            return null;
        } catch (RestClientException ex) {
            bindingResult.reject("localizacao.indisponivel",
                    "Não foi possível validar a localização agora. Tente novamente.");
            return null;
        }
    }

    private boolean valorInformado(String valor) {
        return valor != null && !valor.isBlank();
    }
}
