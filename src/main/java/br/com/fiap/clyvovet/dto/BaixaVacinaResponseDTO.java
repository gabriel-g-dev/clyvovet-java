package br.com.fiap.clyvovet.dto;

public class BaixaVacinaResponseDTO {

    private String mensagem;
    private VacinaResponseDTO vacinaAplicada;
    private VacinaResponseDTO proximaDose;

    public BaixaVacinaResponseDTO() {}

    public BaixaVacinaResponseDTO(String mensagem, VacinaResponseDTO vacinaAplicada, VacinaResponseDTO proximaDose) {
        this.mensagem = mensagem;
        this.vacinaAplicada = vacinaAplicada;
        this.proximaDose = proximaDose;
    }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public VacinaResponseDTO getVacinaAplicada() { return vacinaAplicada; }
    public void setVacinaAplicada(VacinaResponseDTO vacinaAplicada) { this.vacinaAplicada = vacinaAplicada; }
    public VacinaResponseDTO getProximaDose() { return proximaDose; }
    public void setProximaDose(VacinaResponseDTO proximaDose) { this.proximaDose = proximaDose; }
}
