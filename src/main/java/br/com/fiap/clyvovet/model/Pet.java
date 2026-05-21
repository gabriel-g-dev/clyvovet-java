package br.com.fiap.clyvovet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "PETS")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "pet_nome", length = 50, nullable = false)
    private String nome;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Column(name = "pet_data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @NotNull(message = "Peso é obrigatório")
    @Column(name = "pet_peso", precision = 5, scale = 2, nullable = false)
    private BigDecimal peso;

    @NotBlank(message = "Espécie é obrigatória")
    @Column(name = "pet_especie", length = 50, nullable = false)
    private String especie;

    @NotBlank(message = "Raça é obrigatória")
    @Column(name = "pet_raca", length = 50, nullable = false)
    private String raca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dono_id", nullable = false, foreignKey = @ForeignKey(name = "PETS_DONOS_FK"))
    private Dono dono;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Consulta> consultas;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vacina> vacinas;

    public Pet() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }
    public Dono getDono() { return dono; }
    public void setDono(Dono dono) { this.dono = dono; }
    public List<Consulta> getConsultas() { return consultas; }
    public void setConsultas(List<Consulta> consultas) { this.consultas = consultas; }
    public List<Vacina> getVacinas() { return vacinas; }
    public void setVacinas(List<Vacina> vacinas) { this.vacinas = vacinas; }
}
