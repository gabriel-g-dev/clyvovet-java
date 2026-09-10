package br.com.fiap.clyvovet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VacinaResponseDTO {

    private Long id;
    private String nome;
    private LocalDate data;
    private String status;
    private Long petId;
    private Integer intervaloReforcoDias;

    public VacinaResponseDTO() {}

    public VacinaResponseDTO(Long id, String nome, LocalDate data, String status, Long petId) {
        this(id, nome, data, status, petId, null);
    }

    public VacinaResponseDTO(Long id, String nome, LocalDate data, String status, Long petId, Integer intervaloReforcoDias) {
        this.id = id;
        this.nome = nome;
        this.data = data;
        this.status = status;
        this.petId = petId;
        this.intervaloReforcoDias = intervaloReforcoDias;
    }

    public String getStatusDescricao() {
        if (status == null) return null;
        return switch (status) {
            case "A" -> "Aplicada";
            case "P" -> "Pendente";
            default  -> status;
        };
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; } public void setNome(String nome) { this.nome = nome; }
    public LocalDate getData() { return data; } public void setData(LocalDate data) { this.data = data; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getPetId() { return petId; } public void setPetId(Long petId) { this.petId = petId; }
    public Integer getIntervaloReforcoDias() { return intervaloReforcoDias; }
    public void setIntervaloReforcoDias(Integer intervaloReforcoDias) { this.intervaloReforcoDias = intervaloReforcoDias; }
}
