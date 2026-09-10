package br.com.fiap.clyvovet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PetResponseDTO {

    private Long id;
    private String nome;
    private String especie;
    private String raca;
    private LocalDate dataNascimento;
    private BigDecimal peso;
    private Long tutorId;

    public PetResponseDTO() {}

    public PetResponseDTO(Long id, String nome, String especie, String raca, LocalDate dataNascimento, BigDecimal peso, Long tutorId) {
        this.id = id;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.dataNascimento = dataNascimento;
        this.peso = peso;
        this.tutorId = tutorId;
    }

    public String getIdade() {
        return idadeEmTexto(dataNascimento);
    }

    public static String idadeEmTexto(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            return null;
        }

        LocalDate hoje = LocalDate.now();
        if (dataNascimento.isAfter(hoje)) {
            return "0 dias";
        }

        Period periodo = Period.between(dataNascimento, hoje);
        if (periodo.getYears() >= 1) {
            return periodo.getYears() + (periodo.getYears() == 1 ? " ano" : " anos");
        }
        if (periodo.getMonths() >= 1) {
            return periodo.getMonths() + (periodo.getMonths() == 1 ? " mês" : " meses");
        }
        return periodo.getDays() + (periodo.getDays() == 1 ? " dia" : " dias");
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; } public void setNome(String nome) { this.nome = nome; }
    public String getEspecie() { return especie; } public void setEspecie(String especie) { this.especie = especie; }
    public String getRaca() { return raca; } public void setRaca(String raca) { this.raca = raca; }
    public LocalDate getDataNascimento() { return dataNascimento; } public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public BigDecimal getPeso() { return peso; } public void setPeso(BigDecimal peso) { this.peso = peso; }
    public Long getTutorId() { return tutorId; } public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
}
