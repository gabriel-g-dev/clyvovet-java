package br.com.fiap.clyvovet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Entity
@Table(name = "MEDICAMENTOS")
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicamento_id")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "medicamento_nome", length = 100, nullable = false)
    private String nome;

    @OneToMany(mappedBy = "medicamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConsultaMedicamento> consultasMedicamentos;

    public Medicamento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public List<ConsultaMedicamento> getConsultasMedicamentos() { return consultasMedicamentos; }
    public void setConsultasMedicamentos(List<ConsultaMedicamento> consultasMedicamentos) { this.consultasMedicamentos = consultasMedicamentos; }
}
