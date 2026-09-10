package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class AplicacaoVacinaRequestDTO {

    @PastOrPresent(message = "A data de aplicação não pode estar no futuro")
    private LocalDate dataAplicacao;

    public AplicacaoVacinaRequestDTO() {}

    public AplicacaoVacinaRequestDTO(LocalDate dataAplicacao) {
        this.dataAplicacao = dataAplicacao;
    }

    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public void setDataAplicacao(LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }
}
