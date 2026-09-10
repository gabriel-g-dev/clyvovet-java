package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class VacinaRequestDTO {

    @NotBlank(message = "Nome da vacina é obrigatório")
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    private String nome;

    @NotNull(message = "Data de aplicação é obrigatória")
    private LocalDate dataAplicacao;

    @NotBlank(message = "Status é obrigatório")
    @Pattern(regexp = "[AP]", message = "Status deve ser 'A' (aplicada) ou 'P' (pendente)")
    private String status;

    @Positive(message = "Intervalo de reforço deve ser maior que zero")
    private Integer intervaloReforcoDias;

    @NotNull(message = "ID do pet é obrigatório")
    private Long petId;

    public String getNome() { return nome; } public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataAplicacao() { return dataAplicacao; } public void setDataAplicacao(LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Integer getIntervaloReforcoDias() { return intervaloReforcoDias; } public void setIntervaloReforcoDias(Integer intervaloReforcoDias) { this.intervaloReforcoDias = intervaloReforcoDias; }
    public Long getPetId() { return petId; } public void setPetId(Long petId) { this.petId = petId; }
}
