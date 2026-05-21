package br.com.fiap.clyvovet.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
public class VacinaRequestDTO {
    @NotBlank private String nome;
    @NotNull private LocalDate dataAplicacao;
    @NotBlank private String status;
    @NotNull private Long petId;
    public String getNome() { return nome; } public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataAplicacao() { return dataAplicacao; } public void setDataAplicacao(LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getPetId() { return petId; } public void setPetId(Long petId) { this.petId = petId; }
}
