package br.com.fiap.clyvovet.dto;

import java.util.List;

/**
 * Separado do {@link PetResponseDTO} porque a tela precisa do nome do tutor já
 * resolvido e do calendário vacinal aninhado, que o contrato REST não inclui.
 */
public class PetDashboardDTO {

    private Long id;
    private String nome;
    private String especie;
    private String raca;
    private String idade;
    private String tutorNome;
    private List<VacinaResponseDTO> vacinas;
    private long vacinasPendentes;

    public PetDashboardDTO() {}

    public PetDashboardDTO(Long id, String nome, String especie, String raca, String idade,
                           String tutorNome, List<VacinaResponseDTO> vacinas, long vacinasPendentes) {
        this.id = id;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.idade = idade;
        this.tutorNome = tutorNome;
        this.vacinas = vacinas;
        this.vacinasPendentes = vacinasPendentes;
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; } public void setNome(String nome) { this.nome = nome; }
    public String getEspecie() { return especie; } public void setEspecie(String especie) { this.especie = especie; }
    public String getRaca() { return raca; } public void setRaca(String raca) { this.raca = raca; }
    public String getIdade() { return idade; } public void setIdade(String idade) { this.idade = idade; }
    public String getTutorNome() { return tutorNome; } public void setTutorNome(String tutorNome) { this.tutorNome = tutorNome; }
    public List<VacinaResponseDTO> getVacinas() { return vacinas; } public void setVacinas(List<VacinaResponseDTO> vacinas) { this.vacinas = vacinas; }
    public long getVacinasPendentes() { return vacinasPendentes; } public void setVacinasPendentes(long vacinasPendentes) { this.vacinasPendentes = vacinasPendentes; }
}
