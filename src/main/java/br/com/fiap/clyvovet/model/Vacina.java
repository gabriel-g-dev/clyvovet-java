package br.com.fiap.clyvovet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "TB_VACINA")
public class Vacina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "Data de aplicação é obrigatória")
    private LocalDate dataAplicacao;

    private LocalDate proximaDose;

    private boolean aplicada;

    @NotNull(message = "Pet é obrigatório")
    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    public Vacina() {}

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
    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }
}