package br.com.fiap.clyvovet;

import br.com.fiap.clyvovet.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistroTutorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String corpo(String email) {
        return """
                {"nome":"Tutor Novo","email":"%s","telefone":"(11) 90000-0000","senha":"segredo123"}
                """.formatted(email);
    }

    @Test
    @DisplayName("cadastro cria o login e o tutor, e a resposta não traz a senha")
    void registroCriaLoginETutor() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo("novo@teste.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_TUTOR"))
                .andExpect(jsonPath("$.email").value("novo@teste.com"))
                .andExpect(jsonPath("$.tutorId").exists())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("a senha é gravada como hash BCrypt, nunca em texto puro")
    void senhaGravadaComBCrypt() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo("hash@teste.com")))
                .andExpect(status().isCreated());

        String armazenada = usuarioRepository.findByEmail("hash@teste.com").orElseThrow().getSenha();
        assertNotEquals("segredo123", armazenada);
        assertTrue(armazenada.startsWith("$2"));
        assertTrue(passwordEncoder.matches("segredo123", armazenada));
    }

    @Test
    @DisplayName("o cliente não escolhe o perfil: mesmo pedindo ADMIN, sai TUTOR")
    void clienteNaoEscolheOPerfil() throws Exception {
        String tentativa = """
                {"nome":"Invasor","email":"invasor@teste.com","telefone":"(11) 90000-0000",
                 "senha":"segredo123","role":"ROLE_ADMIN","perfil":"ADMIN"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(tentativa))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_TUTOR"));

        assertEquals("ROLE_TUTOR",
                usuarioRepository.findByEmail("invasor@teste.com").orElseThrow().getRole());
    }

    @Test
    @DisplayName("e-mail já cadastrado é recusado com 409")
    void emailDuplicadoRecusado() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo("duplicado@teste.com")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo("duplicado@teste.com")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("e-mail de tutor que já existe no cadastro da clínica é recusado")
    void emailDeTutorExistenteRecusado() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo("marina@clyvovet.com.br")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("campos obrigatórios são validados")
    void camposObrigatoriosValidados() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","email":"nao-e-email","telefone":"","senha":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").exists());
    }
}
