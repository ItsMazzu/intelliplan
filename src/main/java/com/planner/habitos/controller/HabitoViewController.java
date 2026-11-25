package com.planner.habitos.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.DuplicateResourceException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.habitos.dto.HabitoRequestDTO;
import com.planner.habitos.dto.HabitoResponseDTO;
import com.planner.habitos.dto.RegistroHabitoRequestDTO;
import com.planner.habitos.dto.RegistroHabitoResponseDTO;
import com.planner.habitos.entity.Habito;
import com.planner.habitos.service.HabitoService;
import com.planner.habitos.service.RegistroHabitoService;
import com.planner.usuario.dto.UsuarioResponseDTO;
import com.planner.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/habitos")
public class HabitoViewController {

    private final HabitoService habitoService;
    private final RegistroHabitoService registroHabitoService;
    private final UsuarioService usuarioService;
    private final UsuarioContextService usuarioContextService;

    public HabitoViewController(HabitoService habitoService,
                                RegistroHabitoService registroHabitoService,
                                UsuarioService usuarioService,
                                UsuarioContextService usuarioContextService) {
        this.habitoService = habitoService;
        this.registroHabitoService = registroHabitoService;
        this.usuarioService = usuarioService;
        this.usuarioContextService = usuarioContextService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false, defaultValue = "false") boolean apenasAtivos,
                         Model model) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) {
            popularUsuarioPadrao(model);
            model.addAttribute("habitos", Collections.emptyList());
            model.addAttribute("habitosResumo", Collections.emptyList());
            model.addAttribute("apenasAtivos", apenasAtivos);
            return "habitos/lista";
        }

        Long usuarioId = usuarioIdOpt.get();
        List<HabitoResponseDTO> habitos = apenasAtivos
                ? habitoService.listarAtivos(usuarioId)
                : habitoService.listarPorUsuario(usuarioId);
        LocalDate inicio = LocalDate.now().minusDays(29);
        LocalDate hoje = LocalDate.now();
        List<Map<String, Object>> habitosResumo = new ArrayList<>();
        int concluidosHoje = 0;
        for (HabitoResponseDTO habito : habitos) {
            List<RegistroHabitoResponseDTO> registros = registroHabitoService
                    .listarPorPeriodo(usuarioId, habito.id(), inicio, hoje);
            long concluidosPeriodo = registros.size();
            boolean concluidoHoje = registros.stream().anyMatch(r -> r.dataRegistro().equals(hoje));
            if (concluidoHoje) concluidosHoje++;
            int progresso = (int) ((concluidosPeriodo * 100) / 30);
            Map<String, Object> resumo = new HashMap<>();
            resumo.put("habito", habito);
            resumo.put("registros", registros);
            resumo.put("progresso", Math.min(progresso, 100));
            resumo.put("concluidoHoje", concluidoHoje);
            habitosResumo.add(resumo);
        }

        popularUsuario(model, usuarioId);
        model.addAttribute("habitos", habitos);
        model.addAttribute("habitosResumo", habitosResumo);
        model.addAttribute("apenasAtivos", apenasAtivos);
        model.addAttribute("totalHabitos", habitos.size());
        model.addAttribute("habitosAtivos", habitos.stream().filter(HabitoResponseDTO::ativo).count());
        model.addAttribute("concluidosHoje", concluidosHoje);
        return "habitos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/habitos";
        popularUsuario(model, usuarioIdOpt.get());
        if (!model.containsAttribute("habitoForm")) {
            model.addAttribute("habitoForm",
                    new HabitoRequestDTO("", "", Habito.Frequencia.DIARIO));
        }
        model.addAttribute("frequenciaOpcoes", Habito.Frequencia.values());
        model.addAttribute("modoEdicao", false);
        return "habitos/formulario";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("habitoForm") HabitoRequestDTO dto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/habitos";
        if (bindingResult.hasErrors()) {
            popularUsuario(model, usuarioIdOpt.get());
            model.addAttribute("frequenciaOpcoes", Habito.Frequencia.values());
            model.addAttribute("modoEdicao", false);
            return "habitos/formulario";
        }
        habitoService.criar(usuarioIdOpt.get(), dto);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Hábito criado com sucesso!");
        return "redirect:/habitos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/habitos";
        Long usuarioId = usuarioIdOpt.get();
        try {
            HabitoResponseDTO habito = habitoService.buscarPorId(usuarioId, id);
            if (!model.containsAttribute("habitoForm")) {
                model.addAttribute("habitoForm",
                        new HabitoRequestDTO(habito.nome(), habito.descricao(), habito.frequencia()));
            }
            popularUsuario(model, usuarioId);
            model.addAttribute("habitoId", id);
            model.addAttribute("frequenciaOpcoes", Habito.Frequencia.values());
            model.addAttribute("modoEdicao", true);
            return "habitos/formulario";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/habitos";
        }
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("habitoForm") HabitoRequestDTO dto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/habitos";
        if (bindingResult.hasErrors()) {
            popularUsuario(model, usuarioIdOpt.get());
            model.addAttribute("habitoId", id);
            model.addAttribute("frequenciaOpcoes", Habito.Frequencia.values());
            model.addAttribute("modoEdicao", true);
            return "habitos/formulario";
        }
        try {
            habitoService.atualizar(usuarioIdOpt.get(), id, dto);
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/habitos";
        }
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Hábito atualizado com sucesso!");
        return "redirect:/habitos";
    }

    @PostMapping("/{id}/registrar")
    public String registrar(@PathVariable Long id,
                            @RequestParam(required = false) LocalDate dataRegistro,
                            @RequestParam(required = false) String observacao,
                            RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/habitos";
        LocalDate data = dataRegistro == null ? LocalDate.now() : dataRegistro;
        try {
            registroHabitoService.registrar(
                    usuarioIdOpt.get(),
                    new RegistroHabitoRequestDTO(id, data, observacao));
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Realização registrada com sucesso!");
        } catch (BusinessRuleException | DuplicateResourceException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/habitos";
    }

    @PostMapping("/{id}/ativar")
    public String ativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/habitos";
        try {
            habitoService.ativar(usuarioIdOpt.get(), id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Hábito ativado!");
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/habitos";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/habitos";
        try {
            habitoService.desativar(usuarioIdOpt.get(), id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Hábito desativado!");
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/habitos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/habitos";
        try {
            habitoService.excluir(usuarioIdOpt.get(), id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Hábito excluído com sucesso!");
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/habitos";
    }

    @PostMapping("/registros/{registroId}/excluir")
    public String excluirRegistro(@PathVariable Long registroId, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/habitos";
        try {
            registroHabitoService.excluir(usuarioIdOpt.get(), registroId);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Registro excluído com sucesso!");
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/habitos";
    }

    private void popularUsuario(Model model, Long usuarioId) {
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);
        model.addAttribute("usuarioNome", usuario.nome());
        model.addAttribute("usuarioIniciais", iniciais(usuario.nome()));
    }

    private void popularUsuarioPadrao(Model model) {
        model.addAttribute("usuarioNome", "Visitante");
        model.addAttribute("usuarioIniciais", "VI");
    }

    private String iniciais(String nome) {
        if (nome == null || nome.isBlank()) return "U";
        String[] partes = nome.trim().split("\\s+");
        return partes.length == 1 ? partes[0].substring(0, 1).toUpperCase()
                : (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase();
    }
}
