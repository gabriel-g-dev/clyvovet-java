package br.com.fiap.clyvovet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prova que os dois perfis têm permissões diferentes de verdade, e que o TUTOR
 * não alcança o dado de outro tutor.
 *
 * gabriel@clyvovet.com.br é o tutor do Rex (pet 1) na carga inicial;
 * marina@clyvovet.com.br é a tutora da Mel (pet 2).
 */
@SpringBootTest
@AutoConfigureMockMvc
class PerfilDeAcessoTest {

    private static final String TUTOR_GABRIEL = "gabriel@clyvovet.com.br";
    private static final String TUTOR_MARINA = "marina@clyvovet.com.br";

    private static final String PET_JSON = """
            {"nome":"Teste","dataNascimento":"2024-01-10","peso":5.0,
             "especie":"Cachorro","raca":"SRD","tutorId":1}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("sem token, a API responde 401")
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/pets")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TUTOR faz CRUD de pet")
    @WithMockUser(username = TUTOR_GABRIEL, authorities = "ROLE_TUTOR")
    void tutorFazCrudDePet() throws Exception {
        mockMvc.perform(get("/api/pets")).andExpect(status().isOk());
        mockMvc.perform(post("/api/pets").contentType(MediaType.APPLICATION_JSON).content(PET_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("TUTOR agenda consulta para o próprio pet")
    @WithMockUser(username = TUTOR_GABRIEL, authorities = "ROLE_TUTOR")
    void tutorAgendaConsulta() throws Exception {
        String consulta = """
                {"data":"2026-12-01T10:00:00","tipo":"Rotina","descricao":"Check-up",
                 "valor":150.00,"status":"A","petId":1,"funcionarioId":1}
                """;
        mockMvc.perform(post("/api/consultas").contentType(MediaType.APPLICATION_JSON).content(consulta))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("TUTOR não alcança o pet de outro tutor")
    @WithMockUser(username = TUTOR_MARINA, authorities = "ROLE_TUTOR")
    void tutorNaoAlcancaPetDeOutro() throws Exception {
        mockMvc.perform(get("/api/pets/1")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/pets/1/inteligencia-preventiva")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/pets/1").contentType(MediaType.APPLICATION_JSON).content(PET_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("a listagem do TUTOR traz apenas os pets dele")
    @WithMockUser(username = TUTOR_MARINA, authorities = "ROLE_TUTOR")
    void listagemDoTutorEhFiltrada() throws Exception {
        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].nome").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.not("Rex"))));
    }

    @Test
    @DisplayName("TUTOR não registra vacina nem dá baixa: isso é ato da clínica")
    @WithMockUser(username = TUTOR_GABRIEL, authorities = "ROLE_TUTOR")
    void tutorNaoMexeNoCalendarioVacinal() throws Exception {
        mockMvc.perform(post("/api/vacinas/3/aplicar").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/vacinas").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Teste","dataAplicacao":"2026-10-01","status":"P","petId":1}
                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TUTOR não enxerga medicamentos nem o cadastro de tutores")
    @WithMockUser(username = TUTOR_GABRIEL, authorities = "ROLE_TUTOR")
    void tutorNaoVeCadastrosInternos() throws Exception {
        mockMvc.perform(get("/api/medicamentos")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/tutores")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TUTOR lê a equipe para escolher o veterinário, mas não a altera")
    @WithMockUser(username = TUTOR_GABRIEL, authorities = "ROLE_TUTOR")
    void tutorLeEquipeMasNaoAltera() throws Exception {
        mockMvc.perform(get("/api/funcionarios")).andExpect(status().isOk());
        mockMvc.perform(post("/api/funcionarios").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"X","setor":"Y","cargo":"Z","email":"x@y.com","telefone":"1199"}
                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN alcança qualquer pet e os cadastros internos")
    @WithMockUser(username = "admin@clyvovet.com.br", authorities = "ROLE_ADMIN")
    void adminTemAcessoTotal() throws Exception {
        mockMvc.perform(get("/api/pets/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/pets/2")).andExpect(status().isOk());
        mockMvc.perform(get("/api/medicamentos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/tutores")).andExpect(status().isOk());
        mockMvc.perform(post("/api/pets").contentType(MediaType.APPLICATION_JSON).content(PET_JSON))
                .andExpect(status().isCreated());
    }
}
