package br.com.fiap.clyvovet.dto;

import java.time.LocalDate;

public class VacinaResponseDTO {

    private Long id;
    private String nome;
    private LocalDate dataAplicacao;
    private LocalDate proximaDose;
    private boolean aplicada;
    private Long petId;

    public VacinaResponseDTO() {}

    public VacinaResponseDTO(Long id, String nome, LocalDate dataAplicacao, LocalDate proximaDose, boolean aplicada, Long petId) {
        this.id = id;
        this.nome = nome;
        this.dataAplicacao = dataAplicacao;
        this.proximaDose = proximaDose;
        this.aplicada = aplicada;
        this.petId = petId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
