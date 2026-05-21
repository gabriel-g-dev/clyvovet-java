package br.com.fiap.clyvovet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "CONSULTAS")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consulta_id")
    private Long id;

    @NotBlank(message = "Tipo é obrigatório")
    @Column(name = "consulta_tipo", length = 50, nullable = false)
    private String tipo;

    @NotNull(message = "Valor é obrigatório")
    @Column(name = "consulta_valor", precision = 10, scale = 2, nullable = false)
    private BigDecimal valor;

    @NotBlank(message = "Descrição é obrigatória")
    @Column(name = "consulta_descricao", length = 500, nullable = false)
    private String descricao;

    @NotBlank(message = "Status é obrigatório")
    @Column(name = "consulta_status", length = 1, nullable = false)
    private String status;

    @NotNull(message = "Data é obrigatória")
    @Column(name = "consulta_data", nullable = false)
    private LocalDateTime data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false, foreignKey = @ForeignKey(name = "CONSULTAS_PETS_FK"))
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id", nullable = false, foreignKey = @ForeignKey(name = "CONSULTAS_FUNCIONARIOS_FK"))
    private Funcionario funcionario;

    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConsultaMedicamento> consultasMedicamentos;

    public Consulta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }
    public List<ConsultaMedicamento> getConsultasMedicamentos() { return consultasMedicamentos; }
    public void setConsultasMedicamentos(List<ConsultaMedicamento> consultasMedicamentos) { this.consultasMedicamentos = consultasMedicamentos; }
}
