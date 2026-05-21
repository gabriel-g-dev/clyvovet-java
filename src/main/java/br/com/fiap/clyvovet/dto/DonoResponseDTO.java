package br.com.fiap.clyvovet.dto;

import java.util.List;

public class DonoResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private List<PetResponseDTO> pets;

    public DonoResponseDTO() {}

    public DonoResponseDTO(Long id, String nome, String email, String telefone, List<PetResponseDTO> pets) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.pets = pets;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public List<PetResponseDTO> getPets() { return pets; }
    public void setPets(List<PetResponseDTO> pets) { this.pets = pets; }
}
