package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class VacinaRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "Data de aplicação é obrigatória")
    private LocalDate dataAplicacao;

    private LocalDate proximaDose;

    private boolean aplicada;

    @NotNull(message = "ID do pet é obrigatório")
    private Long petId;

    public VacinaRequestDTO() {}

    public VacinaRequestDTO(String nome, LocalDate dataAplicacao, LocalDate proximaDose, boolean aplicada, Long petId) {
        this.nome = nome;
        this.dataAplicacao = dataAplicacao;
        this.proximaDose = proximaDose;
        this.aplicada = aplicada;
        this.petId = petId;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public void setDataAplicacao(LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }
    public LocalDate getProximaDose() { return proximaDose; }
    public void setProximaDose(LocalDate proximaDose) { this.proximaDose = proximaDose; }
    public boolean isAplicada() { return aplicada; }
    public void setAplicada(boolean aplicada) { this.aplicada = aplicada; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
}
