package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ConsultaRequestDTO {

    @NotNull(message = "Data da consulta é obrigatória")
    private LocalDateTime data;

    @NotBlank(message = "Tipo é obrigatório")
    @Size(max = 50, message = "Tipo deve ter no máximo 50 caracteres")
    private String tipo;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @PositiveOrZero(message = "Valor não pode ser negativo")
    @DecimalMax(value = "99999999.99", message = "Valor excede o limite permitido")
    private BigDecimal valor;

    @NotBlank(message = "Status é obrigatório")
    @Pattern(regexp = "[ARC]", message = "Status deve ser 'A' (agendada), 'R' (realizada) ou 'C' (cancelada)")
    private String status;

    @NotNull(message = "ID do pet é obrigatório")
    private Long petId;

    @NotNull(message = "ID do funcionário é obrigatório")
    private Long funcionarioId;

    public LocalDateTime getData() { return data; } public void setData(LocalDateTime data) { this.data = data; }
    public String getTipo() { return tipo; } public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; } public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; } public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getPetId() { return petId; } public void setPetId(Long petId) { this.petId = petId; }
    public Long getFuncionarioId() { return funcionarioId; } public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
}
