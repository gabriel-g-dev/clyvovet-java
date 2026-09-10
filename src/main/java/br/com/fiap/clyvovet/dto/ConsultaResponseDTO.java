package br.com.fiap.clyvovet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsultaResponseDTO {

    private Long id;
    private LocalDateTime data;
    private String tipo;
    private String descricao;
    private BigDecimal valor;
    private String status;
    private Long petId;
    private Long funcionarioId;
    private String petNome;
    private String funcionarioNome;

    public ConsultaResponseDTO() {}

    public ConsultaResponseDTO(Long id, LocalDateTime data, String tipo, String descricao, BigDecimal valor,
                               String status, Long petId, Long funcionarioId) {
        this(id, data, tipo, descricao, valor, status, petId, funcionarioId, null, null);
    }

    public ConsultaResponseDTO(Long id, LocalDateTime data, String tipo, String descricao, BigDecimal valor,
                               String status, Long petId, Long funcionarioId,
                               String petNome, String funcionarioNome) {
        this.id = id;
        this.data = data;
        this.tipo = tipo;
        this.descricao = descricao;
        this.valor = valor;
        this.status = status;
        this.petId = petId;
        this.funcionarioId = funcionarioId;
        this.petNome = petNome;
        this.funcionarioNome = funcionarioNome;
    }

    public String getStatusDescricao() {
        if (status == null) return null;
        return switch (status) {
            case "A" -> "Agendada";
            case "R" -> "Realizada";
            case "C" -> "Cancelada";
            default  -> status;
        };
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public LocalDateTime getData() { return data; } public void setData(LocalDateTime data) { this.data = data; }
    public String getTipo() { return tipo; } public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; } public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; } public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getPetId() { return petId; } public void setPetId(Long petId) { this.petId = petId; }
    public Long getFuncionarioId() { return funcionarioId; } public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
    public String getPetNome() { return petNome; } public void setPetNome(String petNome) { this.petNome = petNome; }
    public String getFuncionarioNome() { return funcionarioNome; } public void setFuncionarioNome(String funcionarioNome) { this.funcionarioNome = funcionarioNome; }
}
