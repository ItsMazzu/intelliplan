package com.planner.notificacao.repository;

import com.planner.notificacao.entity.Notificacao;
import com.planner.usuario.entity.Usuario;
import com.planner.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificacaoRepositoryTest {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devePersistirUmaUnicaNotificacaoPorEventoEIsolarContagens() {
        Usuario destinatario = criarUsuario();
        Usuario outroUsuario = criarUsuario();
        String chaveEvento = "LEMBRETE_TAREFA:TAREFA:999:" + UUID.randomUUID();

        int primeiraInsercao = inserir(destinatario.getId(), chaveEvento);
        int insercaoDuplicada = inserir(destinatario.getId(), chaveEvento);

        assertEquals(1, primeiraInsercao);
        assertEquals(0, insercaoDuplicada);
        assertEquals(1, notificacaoRepository.countByUsuarioIdAndLidaEmIsNull(destinatario.getId()));
        assertEquals(0, notificacaoRepository.countByUsuarioIdAndLidaEmIsNull(outroUsuario.getId()));
        assertEquals(0, notificacaoRepository.marcarTodasComoLidas(outroUsuario.getId()));
        assertEquals(1, notificacaoRepository.marcarTodasComoLidas(destinatario.getId()));
        assertEquals(0, notificacaoRepository.countByUsuarioIdAndLidaEmIsNull(destinatario.getId()));

        Notificacao persistida = notificacaoRepository
                .findTop30ByUsuarioIdOrderByCriadaEmDesc(destinatario.getId())
                .getFirst();
        assertNotNull(persistida.getLidaEm());
        assertEquals(chaveEvento, persistida.getChaveEvento());
    }

    @Test
    void deveRenderizarCentralEProtegerOperacoesDeLeituraPorUsuario() throws Exception {
        Usuario destinatario = criarUsuario();
        Usuario outroUsuario = criarUsuario();
        String chaveEvento = "ALERTA_PROJETO:PROJETO:101:" + UUID.randomUUID();
        inserir(destinatario.getId(), chaveEvento);
        Notificacao notificacao = notificacaoRepository
                .findTop30ByUsuarioIdOrderByCriadaEmDesc(destinatario.getId())
                .getFirst();

        mockMvc.perform(get("/configuracoes").with(user(destinatario.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Central de notificações")));

        mockMvc.perform(get("/api/v1/notificacoes"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/api/v1/notificacoes")
                        .with(user(destinatario.getEmail()).roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naoLidas").value(1))
                .andExpect(jsonPath("$.notificacoes[0].titulo").value("Lembrete de tarefa"));

        mockMvc.perform(patch("/api/v1/notificacoes/{id}/lida", notificacao.getId())
                        .with(user(destinatario.getEmail()).roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/notificacoes/{id}/lida", notificacao.getId())
                        .with(user(destinatario.getEmail()).roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/notificacoes/{id}/lida", notificacao.getId())
                        .with(user(outroUsuario.getEmail()).roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        inserir(destinatario.getId(), "LEMBRETE_TAREFA:TAREFA:1000:" + UUID.randomUUID());
        mockMvc.perform(patch("/api/v1/notificacoes/lidas")
                        .with(user(destinatario.getEmail()).roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertEquals(0, notificacaoRepository.countByUsuarioIdAndLidaEmIsNull(destinatario.getId()));
        assertEquals(0, notificacaoRepository.countByUsuarioIdAndLidaEmIsNull(outroUsuario.getId()));
    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario();
        String identificador = UUID.randomUUID().toString();
        usuario.setNome("Conta de teste");
        usuario.setEmail(identificador + "@example.test");
        usuario.setSenha("hash-de-teste");
        return usuarioRepository.saveAndFlush(usuario);
    }

    private int inserir(Long usuarioId, String chaveEvento) {
        return notificacaoRepository.inserirSeNovoEvento(
                "Lembrete de tarefa",
                "A tarefa vence em breve.",
                Notificacao.Tipo.LEMBRETE_TAREFA.name(),
                chaveEvento,
                "TAREFA",
                999L,
                "/tarefas",
                usuarioId
        );
    }
}
