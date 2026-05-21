package br.com.fiap.clyvovet.dto;

import java.time.LocalDateTime;

public class ConsultaResponseDTO {

    private Long id;
    private LocalDateTime data;
    private String descricao;
    private String veterinario;
    private String observacoes;
    private Long petId;

    public ConsultaResponseDTO() {}

    public ConsultaResponseDTO(Long id, LocalDateTime data, String descricao, String veterinario, String observacoes, Long petId) {
        this.id = id;
        this.data = data;
        this.descricao = descricao;
        this.veterinario = veterinario;
        this.observacoes = observacoes;
        this.petId = petId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getVeterinario() { return veterinario; }
    public void setVeterinario(String veterinario) { this.veterinario = veterinario; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
}
