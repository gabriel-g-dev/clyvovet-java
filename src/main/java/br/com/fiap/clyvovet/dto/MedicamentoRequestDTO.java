package br.com.fiap.clyvovet.dto;
import jakarta.validation.constraints.NotBlank;
public class MedicamentoRequestDTO {
    @NotBlank private String nome;
    public String getNome() { return nome; } public void setNome(String nome) { this.nome = nome; }
}
