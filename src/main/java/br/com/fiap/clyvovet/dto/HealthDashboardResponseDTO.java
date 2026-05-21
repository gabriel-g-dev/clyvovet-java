package br.com.fiap.clyvovet.dto;

import java.util.List;

public class HealthDashboardResponseDTO {

    private Long petId;
    private String petNome;
    private int scoreSaude;
    private String statusEngajamento;
    private List<String> recomendacoes;

    public HealthDashboardResponseDTO() {}

    public HealthDashboardResponseDTO(Long petId, String petNome, int scoreSaude, String statusEngajamento, List<String> recomendacoes) {
        this.petId = petId;
        this.petNome = petNome;
        this.scoreSaude = scoreSaude;
        this.statusEngajamento = statusEngajamento;
        this.recomendacoes = recomendacoes;
    }

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getPetNome() { return petNome; }
    public void setPetNome(String petNome) { this.petNome = petNome; }
    public int getScoreSaude() { return scoreSaude; }
    public void setScoreSaude(int scoreSaude) { this.scoreSaude = scoreSaude; }
    public String getStatusEngajamento() { return statusEngajamento; }
    public void setStatusEngajamento(String statusEngajamento) { this.statusEngajamento = statusEngajamento; }
    public List<String> getRecomendacoes() { return recomendacoes; }
    public void setRecomendacoes(List<String> recomendacoes) { this.recomendacoes = recomendacoes; }
}
