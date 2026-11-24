package com.planner.tarefa.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.projeto.dto.ProjetoResponseDTO;
import com.planner.projeto.service.ProjetoService;
import com.planner.tarefa.dto.TarefaRequestDTO;
import com.planner.tarefa.dto.TarefaResponseDTO;
import com.planner.tarefa.entity.Tarefa;
import com.planner.tarefa.service.TarefaService;
import com.planner.usuario.dto.UsuarioResponseDTO;
import com.planner.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tarefas")
public class TarefaViewController {

    private final TarefaService tarefaService;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;
    private final UsuarioContextService usuarioContextService;

    public TarefaViewController(TarefaService tarefaService,
                                ProjetoService projetoService,
                                UsuarioService usuarioService,
                                UsuarioContextService usuarioContextService) {
        this.tarefaService = tarefaService;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
        this.usuarioContextService = usuarioContextService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Tarefa.Status status,
                         @RequestParam(required = false) Long projetoId,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                         Model model) {
        Optional<Long> usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) {
            popularDadosVazios(model);
            return "tarefas/tarefas";
        }

        Long usuarioId = usuarioIdOpt.get();
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);
        List<TarefaResponseDTO> tarefas = tarefaService.listarComFiltros(usuarioId, status, projetoId, inicio, fim);
        List<ProjetoResponseDTO> projetos = projetoService.listarPorUsuario(usuarioId);

        List<TarefaResponseDTO> todasDoUsuario = tarefaService.listarPorUsuario(usuarioId);
        long totalTarefas = todasDoUsuario.size();
        long totalPendentes = todasDoUsuario.stream().filter(t -> t.status() == Tarefa.Status.PENDENTE).count();
        long totalEmAndamento = todasDoUsuario.stream().filter(t -> t.status() == Tarefa.Status.EM_ANDAMENTO).count();
        long totalConcluidas = todasDoUsuario.stream().filter(t -> t.status() == Tarefa.Status.CONCLUIDA).count();

        model.addAttribute("usuarioNome", usuario.nome());
        model.addAttribute("primeiroNome", extrairPrimeiroNome(usuario.nome()));
        model.addAttribute("tarefas", tarefas);
        model.addAttribute("projetos", projetos);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("projetoIdSelecionado", projetoId);
        model.addAttribute("inicioSelecionado", inicio);
        model.addAttribute("fimSelecionado", fim);
        model.addAttribute("totalTarefas", totalTarefas);
        model.addAttribute("totalPendentes", totalPendentes);
        model.addAttribute("totalEmAndamento", totalEmAndamento);
        model.addAttribute("totalConcluidas", totalConcluidas);

        return "tarefas/tarefas";
    }

    @GetMapping("/nova")
    public String formularioNovo(Model model) {
        Optional<Long> usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) {
            return "redirect:/tarefas";
        }

        Long usuarioId = usuarioIdOpt.get();
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);
        List<ProjetoResponseDTO> projetos = projetoService.listarPorUsuario(usuarioId);

        if (!model.containsAttribute("tarefaForm")) {
            model.addAttribute("tarefaForm", new TarefaRequestDTO("", "", Tarefa.Status.PENDENTE, Tarefa.Prioridade.MEDIA, null, null));
        }

        model.addAttribute("usuarioNome", usuario.nome());
        model.addAttribute("projetos", projetos);
        model.addAttribute("statusOpcoes", Tarefa.Status.values());
        model.addAttribute("prioridadeOpcoes", Tarefa.Prioridade.values());
        model.addAttribute("modoEdicao", false);

        return "tarefas/formulario";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("tarefaForm") TarefaRequestDTO dto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Optional<Long> usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) {
            return "redirect:/tarefas";
        }

        Long usuarioId = usuarioIdOpt.get();
        if (bindingResult.hasErrors()) {
            UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);
            model.addAttribute("usuarioNome", usuario.nome());
            model.addAttribute("projetos", projetoService.listarPorUsuario(usuarioId));
            model.addAttribute("statusOpcoes", Tarefa.Status.values());
            model.addAttribute("prioridadeOpcoes", Tarefa.Prioridade.values());
            model.addAttribute("modoEdicao", false);
            return "tarefas/formulario";
        }

        tarefaService.criar(usuarioId, dto);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Tarefa criada com sucesso!");
        return "redirect:/tarefas";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Optional<Long> usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) {
            return "redirect:/tarefas";
        }

        Long usuarioId = usuarioIdOpt.get();
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);
        TarefaResponseDTO tarefa = tarefaService.buscarPorId(usuarioId, id);
        List<ProjetoResponseDTO> projetos = projetoService.listarPorUsuario(usuarioId);

        if (!model.containsAttribute("tarefaForm")) {
            model.addAttribute("tarefaForm", new TarefaRequestDTO(
                    tarefa.titulo(),
                    tarefa.descricao(),
                    tarefa.status(),
                    tarefa.prioridade(),
                    tarefa.dataVencimento(),
                    tarefa.projetoId()
            ));
        }

        model.addAttribute("usuarioNome", usuario.nome());
        model.addAttribute("tarefaId", id);
        model.addAttribute("projetos", projetos);
        model.addAttribute("statusOpcoes", Tarefa.Status.values());
        model.addAttribute("prioridadeOpcoes", Tarefa.Prioridade.values());
        model.addAttribute("modoEdicao", true);

        return "tarefas/formulario";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("tarefaForm") TarefaRequestDTO dto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        Optional<Long> usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) {
            return "redirect:/tarefas";
        }

        Long usuarioId = usuarioIdOpt.get();
        if (bindingResult.hasErrors()) {
            UsuarioResponseDTO usuario = usuarioService.buscarPorId(usuarioId);
            model.addAttribute("usuarioNome", usuario.nome());
            model.addAttribute("tarefaId", id);
            model.addAttribute("projetos", projetoService.listarPorUsuario(usuarioId));
            model.addAttribute("statusOpcoes", Tarefa.Status.values());
            model.addAttribute("prioridadeOpcoes", Tarefa.Prioridade.values());
            model.addAttribute("modoEdicao", true);
            return "tarefas/formulario";
        }

        tarefaService.atualizar(usuarioId, id, dto);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Tarefa atualizada com sucesso!");
        return "redirect:/tarefas";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioContextService.obterUsuarioIdOpcional().ifPresent(usuarioId ->
                tarefaService.excluir(usuarioId, id));
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Tarefa excluída com sucesso!");
        return "redirect:/tarefas";
    }

    private void popularDadosVazios(Model model) {
        model.addAttribute("usuarioNome", "Visitante");
        model.addAttribute("primeiroNome", "Visitante");
        model.addAttribute("tarefas", Collections.emptyList());
        model.addAttribute("projetos", Collections.emptyList());
        model.addAttribute("statusSelecionado", null);
        model.addAttribute("projetoIdSelecionado", null);
        model.addAttribute("inicioSelecionado", null);
        model.addAttribute("fimSelecionado", null);
        model.addAttribute("totalTarefas", 0L);
        model.addAttribute("totalPendentes", 0L);
        model.addAttribute("totalEmAndamento", 0L);
        model.addAttribute("totalConcluidas", 0L);
    }

    private String extrairPrimeiroNome(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            return "Visitante";
        }
        String[] partes = nomeCompleto.trim().split("\\s+");
        return partes[0];
    }
}
