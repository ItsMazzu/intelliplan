package com.planner.agenda.controller;

import com.planner.agenda.dto.CompromissoRequestDTO;
import com.planner.agenda.service.CompromissoService;
import com.planner.common.context.UsuarioContextService;
import com.planner.integracao.feriados.service.FeriadoService;
import com.planner.usuario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgendaViewControllerDateBindingTest {

    @Test
    void devePreservarDataHoraDoCompromissoAoEditar() throws Exception {
        CompromissoService compromissoService = mock(CompromissoService.class);
        UsuarioContextService usuarioContextService = mock(UsuarioContextService.class);
        when(usuarioContextService.obterUsuarioIdOpcional()).thenReturn(Optional.of(7L));
        AgendaViewController controller = new AgendaViewController(
                compromissoService,
                mock(UsuarioService.class),
                usuarioContextService,
                mock(FeriadoService.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/agenda/42/editar")
                        .param("titulo", "Consulta")
                        .param("dataHoraInicio", "2026-11-25T14:30:45")
                        .param("dataHoraFim", "2026-11-25T15:15:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agenda"));

        var captor = forClass(CompromissoRequestDTO.class);
        verify(compromissoService).atualizar(eq(7L), eq(42L), captor.capture());
        assertEquals(LocalDateTime.of(2026, 11, 25, 14, 30, 45), captor.getValue().dataHoraInicio());
        assertEquals(LocalDateTime.of(2026, 11, 25, 15, 15, 30), captor.getValue().dataHoraFim());
    }
}
