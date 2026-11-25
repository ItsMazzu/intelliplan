package com.planner.tarefa.controller;

import com.planner.common.context.UsuarioContextService;
import com.planner.projeto.service.ProjetoService;
import com.planner.tarefa.dto.TarefaRequestDTO;
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

class TarefaViewControllerDateBindingTest {

    @Test
    void devePreservarDataDeVencimentoAoEditar() throws Exception {
        TarefaService tarefaService = mock(TarefaService.class);
        UsuarioContextService usuarioContextService = mock(UsuarioContextService.class);
        when(usuarioContextService.obterUsuarioIdOpcional()).thenReturn(Optional.of(7L));
        TarefaViewController controller = new TarefaViewController(
                tarefaService,
                mock(ProjetoService.class),
                mock(UsuarioService.class),
                usuarioContextService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/tarefas/42/editar")
                        .param("titulo", "Entregar relatório")
                        .param("status", "PENDENTE")
                        .param("prioridade", "MEDIA")
                        .param("dataVencimento", "2026-11-25"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tarefas"));

        var captor = org.mockito.ArgumentCaptor.forClass(TarefaRequestDTO.class);
        verify(tarefaService).atualizar(eq(7L), eq(42L), captor.capture());
        assertEquals(LocalDate.of(2026, 11, 25), captor.getValue().dataVencimento());
    }
}
