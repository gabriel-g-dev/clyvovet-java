package br.com.fiap.clyvovet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.h2.console.enabled=false")
@AutoConfigureMockMvc
class ConsoleDesabilitadoTest {

    @Autowired private MockMvc mvc;

    @Test
    void paginaPublicaContinuaDisponivelSemAutoConfiguracaoDoConsole() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/health")).andExpect(status().isOk());
        mvc.perform(get("/h2-console/")).andExpect(status().is3xxRedirection());
    }
}
