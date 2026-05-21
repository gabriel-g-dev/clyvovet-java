package br.com.fiap.clyvovet.dto;
public class FuncionarioResponseDTO {
    private Long id; private String nome; private String setor; private String cargo; private String email; private String telefone;
    public FuncionarioResponseDTO() {}
    public FuncionarioResponseDTO(Long id, String nome, String setor, String cargo, String email, String telefone) {
        this.id = id; this.nome = nome; this.setor = setor; this.cargo = cargo; this.email = email; this.telefone = telefone;
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; } public void setNome(String nome) { this.nome = nome; }
    public String getSetor() { return setor; } public void setSetor(String setor) { this.setor = setor; }
    public String getCargo() { return cargo; } public void setCargo(String cargo) { this.cargo = cargo; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; } public void setTelefone(String telefone) { this.telefone = telefone; }
}
