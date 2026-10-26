package br.com.fiap.clyvovet;

import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.VacinaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.cache.CacheManager;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WebFluxosTest {

    @Autowired private MockMvc mvc;
    @Autowired private VacinaRepository vacinaRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private CacheManager cacheManager;

    @AfterEach
    void limparResultadosDaTransacaoDeTeste() {
        cacheManager.getCacheNames().forEach(nome -> cacheManager.getCache(nome).clear());
    }

    @Test
    void adminCriaPetAgendaAplicaVacinaERecebeReforcoPeloPainel() throws Exception {
        MockHttpSession sessao = (MockHttpSession) mvc.perform(post("/login").with(csrf())
                        .param("username", "admin@clyvovet.com.br").param("password", "admin123"))
                .andExpect(redirectedUrl("/dashboard")).andReturn().getRequest().getSession(false);
        assertNotNull(sessao);

        String ficha = mvc.perform(post("/pets/salvar").session(sessao).with(csrf())
                        .param("nome", "Nina Web").param("especie", "Gato").param("raca", "SRD")
                        .param("peso", "4.5").param("dataNascimento", "2024-01-01").param("tutorId", "1"))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        assertNotNull(ficha);
        Long petId = Long.valueOf(ficha.substring(ficha.lastIndexOf('/') + 1));
        entityManager.flush();
        entityManager.clear();

        mvc.perform(get(ficha).session(sessao)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Nina Web")))
                .andExpect(content().string(containsString("Nenhuma vacina registrada")));
        mvc.perform(get(ficha + "/vacinas/novo").session(sessao)).andExpect(status().isOk());
        mvc.perform(post(ficha + "/vacinas/salvar").session(sessao).with(csrf())
                        .param("nome", "Antirrábica Web").param("dataAplicacao", LocalDate.now().minusDays(1).toString())
                        .param("intervaloReforcoDias", "30").param("status", "P").param("petId", "1"))
                .andExpect(redirectedUrl(ficha));
        entityManager.flush();
        entityManager.clear();
        List<Vacina> doses = vacinaRepository.findByPetId(petId);
        assertEquals(1, doses.size(), "o agendamento deve usar o pet da URL, mesmo com ID diferente no formulário");

        mvc.perform(post("/vacinas/" + doses.get(0).getId() + "/aplicar").session(sessao).with(csrf()))
                .andExpect(redirectedUrl(ficha));
        entityManager.flush();
        entityManager.clear();
        List<Vacina> resultado = vacinaRepository.findByPetId(petId);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().anyMatch(v -> "A".equals(v.getStatus()) && LocalDate.now().equals(v.getData())));
        assertTrue(resultado.stream().anyMatch(v -> "P".equals(v.getStatus()) && LocalDate.now().plusDays(30).equals(v.getData())));
        mvc.perform(get(ficha).session(sessao)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Antirrábica Web")));
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void edicaoMostraErrosEPermiteAtualizarCadastro() throws Exception {
        mvc.perform(get("/pets/1/editar")).andExpect(status().isOk()).andExpect(view().name("pet-form"));
        mvc.perform(post("/pets/1/atualizar").with(csrf())
                        .param("nome", "").param("especie", "Cachorro").param("raca", "SRD")
                        .param("peso", "-1").param("dataNascimento", "2024-01-01").param("tutorId", "1"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("pet", "nome", "peso"));
        mvc.perform(post("/pets/1/atualizar").with(csrf())
                        .param("nome", "Rex Atualizado").param("especie", "Cachorro").param("raca", "SRD")
                        .param("peso", "29.5").param("dataNascimento", "2024-01-01").param("tutorId", "1"))
                .andExpect(redirectedUrl("/pets/1"));
        mvc.perform(get("/pets/1")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Rex Atualizado")))
                .andExpect(content().string(containsString("Consultas do pet")));
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void vacinaInvalidaRetornaFormularioComFeedback() throws Exception {
        mvc.perform(post("/pets/1/vacinas/salvar").with(csrf()).param("nome", "")
                        .param("dataAplicacao", "invalida").param("intervaloReforcoDias", "0")
                        .param("status", "P").param("petId", "1"))
                .andExpect(status().isOk()).andExpect(view().name("vacina-form"))
                .andExpect(model().attributeHasFieldErrors("vacina", "nome", "dataAplicacao", "intervaloReforcoDias"));
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void fichaInexistenteRetorna404EmHtml() throws Exception {
        mvc.perform(get("/pets/999999")).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML)).andExpect(view().name("erro"));
    }

    @Test
    @WithMockUser(username = "gabriel@clyvovet.com.br", roles = "TUTOR")
    void fichaDeOutroTutorRetorna403EmHtml() throws Exception {
        mvc.perform(get("/pets/2")).andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML)).andExpect(view().name("erro"));
    }
}
