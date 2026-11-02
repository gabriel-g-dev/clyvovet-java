package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.nio.charset.StandardCharsets;

/** O perfil não vem do cliente: todo cadastro por aqui é ROLE_TUTOR. */
public class RegistroRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 50, message = "Email deve ter no máximo 50 caracteres")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 12, max = 60, message = "Senha deve ter entre 12 e 60 caracteres")
    private String senha;

    public RegistroRequestDTO() {}

    @JsonIgnore
    @AssertTrue(message = "Senha deve ter no máximo 72 bytes em UTF-8")
    public boolean isSenhaCompativelComBCrypt() {
        return senha == null || senha.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
