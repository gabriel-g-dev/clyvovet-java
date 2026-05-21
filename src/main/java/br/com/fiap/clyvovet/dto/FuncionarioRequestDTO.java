package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class FuncionarioRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Setor é obrigatório")
    private String setor;

    @NotBlank(message = "Cargo é obrigatório")
    private String cargo;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;

    public FuncionarioRequestDTO() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSetor() { return setor; }
    public void setSetor(String setor) { this.setor = setor; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
