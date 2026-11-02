package br.com.fiap.clyvovet;

import br.com.fiap.clyvovet.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("demo")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:publicacaotest;DB_CLOSE_DELAY=-1",
        // Chave exclusiva dos testes; as contas vêm da carga fictícia, sem provisionamento privado.
        "CLYVOVET_JWT_SECRET=bm90LWEtcHJvZHVjdGlvbi1zZWNyZXQtMzItYnl0ZXM=",
        "CLYVOVET_COOKIE_SECURE=false"
})
@AutoConfigureMockMvc
class ContasPublicacaoTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void contasDeDemonstracaoAutenticamNaApiENaWebSemVariaveisPrivadas() throws Exception {
        for (String[] conta : new String[][] {
                {"admin@clyvovet.com.br", "admin123", "ADMIN"},
                {"gabriel@clyvovet.com.br", "tutor123", "TUTOR"}}) {
            mvc.perform(post("/api/auth/login").contentType("application/json")
                    .content(mapper.writeValueAsString(Map.of("email", conta[0], "senha", conta[1]))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("role").value("ROLE_" + conta[2]))
                    .andExpect(jsonPath("token").isNotEmpty());
            mvc.perform(post("/login").with(csrf()).param("username", conta[0]).param("password", conta[1]))
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(authenticated().withUsername(conta[0]).withRoles(conta[2]));
        }
    }

    @Test
    void tutorContinuaRestritoAosPropriosPetsEAosAtosPermitidos() throws Exception {
        String corpo = mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"email\":\"gabriel@clyvovet.com.br\",\"senha\":\"tutor123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String autorizacao = "Bearer " + mapper.readTree(corpo).get("token").asText();
        mvc.perform(get("/api/pets/1").header("Authorization", autorizacao))
                .andExpect(status().isOk()).andExpect(jsonPath("nome").value("Rex"));
        mvc.perform(get("/api/pets/2").header("Authorization", autorizacao))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/tutores").header("Authorization", autorizacao))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/vacinas/1/aplicar").header("Authorization", autorizacao)
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cadastroNaoSobrescreveAsContasDaDemonstracao() throws Exception {
        for (String email : new String[] {"admin@clyvovet.com.br", "gabriel@clyvovet.com.br"}) {
            mvc.perform(post("/api/auth/register").contentType("application/json").content(
                    mapper.writeValueAsString(Map.of("nome", "Tentativa", "email", email,
                            "telefone", "11999999999", "senha", "senha-fixture-2026"))))
                    .andExpect(status().isConflict());
        }
    }

    @Test
    void publicacaoPreservaRestricoesDeCorsConsoleEConteudoDoLogin() throws Exception {
        mvc.perform(options("/api/pets").header("Origin", "http://localhost:19006")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        mvc.perform(get("/h2-console/")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("admin@clyvovet.com.br"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("gabriel@clyvovet.com.br"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("admin123"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("tutor123"))));
    }

    @Test
    void perfisPublicadosContinuamRecusandoAChaveJwtDeDesenvolvimento() {
        for (String perfil : new String[] {"demo", "persistente"}) {
            var ambiente = new MockEnvironment();
            ambiente.setActiveProfiles(perfil);
            assertThatThrownBy(() -> new TokenService(
                    "3ktdFCeznoWf+3hS0xCyRvp9RLw7QzZ17zz3f7zaZOM=", "clyvovet", 60, ambiente))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("chave JWT própria");
        }
    }
}
