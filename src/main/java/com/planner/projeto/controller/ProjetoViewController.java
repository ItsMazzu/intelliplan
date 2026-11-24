package com.planner.projeto.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.projeto.dto.ProjetoRequestDTO;
import com.planner.projeto.dto.ProjetoResponseDTO;
import com.planner.projeto.entity.Projeto;
import com.planner.projeto.service.ProjetoService;
import com.planner.tarefa.dto.TarefaResponseDTO;
import com.planner.tarefa.service.TarefaService;
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

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/projetos")
public class ProjetoViewController {

    private final ProjetoService projetoService;
    private final TarefaService tarefaService;
    private final UsuarioService usuarioService;
    private final UsuarioContextService usuarioContextService;

    public ProjetoViewController(ProjetoService projetoService,
                                 TarefaService tarefaService,
                                 UsuarioService usuarioService,
                                 UsuarioContextService usuarioContextService) {
        this.projetoService = projetoService;
        this.tarefaService = tarefaService;
        this.usuarioService = usuarioService;
        this.usuarioContextService = usuarioContextService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Projeto.Status status, Model model) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) {
            popularUsuarioPadrao(model);
            model.addAttribute("projetos", Collections.emptyList());
            model.addAttribute("statusSelecionado", status);
            return "projetos/lista";
        }

        Long usuarioId = usuarioIdOpt.get();
        List<ProjetoResponseDTO> projetos = status == null
                ? projetoService.listarPorUsuario(usuarioId)
                : projetoService.listarPorStatus(usuarioId, status);
        popularUsuario(model, usuarioId);
        model.addAttribute("projetos", projetos);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("statusOpcoes", Projeto.Status.values());
        model.addAttribute("totalProjetos", projetos.size());
        model.addAttribute("totalAndamento", projetos.stream().filter(p -> p.status() == Projeto.Status.EM_ANDAMENTO).count());
        model.addAttribute("totalConcluidos", projetos.stream().filter(p -> p.status() == Projeto.Status.CONCLUIDO).count());
        model.addAttribute("totalPlanejados", projetos.stream().filter(p -> p.status() == Projeto.Status.PLANEJADO).count());
        return "projetos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/projetos";
        popularUsuario(model, usuarioIdOpt.get());
        if (!model.containsAttribute("projetoForm")) {
            model.addAttribute("projetoForm", new ProjetoRequestDTO("", "", Projeto.Status.PLANEJADO, null, null));
        }
        model.addAttribute("statusOpcoes", Projeto.Status.values());
        model.addAttribute("modoEdicao", false);
        return "projetos/formulario";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("projetoForm") ProjetoRequestDTO dto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/projetos";
        if (bindingResult.hasErrors()) {
            popularUsuario(model, usuarioIdOpt.get());
            model.addAttribute("statusOpcoes", Projeto.Status.values());
            model.addAttribute("modoEdicao", false);
            return "projetos/formulario";
        }
        try {
            projetoService.criar(usuarioIdOpt.get(), dto);
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/projetos/novo";
        }
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Projeto criado com sucesso!");
        return "redirect:/projetos";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/projetos";
        Long usuarioId = usuarioIdOpt.get();
        try {
            ProjetoResponseDTO projeto = projetoService.buscarPorId(usuarioId, id);
            List<TarefaResponseDTO> tarefas = tarefaService.listarPorProjeto(usuarioId, id);
            long concluidas = tarefas.stream().filter(t -> t.status() == com.planner.tarefa.entity.Tarefa.Status.CONCLUIDA).count();
            int progresso = tarefas.isEmpty() ? 0 : (int) ((concluidas * 100) / tarefas.size());
            popularUsuario(model, usuarioId);
            model.addAttribute("projeto", projeto);
            model.addAttribute("tarefas", tarefas);
            model.addAttribute("progressoProjeto", progresso);
            return "projetos/detalhes";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/projetos";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/projetos";
        Long usuarioId = usuarioIdOpt.get();
        try {
            ProjetoResponseDTO projeto = projetoService.buscarPorId(usuarioId, id);
            if (!model.containsAttribute("projetoForm")) {
                model.addAttribute("projetoForm", new ProjetoRequestDTO(
                        projeto.nome(), projeto.descricao(), projeto.status(),
                        projeto.dataInicio(), projeto.dataConclusao()));
            }
            popularUsuario(model, usuarioId);
            model.addAttribute("projetoId", id);
            model.addAttribute("statusOpcoes", Projeto.Status.values());
            model.addAttribute("modoEdicao", true);
            return "projetos/formulario";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/projetos";
        }
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("projetoForm") ProjetoRequestDTO dto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/projetos";
        if (bindingResult.hasErrors()) {
            popularUsuario(model, usuarioIdOpt.get());
            model.addAttribute("projetoId", id);
            model.addAttribute("statusOpcoes", Projeto.Status.values());
            model.addAttribute("modoEdicao", true);
            return "projetos/formulario";
        }
        try {
            projetoService.atualizar(usuarioIdOpt.get(), id, dto);
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/projetos/" + id + "/editar";
        }
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Projeto atualizado com sucesso!");
        return "redirect:/projetos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/projetos";
        try {
            projetoService.excluir(usuarioIdOpt.get(), id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Projeto excluído com sucesso!");
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/projetos";
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
