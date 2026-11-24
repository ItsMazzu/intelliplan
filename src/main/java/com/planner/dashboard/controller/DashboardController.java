package com.planner.dashboard.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.dashboard.dto.DashboardDadosDTO;
import com.planner.dashboard.service.DashboardService;
import com.planner.tarefa.service.TarefaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final TarefaService tarefaService;
    private final UsuarioContextService usuarioContextService;

    public DashboardController(DashboardService dashboardService,
                               TarefaService tarefaService,
                               UsuarioContextService usuarioContextService) {
        this.dashboardService = dashboardService;
        this.tarefaService = tarefaService;
        this.usuarioContextService = usuarioContextService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        DashboardDadosDTO dados = dashboardService.obterDadosDashboard();
        model.addAttribute("usuarioNome", dados.usuarioNome());
        model.addAttribute("primeiroNome", dados.primeiroNome());
        model.addAttribute("totalTarefas", dados.totalTarefas());
        model.addAttribute("totalProjetos", dados.totalProjetos());
        model.addAttribute("totalHabitos", dados.totalHabitos());
        model.addAttribute("diasHabitos", dados.diasHabitos());
        model.addAttribute("mesAtual", dados.mesAtual());
        model.addAttribute("diasCalendario", dados.diasCalendario());
        model.addAttribute("tarefasHoje", dados.tarefasHoje());
        model.addAttribute("progressoGeral", dados.progressoGeral());
        model.addAttribute("progressoTarefas", dados.progressoTarefas());
        model.addAttribute("progressoEstudos", dados.progressoEstudos());
        model.addAttribute("progressoHabitos", dados.progressoHabitos());
        model.addAttribute("progressoProjetos", dados.progressoProjetos());
        model.addAttribute("projetosAtivos", dados.projetosAtivos());
        model.addAttribute("materias", dados.materias());
        return "dashboard/dashboard";
    }

    @PostMapping("/tarefas/{id}/concluir")
    public String alternarConclusaoTarefa(@PathVariable Long id,
                                          RedirectAttributes redirectAttributes) {
        Optional<Long> usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário de desenvolvimento não configurado.");
            return "redirect:/tarefas";
        }
        try {
            tarefaService.alternarConclusao(usuarioIdOpt.get(), id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Status da tarefa atualizado!");
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/tarefas";
    }
}
