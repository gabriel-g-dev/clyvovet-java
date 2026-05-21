package br.com.fiap.clyvovet.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.math.BigDecimal;
public class ConsultaRequestDTO {
    @NotNull private LocalDateTime data;
    @NotBlank private String tipo;
    @NotBlank private String descricao;
    @NotNull private BigDecimal valor;
    @NotBlank private String status;
    @NotNull private Long petId;
    @NotNull private Long funcionarioId;
    public LocalDateTime getData() { return data; } public void setData(LocalDateTime data) { this.data = data; }
    public String getTipo() { return tipo; } public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; } public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; } public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getPetId() { return petId; } public void setPetId(Long petId) { this.petId = petId; }
    public Long getFuncionarioId() { return funcionarioId; } public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
}
