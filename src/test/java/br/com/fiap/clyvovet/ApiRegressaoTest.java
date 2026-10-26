package br.com.fiap.clyvovet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiRegressaoTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void loginJwtRealAutorizaPetDoTutorERejeitaPetDeOutro() throws Exception {
        String resposta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"gabriel@clyvovet.com.br\",\"senha\":\"tutor123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(resposta).path("token").asText();

        mvc.perform(get("/api/pets/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Rex"));
        mvc.perform(get("/api/pets/2").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void apiNaoAceitaBasicETokenInvalidoNaoAutentica() throws Exception {
        mvc.perform(get("/api/pets").with(httpBasic("admin@clyvovet.com.br", "admin123")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void atualizacaoDeTutorPreservaPetsNaResposta() throws Exception {
        mvc.perform(put("/api/tutores/1").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Gabriel Atualizado","email":"GABRIEL@CLYVOVET.COM.BR","telefone":"11999999999"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("gabriel@clyvovet.com.br"))
                .andExpect(jsonPath("$.pets[0].nome").value("Rex"));
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void emailDeTutorComLoginNaoPodeSerDesvinculado() throws Exception {
        mvc.perform(put("/api/tutores/1").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Gabriel","email":"outro@teste.com","telefone":"11999999999"}
                """))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void naoRegistraVacinaAplicadaNoFuturo() throws Exception {
        mvc.perform(post("/api/vacinas").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Teste","dataAplicacao":"%s","status":"A","petId":1}
                """.formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "gabriel@clyvovet.com.br", roles = "TUTOR")
    void tutorNaoAcessaFormulariosClinicos() throws Exception {
        mvc.perform(get("/pets/1/editar")).andExpect(status().isForbidden());
        mvc.perform(get("/pets/1/vacinas/novo")).andExpect(status().isForbidden());
    }
}
