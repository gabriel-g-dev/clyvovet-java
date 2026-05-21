package br.com.fiap.clyvovet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "CONSULTAS_MEDICAMENTOS")
public class ConsultaMedicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consulta_medicamento_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id", nullable = false, foreignKey = @ForeignKey(name = "CM_CONSULTAS_FK"))
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false, foreignKey = @ForeignKey(name = "CM_MEDICAMENTOS_FK"))
    private Medicamento medicamento;

    @NotBlank(message = "Dosagem é obrigatória")
    @Column(name = "cm_dosagem", length = 100, nullable = false)
    private String dosagem;

    public ConsultaMedicamento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Consulta getConsulta() { return consulta; }
    public void setConsulta(Consulta consulta) { this.consulta = consulta; }
    public Medicamento getMedicamento() { return medicamento; }
    public void setMedicamento(Medicamento medicamento) { this.medicamento = medicamento; }
    public String getDosagem() { return dosagem; }
    public void setDosagem(String dosagem) { this.dosagem = dosagem; }
}
