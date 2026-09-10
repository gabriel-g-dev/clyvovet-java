package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PetRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    private String nome;

    @NotNull(message = "Data de nascimento é obrigatória")
    @PastOrPresent(message = "Data de nascimento não pode estar no futuro")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataNascimento;

    @NotNull(message = "Peso é obrigatório")
    @Positive(message = "Peso deve ser maior que zero")
    @DecimalMax(value = "999.99", message = "Peso deve ser menor que 1000 kg")
    private BigDecimal peso;

    @NotBlank(message = "Espécie é obrigatória")
    @Size(max = 50, message = "Espécie deve ter no máximo 50 caracteres")
    private String especie;

    @NotBlank(message = "Raça é obrigatória")
    @Size(max = 50, message = "Raça deve ter no máximo 50 caracteres")
    private String raca;

    @NotNull(message = "Tutor é obrigatório")
    private Long tutorId;

    public String getNome() { return nome; } public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataNascimento() { return dataNascimento; } public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public BigDecimal getPeso() { return peso; } public void setPeso(BigDecimal peso) { this.peso = peso; }
    public String getEspecie() { return especie; } public void setEspecie(String especie) { this.especie = especie; }
    public String getRaca() { return raca; } public void setRaca(String raca) { this.raca = raca; }
    public Long getTutorId() { return tutorId; } public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
}
