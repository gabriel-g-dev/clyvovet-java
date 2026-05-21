package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ConsultaRequestDTO {

    @NotNull(message = "Data é obrigatória")
    private LocalDateTime data;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotBlank(message = "Veterinário é obrigatório")
    private String veterinario;

    private String observacoes;

    @NotNull(message = "ID do pet é obrigatório")
    private Long petId;

    public ConsultaRequestDTO() {}

    public ConsultaRequestDTO(LocalDateTime data, String descricao, String veterinario, String observacoes, Long petId) {
        this.data = data;
        this.descricao = descricao;
        this.veterinario = veterinario;
        this.observacoes = observacoes;
        this.petId = petId;
    }

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
