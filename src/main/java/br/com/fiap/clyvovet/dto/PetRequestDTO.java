package br.com.fiap.clyvovet.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.math.BigDecimal;
public class PetRequestDTO {
    @NotBlank private String nome;
    @NotNull private LocalDate dataNascimento;
    @NotNull private BigDecimal peso;
    @NotBlank private String especie;
    @NotBlank private String raca;
    @NotNull private Long tutorId;
    public String getNome() { return nome; } public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataNascimento() { return dataNascimento; } public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public BigDecimal getPeso() { return peso; } public void setPeso(BigDecimal peso) { this.peso = peso; }
    public String getEspecie() { return especie; } public void setEspecie(String especie) { this.especie = especie; }
    public String getRaca() { return raca; } public void setRaca(String raca) { this.raca = raca; }
    public Long getTutorId() { return tutorId; } public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
}
