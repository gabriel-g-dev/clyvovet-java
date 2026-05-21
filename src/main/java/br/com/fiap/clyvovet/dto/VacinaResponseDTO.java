package br.com.fiap.clyvovet.dto;
import java.time.LocalDate;
public class VacinaResponseDTO {
    private Long id; private String nome; private LocalDate data; private String status; private Long petId;
    public VacinaResponseDTO() {}
    public VacinaResponseDTO(Long id, String nome, LocalDate data, String status, Long petId) {
        this.id = id; this.nome = nome; this.data = data; this.status = status; this.petId = petId;
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; } public void setNome(String nome) { this.nome = nome; }
    public LocalDate getData() { return data; } public void setData(LocalDate data) { this.data = data; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getPetId() { return petId; } public void setPetId(Long petId) { this.petId = petId; }
}
