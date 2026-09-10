package br.com.fiap.clyvovet.dto;

/**
 * Resposta do cadastro. Não carrega senha nem hash em nenhuma hipótese.
 */
public class RegistroResponseDTO {

    private Long usuarioId;
    private Long tutorId;
    private String nome;
    private String email;
    private String telefone;
    private String role;

    public RegistroResponseDTO() {}

    public RegistroResponseDTO(Long usuarioId, Long tutorId, String nome, String email, String telefone, String role) {
        this.usuarioId = usuarioId;
        this.tutorId = tutorId;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.role = role;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
