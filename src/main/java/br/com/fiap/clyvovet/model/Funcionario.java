package br.com.fiap.clyvovet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Entity
@Table(name = "FUNCIONARIOS", uniqueConstraints = {@UniqueConstraint(name = "UQ_FUNC_EMAIL", columnNames = {"funcionario_email"})})
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "funcionario_id")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "funcionario_nome", length = 50, nullable = false)
    private String nome;

    @NotBlank(message = "Setor é obrigatório")
    @Column(name = "funcionario_setor", length = 50, nullable = false)
    private String setor;

    @NotBlank(message = "Cargo é obrigatório")
    @Column(name = "funcionario_cargo", length = 50, nullable = false)
    private String cargo;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Column(name = "funcionario_email", length = 50, nullable = false)
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Column(name = "funcionario_telefone", length = 20, nullable = false)
    private String telefone;

    @OneToMany(mappedBy = "funcionario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Consulta> consultas;

    public Funcionario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSetor() { return setor; }
    public void setSetor(String setor) { this.setor = setor; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public List<Consulta> getConsultas() { return consultas; }
    public void setConsultas(List<Consulta> consultas) { this.consultas = consultas; }
}
