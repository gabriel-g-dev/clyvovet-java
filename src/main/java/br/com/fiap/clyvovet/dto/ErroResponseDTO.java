package br.com.fiap.clyvovet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroResponseDTO {

    private LocalDateTime timestamp;

    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
    private Map<String, String> campos;

    public ErroResponseDTO() {}

    public ErroResponseDTO(int status, String erro, String mensagem, String caminho, Map<String, String> campos) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
        this.campos = campos;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getErro() { return erro; }
    public void setErro(String erro) { this.erro = erro; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public String getCaminho() { return caminho; }
    public void setCaminho(String caminho) { this.caminho = caminho; }
    public Map<String, String> getCampos() { return campos; }
    public void setCampos(Map<String, String> campos) { this.campos = campos; }
}
