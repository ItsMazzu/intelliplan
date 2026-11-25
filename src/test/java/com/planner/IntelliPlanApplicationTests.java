package com.planner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IntelliPlanApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void localizacaoExigeAutenticacaoECsrf() throws Exception {
		mockMvc.perform(get("/configuracoes"))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(post("/configuracoes/localizacao")
						.with(user("user"))
						.param("uf", "SP")
						.param("municipio", "Santos")
						.param("codigoIbge", "3548500"))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/configuracoes/localizacao")
						.with(csrf())
						.param("uf", "SP")
						.param("municipio", "Santos")
						.param("codigoIbge", "3548500"))
				.andExpect(status().is3xxRedirection());
	}
}
