package com.planner.estudos.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.estudos.dto.ConteudoEstudoRequestDTO;
import com.planner.estudos.dto.ConteudoEstudoResponseDTO;
import com.planner.estudos.dto.MateriaRequestDTO;
import com.planner.estudos.dto.MateriaResponseDTO;
import com.planner.estudos.entity.ConteudoEstudo;
import com.planner.estudos.service.ConteudoEstudoService;
import com.planner.estudos.service.MateriaService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/estudos")
public class EstudoViewController {

    private final MateriaService materiaService;
    private final ConteudoEstudoService conteudoEstudoService;
    private final UsuarioService usuarioService;
    private final UsuarioContextService usuarioContextService;

    public EstudoViewController(MateriaService materiaService,
                                ConteudoEstudoService conteudoEstudoService,
                                UsuarioService usuarioService,
                                UsuarioContextService usuarioContextService) {
        this.materiaService = materiaService;
        this.conteudoEstudoService = conteudoEstudoService;
        this.usuarioService = usuarioService;
        this.usuarioContextService = usuarioContextService;
    }

    @GetMapping
    public String listar(Model model) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) {
            popularUsuarioPadrao(model);
            model.addAttribute("materias", Collections.emptyList());
            model.addAttribute("materiasProgresso", Collections.emptyList());
            return "estudos/lista";
        }

        Long usuarioId = usuarioIdOpt.get();
        List<MateriaResponseDTO> materias = materiaService.listarPorUsuario(usuarioId);
        List<Map<String, Object>> materiasProgresso = new ArrayList<>();
        int concluidos = 0;
        int total = 0;
        for (MateriaResponseDTO materia : materias) {
            List<ConteudoEstudoResponseDTO> conteudos = conteudoEstudoService.listarPorMateria(usuarioId, materia.id());
            long materiaConcluidos = conteudos.stream()
                    .filter(c -> c.status() == ConteudoEstudo.Status.CONCLUIDO).count();
            int progresso = conteudos.isEmpty() ? 0 : (int) ((materiaConcluidos * 100) / conteudos.size());
            Map<String, Object> item = new HashMap<>();
            item.put("materia", materia);
            item.put("conteudos", conteudos);
            item.put("progresso", progresso);
            materiasProgresso.add(item);
            concluidos += (int) materiaConcluidos;
            total += conteudos.size();
        }

        popularUsuario(model, usuarioId);
        model.addAttribute("materias", materias);
        model.addAttribute("materiasProgresso", materiasProgresso);
        model.addAttribute("totalMaterias", materias.size());
        model.addAttribute("totalConteudos", total);
        model.addAttribute("conteudosConcluidos", concluidos);
        model.addAttribute("progressoGeral", total == 0 ? 0 : (concluidos * 100) / total);
        return "estudos/lista";
    }

    @GetMapping("/nova")
    public String novaMateria(Model model) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/estudos";
        popularUsuario(model, usuarioIdOpt.get());
        if (!model.containsAttribute("materiaForm")) {
            model.addAttribute("materiaForm", new MateriaRequestDTO("", ""));
        }
        model.addAttribute("modoEdicao", false);
        return "estudos/formulario-materia";
    }

    @PostMapping
    public String salvarMateria(@Valid @ModelAttribute("materiaForm") MateriaRequestDTO dto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/estudos";
        if (bindingResult.hasErrors()) {
            popularUsuario(model, usuarioIdOpt.get());
            model.addAttribute("modoEdicao", false);
            return "estudos/formulario-materia";
        }
        materiaService.criar(usuarioIdOpt.get(), dto);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Matéria criada com sucesso!");
        return "redirect:/estudos";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/estudos";
        Long usuarioId = usuarioIdOpt.get();
        try {
            MateriaResponseDTO materia = materiaService.buscarPorId(usuarioId, id);
            List<ConteudoEstudoResponseDTO> conteudos = conteudoEstudoService.listarPorMateria(usuarioId, id);
            long concluidos = conteudos.stream().filter(c -> c.status() == ConteudoEstudo.Status.CONCLUIDO).count();
            int progresso = conteudos.isEmpty() ? 0 : (int) ((concluidos * 100) / conteudos.size());
            popularUsuario(model, usuarioId);
            model.addAttribute("materia", materia);
            model.addAttribute("conteudos", conteudos);
            model.addAttribute("progressoMateria", progresso);
            model.addAttribute("statusOpcoes", ConteudoEstudo.Status.values());
            return "estudos/detalhes";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/estudos";
        }
    }

    @GetMapping("/{id}/editar")
    public String editarMateria(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/estudos";
        Long usuarioId = usuarioIdOpt.get();
        try {
            MateriaResponseDTO materia = materiaService.buscarPorId(usuarioId, id);
            if (!model.containsAttribute("materiaForm")) {
                model.addAttribute("materiaForm", new MateriaRequestDTO(materia.nome(), materia.descricao()));
            }
            popularUsuario(model, usuarioId);
            model.addAttribute("materiaId", id);
            model.addAttribute("modoEdicao", true);
            return "estudos/formulario-materia";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/estudos";
        }
    }

    @PostMapping("/{id}/editar")
    public String atualizarMateria(@PathVariable Long id,
                                   @Valid @ModelAttribute("materiaForm") MateriaRequestDTO dto,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/estudos";
        if (bindingResult.hasErrors()) {
            popularUsuario(model, usuarioIdOpt.get());
            model.addAttribute("materiaId", id);
            model.addAttribute("modoEdicao", true);
            return "estudos/formulario-materia";
        }
        try {
            materiaService.atualizar(usuarioIdOpt.get(), id, dto);
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/estudos";
        }
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Matéria atualizada com sucesso!");
        return "redirect:/estudos";
    }

    @PostMapping("/{id}/excluir")
    public String excluirMateria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/estudos";
        try {
            materiaService.excluir(usuarioIdOpt.get(), id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Matéria excluída com sucesso!");
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/estudos";
    }

    @GetMapping("/{materiaId}/conteudos/novo")
    public String novoConteudo(@PathVariable Long materiaId, Model model, RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/estudos";
        try {
            MateriaResponseDTO materia = materiaService.buscarPorId(usuarioIdOpt.get(), materiaId);
            popularUsuario(model, usuarioIdOpt.get());
            if (!model.containsAttribute("conteudoForm")) {
                model.addAttribute("conteudoForm",
                        new ConteudoEstudoRequestDTO("", "", ConteudoEstudo.Status.NAO_INICIADO, materiaId));
            }
            model.addAttribute("materia", materia);
            model.addAttribute("materiaId", materiaId);
            model.addAttribute("statusOpcoes", ConteudoEstudo.Status.values());
            model.addAttribute("modoEdicao", false);
            return "estudos/formulario-conteudo";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/estudos";
        }
    }

    @PostMapping("/{materiaId}/conteudos")
    public String salvarConteudo(@PathVariable Long materiaId,
                                 @Valid @ModelAttribute("conteudoForm") ConteudoEstudoRequestDTO dto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/estudos";
        if (bindingResult.hasErrors()) {
            popularUsuario(model, usuarioIdOpt.get());
            model.addAttribute("materia", materiaService.buscarPorId(usuarioIdOpt.get(), materiaId));
            model.addAttribute("materiaId", materiaId);
            model.addAttribute("statusOpcoes", ConteudoEstudo.Status.values());
            model.addAttribute("modoEdicao", false);
            return "estudos/formulario-conteudo";
        }
        conteudoEstudoService.criar(usuarioIdOpt.get(), dto);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Conteúdo criado com sucesso!");
        return "redirect:/estudos/" + materiaId;
    }

    @GetMapping("/{materiaId}/conteudos/{conteudoId}/editar")
    public String editarConteudo(@PathVariable Long materiaId,
                                 @PathVariable Long conteudoId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/estudos";
        Long usuarioId = usuarioIdOpt.get();
        try {
            MateriaResponseDTO materia = materiaService.buscarPorId(usuarioId, materiaId);
            ConteudoEstudoResponseDTO conteudo = conteudoEstudoService.buscarPorId(usuarioId, conteudoId);
            if (!model.containsAttribute("conteudoForm")) {
                model.addAttribute("conteudoForm", new ConteudoEstudoRequestDTO(
                        conteudo.titulo(), conteudo.descricao(), conteudo.status(), materiaId));
            }
            popularUsuario(model, usuarioId);
            model.addAttribute("materia", materia);
            model.addAttribute("materiaId", materiaId);
            model.addAttribute("conteudoId", conteudoId);
            model.addAttribute("statusOpcoes", ConteudoEstudo.Status.values());
            model.addAttribute("modoEdicao", true);
            return "estudos/formulario-conteudo";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/estudos/" + materiaId;
        }
    }

    @PostMapping("/{materiaId}/conteudos/{conteudoId}/editar")
    public String atualizarConteudo(@PathVariable Long materiaId,
                                    @PathVariable Long conteudoId,
                                    @Valid @ModelAttribute("conteudoForm") ConteudoEstudoRequestDTO dto,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/estudos";
        if (bindingResult.hasErrors()) {
            popularUsuario(model, usuarioIdOpt.get());
            model.addAttribute("materia", materiaService.buscarPorId(usuarioIdOpt.get(), materiaId));
            model.addAttribute("materiaId", materiaId);
            model.addAttribute("conteudoId", conteudoId);
            model.addAttribute("statusOpcoes", ConteudoEstudo.Status.values());
            model.addAttribute("modoEdicao", true);
            return "estudos/formulario-conteudo";
        }
        try {
            conteudoEstudoService.atualizar(usuarioIdOpt.get(), conteudoId, dto);
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/estudos/" + materiaId;
        }
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Conteúdo atualizado com sucesso!");
        return "redirect:/estudos/" + materiaId;
    }

    @PostMapping("/{materiaId}/conteudos/{conteudoId}/excluir")
    public String excluirConteudo(@PathVariable Long materiaId,
                                  @PathVariable Long conteudoId,
                                  RedirectAttributes redirectAttributes) {
        var usuarioIdOpt = usuarioContextService.obterUsuarioIdOpcional();
        if (usuarioIdOpt.isEmpty()) return "redirect:/estudos";
        try {
            conteudoEstudoService.excluir(usuarioIdOpt.get(), conteudoId);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Conteúdo excluído com sucesso!");
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/estudos/" + materiaId;
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
