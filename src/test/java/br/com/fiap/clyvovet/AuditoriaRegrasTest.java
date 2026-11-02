package br.com.fiap.clyvovet;

import br.com.fiap.clyvovet.service.PainelService;
import br.com.fiap.clyvovet.service.ConsultaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuditoriaRegrasTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private PainelService painel;
    @Autowired private ConsultaService consultas;
    @Autowired private EntityManager entityManager;

    private String consulta(String status, LocalDateTime data, String valor) throws Exception {
        return mapper.writeValueAsString(java.util.Map.of(
                "data", data.toString(), "tipo", "Rotina", "descricao", "Check-up",
                "valor", new java.math.BigDecimal(valor), "status", status, "petId", 1, "funcionarioId", 1));
    }

    private long criarPetSemHistorico() throws Exception {
        String resposta = mvc.perform(post("/api/pets").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Pet sem histórico","especie":"Gato","raca":"SRD","peso":4.50,
                 "dataNascimento":"2024-01-01","tutorId":1}
                """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(resposta).path("id").asLong();
    }

    @Test
    @WithMockUser(username = "gabriel@clyvovet.com.br", roles = "TUTOR")
    void tutorNaoRegistraRealizacaoDeConsulta() throws Exception {
        mvc.perform(post("/api/consultas").contentType(MediaType.APPLICATION_JSON)
                        .content(consulta("R", LocalDateTime.now().minusDays(1), "0.00")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/consultas/2").contentType(MediaType.APPLICATION_JSON)
                        .content(consulta("R", LocalDateTime.now().minusDays(1), "0.00")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gabriel@clyvovet.com.br", roles = "TUTOR")
    void tutorNaoAlteraNemExcluiConsultaRealizada() throws Exception {
        mvc.perform(put("/api/consultas/1").contentType(MediaType.APPLICATION_JSON)
                        .content(consulta("A", LocalDateTime.now().plusDays(1), "0.00")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/consultas/1")).andExpect(status().isForbidden());
        mvc.perform(get("/api/consultas/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("R"));
    }

    @Test
    @WithMockUser(username = "gabriel@clyvovet.com.br", roles = "TUTOR")
    void tutorPreservaHistoricoClinicoAoExcluirPet() throws Exception {
        mvc.perform(delete("/api/pets/1")).andExpect(status().isForbidden());
        mvc.perform(get("/api/pets/1")).andExpect(status().isOk());
        long id = criarPetSemHistorico();
        mvc.perform(delete("/api/pets/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/pets/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "gabriel@clyvovet.com.br", roles = "TUTOR")
    void petComApenasVacinaAplicadaTambemPreservaHistorico() throws Exception {
        long id = criarPetSemHistorico();
        mvc.perform(post("/api/vacinas").with(user("admin@clyvovet.com.br").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Dose histórica","status":"A","dataAplicacao":"2025-01-01","petId":%d}
                """.formatted(id)))
                .andExpect(status().isCreated());
        mvc.perform(delete("/api/pets/" + id)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/pets/" + id).with(user("admin@clyvovet.com.br").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void edicaoDeVacinaNaoBurlaBaixaNemPermiteReaplicacao() throws Exception {
        mvc.perform(put("/api/vacinas/1").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"V10 (Polivalente)","status":"P","dataAplicacao":"2025-01-01","petId":1}
                """))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/vacinas/2").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Antirrabica","status":"A","dataAplicacao":"2025-01-01","petId":1}
                """))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/vacinas/2").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Antirrabica atualizada","status":"P","dataAplicacao":"2025-01-01","petId":1}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Antirrabica atualizada"));
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void adminRegistraRealizacaoMasNaoNoFuturo() throws Exception {
        mvc.perform(post("/api/consultas").contentType(MediaType.APPLICATION_JSON)
                        .content(consulta("R", LocalDateTime.now().minusDays(1), "150.50")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("R"));
        mvc.perform(post("/api/consultas").contentType(MediaType.APPLICATION_JSON)
                        .content(consulta("R", LocalDateTime.now().plusDays(1), "150.50")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.dataCoerenteComStatus").exists());
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void precisaoDecimalImpedeArredondamentoSilencioso() throws Exception {
        mvc.perform(post("/api/pets").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Peso inválido","especie":"Gato","raca":"SRD","peso":0.001,
                 "dataNascimento":"2024-01-01","tutorId":1}
                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.peso").exists());
        mvc.perform(post("/api/consultas").contentType(MediaType.APPLICATION_JSON)
                        .content(consulta("A", LocalDateTime.now().plusDays(1), "1.999")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.valor").exists());
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void periodoInvertidoRetornaConflitoExplicado() throws Exception {
        mvc.perform(get("/api/consultas/periodo").param("inicio", "2026-12-01T10:00:00")
                        .param("fim", "2026-11-01T10:00:00"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("O início do período não pode ser posterior ao fim"));
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void exclusaoDeTutorNaoDeixaLoginOrfao() throws Exception {
        mvc.perform(delete("/api/tutores/1")).andExpect(status().isConflict());
        mvc.perform(get("/api/tutores/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.pets[0].nome").value("Rex"));
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void painelCarregaTutoresEVacinasEmUmaConsulta() {
        entityManager.flush();
        entityManager.clear();
        Statistics statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        boolean habilitada = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        try {
            var pets = painel.listarPets();
            assertFalse(pets.isEmpty());
            assertTrue(pets.stream().anyMatch(pet -> !pet.getVacinas().isEmpty()));
            assertTrue(pets.stream().allMatch(pet -> pet.getTutorNome() != null));
            assertEquals(1, statistics.getPrepareStatementCount(), "não deve haver uma consulta extra por pet");
        } finally {
            statistics.setStatisticsEnabled(habilitada);
        }
    }

    @Test
    @WithMockUser(username = "admin@clyvovet.com.br", roles = "ADMIN")
    void consultasPaginadasNaoFazemBuscaExtraPorPetOuVeterinario() {
        entityManager.flush();
        entityManager.clear();
        Statistics statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        boolean habilitada = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        try {
            var pagina = consultas.listar(PageRequest.of(0, 2));
            assertEquals(2, pagina.getNumberOfElements());
            assertTrue(pagina.getContent().stream().allMatch(consulta -> consulta.getPetNome() != null
                    && consulta.getFuncionarioNome() != null));
            assertEquals(2, statistics.getPrepareStatementCount(), "somente a página e a contagem total são necessárias");
        } finally {
            statistics.setStatisticsEnabled(habilitada);
        }
    }
}
