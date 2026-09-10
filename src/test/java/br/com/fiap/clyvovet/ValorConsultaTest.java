package br.com.fiap.clyvovet;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * O preço da consulta é definido pela clínica: o valor enviado pelo TUTOR é
 * ignorado, e o ADMIN continua informando e alterando normalmente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ValorConsultaTest {

    private static final RequestPostProcessor TUTOR =
            user("gabriel@clyvovet.com.br").authorities(new SimpleGrantedAuthority("ROLE_TUTOR"));
    private static final RequestPostProcessor ADMIN =
            user("admin@clyvovet.com.br").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));

    @Autowired
    private MockMvc mockMvc;

    private String consulta(String valor) {
        String campoValor = valor == null ? "" : "\"valor\":" + valor + ",";
        return """
                {"data":"2026-12-01T10:00:00","tipo":"Rotina","descricao":"Check-up",%s
                 "status":"A","petId":1,"funcionarioId":1}
                """.formatted(campoValor);
    }

    @Test
    @DisplayName("TUTOR agenda sem enviar valor e a consulta nasce com zero")
    void tutorSemValor() throws Exception {
        mockMvc.perform(post("/api/consultas").with(TUTOR)
                        .contentType(MediaType.APPLICATION_JSON).content(consulta(null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(0.0));
    }

    @Test
    @DisplayName("valor enviado pelo TUTOR é ignorado")
    void valorDoTutorIgnorado() throws Exception {
        mockMvc.perform(post("/api/consultas").with(TUTOR)
                        .contentType(MediaType.APPLICATION_JSON).content(consulta("999.99")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(0.0));
    }

    @Test
    @DisplayName("ADMIN informa o valor normalmente")
    void adminInformaValor() throws Exception {
        mockMvc.perform(post("/api/consultas").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(consulta("200.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(200.0));
    }

    @Test
    @DisplayName("TUTOR editando a consulta não apaga o preço definido pelo ADMIN")
    void edicaoDoTutorPreservaPreco() throws Exception {
        String resposta = mockMvc.perform(post("/api/consultas").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(consulta("200.00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(resposta, "$.id");

        mockMvc.perform(put("/api/consultas/" + id).with(TUTOR)
                        .contentType(MediaType.APPLICATION_JSON).content(consulta("1.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(200.0));

        mockMvc.perform(put("/api/consultas/" + id).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(consulta("250.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(250.0));
    }
}
