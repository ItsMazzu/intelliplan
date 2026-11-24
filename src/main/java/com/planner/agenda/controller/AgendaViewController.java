package com.planner.agenda.controller;

import com.planner.agenda.dto.CompromissoRequestDTO;
import com.planner.agenda.dto.CompromissoResponseDTO;
import com.planner.agenda.service.CompromissoService;
import com.planner.common.context.UsuarioContextService;
import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/agenda")
public class AgendaViewController {

    private final CompromissoService compromissoService;
    private final UsuarioService usuarioService;
    private final UsuarioContextService usuarioContextService;

    public AgendaViewController(CompromissoService compromissoService,
                                UsuarioService usuarioService,
                                UsuarioContextService usuarioContextService) {
        this.compromissoService = compromissoService;
        this.usuarioService = usuarioService;
        this.usuarioContextService = usuarioContextService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Integer ano,
                         @RequestParam(required = false) Integer mes,
                         Model model) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        YearMonth mesAtual = YearMonth.now();
        if (ano != null && mes != null) {
            mesAtual = YearMonth.of(ano, mes);
        }

        if (usuarioIdOpt.isEmpty()) {
            popularUsuarioPadrao(model);
            model.addAttribute("compromissos", Collections.emptyList());
            model.addAttribute("diasCalendario", criarDiasCalendario(mesAtual, Collections.emptyList()));
            model.addAttribute("mesAtual", mesAtual);
            model.addAttribute("mesAnterior", mesAtual.minusMonths(1));
            model.addAttribute("mesProximo", mesAtual.plusMonths(1));
            model.addAttribute("mesTitulo", tituloMes(mesAtual));
            return "agenda/lista";
        }

        Long usuarioId = usuarioIdOpt.get();
        List<CompromissoResponseDTO> compromissos;
        if (ano != null || mes != null) {
            compromissos = compromissoService.listarPorPeriodo(
                    usuarioId,
                    mesAtual.atDay(1).atStartOfDay(),
                    mesAtual.atEndOfMonth().atTime(23, 59, 59));
        } else {
            compromissos = compromissoService.listarPorUsuario(usuarioId);
        }

        popularUsuario(model, usuarioId);
        model.addAttribute("compromissos", compromissos);
        model.addAttribute("diasCalendario", criarDiasCalendario(mesAtual, compromissos));
        model.addAttribute("mesAtual", mesAtual);
        model.addAttribute("mesAnterior", mesAtual.minusMonths(1));
        model.addAttribute("mesProximo", mesAtual.plusMonths(1));
        model.addAttribute("mesTitulo", tituloMes(mesAtual));
        model.addAttribute("totalCompromissos", compromissos.size());
        return "agenda/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                       Model model) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/agenda";
        popularUsuario(model, usuarioIdOpt.get());
        LocalDateTime inicio = data == null
                ? LocalDate.now().atTime(9, 0)
                : data.atTime(9, 0);
        if (!model.containsAttribute("compromissoForm")) {
            model.addAttribute("compromissoForm",
                    new CompromissoRequestDTO("", "", inicio, inicio.plusHours(1), ""));
        }
        model.addAttribute("modoEdicao", false);
        return "agenda/formulario";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("compromissoForm") CompromissoRequestDTO dto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/agenda";
        if (bindingResult.hasErrors()) {
            popularUsuario(model, usuarioIdOpt.get());
            model.addAttribute("modoEdicao", false);
            return "agenda/formulario";
        }
        try {
            compromissoService.criar(usuarioIdOpt.get(), dto);
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/agenda/novo";
        }
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Compromisso criado com sucesso!");
        return "redirect:/agenda";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/agenda";
        Long usuarioId = usuarioIdOpt.get();
        try {
            CompromissoResponseDTO compromisso = compromissoService.buscarPorId(usuarioId, id);
            if (!model.containsAttribute("compromissoForm")) {
                model.addAttribute("compromissoForm", new CompromissoRequestDTO(
                        compromisso.titulo(), compromisso.descricao(), compromisso.dataHoraInicio(),
                        compromisso.dataHoraFim(), compromisso.local()));
            }
            popularUsuario(model, usuarioId);
            model.addAttribute("compromissoId", id);
            model.addAttribute("modoEdicao", true);
            return "agenda/formulario";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/agenda";
        }
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("compromissoForm") CompromissoRequestDTO dto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/agenda";
        if (bindingResult.hasErrors()) {
            popularUsuario(model, usuarioIdOpt.get());
            model.addAttribute("compromissoId", id);
            model.addAttribute("modoEdicao", true);
            return "agenda/formulario";
        }
        try {
            compromissoService.atualizar(usuarioIdOpt.get(), id, dto);
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/agenda/" + id + "/editar";
        }
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Compromisso atualizado com sucesso!");
        return "redirect:/agenda";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/agenda";
        try {
            compromissoService.excluir(usuarioIdOpt.get(), id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Compromisso excluído com sucesso!");
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/agenda";
    }

    private List<Map<String, Object>> criarDiasCalendario(YearMonth mes,
                                                           List<CompromissoResponseDTO> compromissos) {
        LocalDate primeiro = mes.atDay(1);
        LocalDate inicioGrade = primeiro.minusDays(primeiro.getDayOfWeek().getValue() % 7);
        Map<LocalDate, List<CompromissoResponseDTO>> porDia = compromissos.stream()
                .collect(Collectors.groupingBy(c -> c.dataHoraInicio().toLocalDate()));
        List<Map<String, Object>> dias = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            LocalDate data = inicioGrade.plusDays(i);
            List<CompromissoResponseDTO> eventos = new ArrayList<>();
            for (CompromissoResponseDTO compromisso : compromissos) {
                LocalDate inicio = compromisso.dataHoraInicio().toLocalDate();
                LocalDate fim = compromisso.dataHoraFim() == null
                        ? inicio
                        : compromisso.dataHoraFim().toLocalDate();
                if (!data.isBefore(inicio) && !data.isAfter(fim)) eventos.add(compromisso);
            }
            Map<String, Object> dia = new HashMap<>();
            dia.put("data", data);
            dia.put("numero", data.getDayOfMonth());
            dia.put("fora", data.getMonth() != mes.getMonth());
            dia.put("hoje", data.equals(LocalDate.now()));
            dia.put("eventos", eventos);
            dias.add(dia);
        }
        return dias;
    }

    private String tituloMes(YearMonth mes) {
        String nome = mes.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"));
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1) + " de " + mes.getYear();
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
