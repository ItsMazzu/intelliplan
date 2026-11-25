package com.planner.projeto.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.projeto.dto.ProjetoRequestDTO;
import com.planner.projeto.service.ProjetoService;
import com.planner.tarefa.service.TarefaService;
import com.planner.usuario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjetoViewControllerDateBindingTest {

    @Test
    void devePreservarDatasDoProjetoAoEditar() throws Exception {
        ProjetoService projetoService = mock(ProjetoService.class);
        UsuarioContextService usuarioContextService = mock(UsuarioContextService.class);
        when(usuarioContextService.obterUsuarioIdOpcional()).thenReturn(Optional.of(7L));
        ProjetoViewController controller = new ProjetoViewController(
                projetoService,
                mock(TarefaService.class),
                mock(UsuarioService.class),
                usuarioContextService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/projetos/42/editar")
                        .param("nome", "Reforma")
                        .param("status", "PLANEJADO")
                        .param("dataInicio", "2026-11-25")
                        .param("dataConclusao", "2026-12-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projetos"));

        var captor = org.mockito.ArgumentCaptor.forClass(ProjetoRequestDTO.class);
        verify(projetoService).atualizar(eq(7L), eq(42L), captor.capture());
        assertEquals(LocalDate.of(2026, 11, 25), captor.getValue().dataInicio());
        assertEquals(LocalDate.of(2026, 12, 10), captor.getValue().dataConclusao());
    }
}
