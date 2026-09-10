package br.com.fiap.clyvovet.dto;

public class LoginResponseDTO {

    private String token;
    private String tipo;
    private String email;
    private String role;
    private long expiraEmSegundos;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, String tipo, String email, String role, long expiraEmSegundos) {
        this.token = token;
        this.tipo = tipo;
        this.email = email;
        this.role = role;
        this.expiraEmSegundos = expiraEmSegundos;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public long getExpiraEmSegundos() { return expiraEmSegundos; }
    public void setExpiraEmSegundos(long expiraEmSegundos) { this.expiraEmSegundos = expiraEmSegundos; }
}
