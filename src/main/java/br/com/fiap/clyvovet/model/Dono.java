package br.com.fiap.clyvovet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Entity
@Table(name = "DONOS", uniqueConstraints = {@UniqueConstraint(name = "UQ_DONO_EMAIL", columnNames = {"dono_email"})})
public class Dono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dono_id")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "dono_nome", length = 50, nullable = false)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Column(name = "dono_email", length = 50, nullable = false)
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Column(name = "dono_telefone", length = 20, nullable = false)
    private String telefone;

    @OneToMany(mappedBy = "dono", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pet> pets;

    public Dono() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public List<Pet> getPets() { return pets; }
    public void setPets(List<Pet> pets) { this.pets = pets; }
}
