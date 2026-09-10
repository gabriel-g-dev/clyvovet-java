package br.com.fiap.clyvovet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "VACINAS")
public class Vacina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacina_id")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "vacina_nome", length = 50, nullable = false)
    private String nome;

    @NotNull(message = "Data é obrigatória")
    @Column(name = "vacina_data", nullable = false)
    private LocalDate data;

    @NotBlank(message = "Status é obrigatório")
    @Column(name = "vacina_status", length = 1, nullable = false)
    private String status = "P";

    @Positive(message = "Intervalo de reforço deve ser maior que zero")
    @Column(name = "vacina_intervalo_reforco_dias")
    private Integer intervaloReforcoDias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false, foreignKey = @ForeignKey(name = "VACINAS_PETS_FK"))
    private Pet pet;

    public Vacina() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getIntervaloReforcoDias() { return intervaloReforcoDias; }
    public void setIntervaloReforcoDias(Integer intervaloReforcoDias) { this.intervaloReforcoDias = intervaloReforcoDias; }
    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public boolean estaPendente() {
        return "P".equals(status);
    }

    public boolean estaAtrasada() {
        return estaPendente() && data != null && data.isBefore(LocalDate.now());
    }

    public boolean venceEm(int dias) {
        if (!estaPendente() || data == null) {
            return false;
        }
        LocalDate hoje = LocalDate.now();
        return !data.isBefore(hoje) && data.isBefore(hoje.plusDays(dias));
    }
}
