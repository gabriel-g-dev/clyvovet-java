package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.PetDashboardDTO;
import br.com.fiap.clyvovet.dto.PetResponseDTO;
import br.com.fiap.clyvovet.dto.VacinaResponseDTO;
import br.com.fiap.clyvovet.exception.RecursoNaoEncontradoException;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.PetRepository;
import br.com.fiap.clyvovet.security.EscopoUsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Monta os dados das telas do painel web.
 *
 * A aplicação roda com open-in-view=false, então as associações LAZY (dono e
 * vacinas) precisam ser materializadas aqui dentro da transação — se fossem
 * tocadas durante a renderização do template, o Hibernate já teria fechado a
 * sessão.
 */
@Service
public class PainelService {

    private final PetRepository petRepository;
    private final EscopoUsuarioService escopo;

    public PainelService(PetRepository petRepository, EscopoUsuarioService escopo) {
        this.petRepository = petRepository;
        this.escopo = escopo;
    }

    @Transactional(readOnly = true)
    public List<PetDashboardDTO> listarPets() {
        List<Pet> encontrados = escopo.ehAdmin()
                ? petRepository.findAll()
                : petRepository.findByDonoEmail(escopo.emailDoUsuarioLogado());

        List<PetDashboardDTO> pets = new ArrayList<>();
        for (Pet pet : encontrados) {
            pets.add(montar(pet));
        }
        return pets;
    }

    @Transactional(readOnly = true)
    public PetDashboardDTO buscarPet(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet não encontrado com o ID " + id));
        escopo.garantirAcessoAoPet(pet);
        return montar(pet);
    }

    public long contarVacinasPendentes(List<PetDashboardDTO> pets) {
        return pets.stream().mapToLong(PetDashboardDTO::getVacinasPendentes).sum();
    }

    private PetDashboardDTO montar(Pet pet) {
        List<VacinaResponseDTO> vacinas = new ArrayList<>();
        long pendentes = 0;

        if (pet.getVacinas() != null) {
            for (Vacina vacina : pet.getVacinas()) {
                vacinas.add(new VacinaResponseDTO(
                        vacina.getId(), vacina.getNome(), vacina.getData(),
                        vacina.getStatus(), pet.getId(), vacina.getIntervaloReforcoDias()));

                if (vacina.estaPendente()) {
                    pendentes++;
                }
            }
            vacinas.sort(Comparator.comparing(VacinaResponseDTO::getData));
        }

        return new PetDashboardDTO(
                pet.getId(), pet.getNome(), pet.getEspecie(), pet.getRaca(),
                PetResponseDTO.idadeEmTexto(pet.getDataNascimento()),
                pet.getDono() != null ? pet.getDono().getNome() : "—",
                vacinas, pendentes);
    }
}
