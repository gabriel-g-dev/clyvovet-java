package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.PetRequestDTO;
import br.com.fiap.clyvovet.dto.PetResponseDTO;
import br.com.fiap.clyvovet.exception.RecursoNaoEncontradoException;
import br.com.fiap.clyvovet.model.Dono;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.repository.DonoRepository;
import br.com.fiap.clyvovet.repository.PetRepository;
import br.com.fiap.clyvovet.security.EscopoUsuarioService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cadastro de pets usado pela API REST e pelo painel web.
 *
 * O ADMIN enxerga e altera qualquer pet. O TUTOR só alcança os pets do próprio
 * cadastro, e o tutor gravado no pet é sempre ele mesmo — o {@code tutorId} que
 * vier no corpo da requisição é ignorado nesse caso.
 *
 * Alterar um pet limpa o cache dos tutores, porque a resposta de tutor traz
 * a lista de pets, e o da análise preventiva.
 */
@Service
@CacheConfig(cacheNames = {"Donoes", "Dono", "DonoEmail", "DonoPets", "inteligenciaPreventiva"})
public class PetService {

    private final PetRepository petRepository;
    private final DonoRepository donoRepository;
    private final EscopoUsuarioService escopo;

    public PetService(PetRepository petRepository, DonoRepository donoRepository, EscopoUsuarioService escopo) {
        this.petRepository = petRepository;
        this.donoRepository = donoRepository;
        this.escopo = escopo;
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> listar(Pageable pageable) {
        Page<Pet> pets = escopo.ehAdmin()
                ? petRepository.findAll(pageable)
                : petRepository.findByDonoEmail(escopo.emailDoUsuarioLogado(), pageable);
        return pets.map(this::paraResposta);
    }

    @Transactional(readOnly = true)
    public PetResponseDTO buscarPorId(Long id) {
        return paraResposta(buscarPetPermitido(id));
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> buscarPorEspecie(String especie) {
        List<Pet> pets = escopo.ehAdmin()
                ? petRepository.findByEspecieIgnoreCase(especie)
                : petRepository.findByEspecieIgnoreCaseAndDonoEmail(especie, escopo.emailDoUsuarioLogado());
        return pets.stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> buscarPorRaca(String raca) {
        List<Pet> pets = escopo.ehAdmin()
                ? petRepository.findByRacaIgnoreCase(raca)
                : petRepository.findByRacaIgnoreCaseAndDonoEmail(raca, escopo.emailDoUsuarioLogado());
        return pets.stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> buscarPorTutor(Long tutorId) {
        Long permitido = escopo.tutorIdPermitido(tutorId);
        return petRepository.findByDonoId(permitido).stream().map(this::paraResposta).toList();
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public PetResponseDTO criar(PetRequestDTO dto) {
        return paraResposta(petRepository.save(paraEntidade(dto, new Pet())));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public PetResponseDTO atualizar(Long id, PetRequestDTO dto) {
        Pet existente = buscarPetPermitido(id);
        return paraResposta(petRepository.save(paraEntidade(dto, existente)));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void excluir(Long id) {
        petRepository.delete(buscarPetPermitido(id));
    }

    @Transactional(readOnly = true)
    public Pet buscarPetPermitido(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet não encontrado com o ID " + id));
        escopo.garantirAcessoAoPet(pet);
        return pet;
    }

    public PetResponseDTO paraResposta(Pet pet) {
        if (pet == null) {
            return null;
        }
        return new PetResponseDTO(
                pet.getId(),
                pet.getNome(),
                pet.getEspecie(),
                pet.getRaca(),
                pet.getDataNascimento(),
                pet.getPeso(),
                pet.getDono() != null ? pet.getDono().getId() : null);
    }

    private Pet paraEntidade(PetRequestDTO dto, Pet pet) {
        pet.setNome(dto.getNome());
        pet.setEspecie(dto.getEspecie());
        pet.setRaca(dto.getRaca());
        pet.setDataNascimento(dto.getDataNascimento());
        pet.setPeso(dto.getPeso());

        Long tutorId = escopo.tutorIdPermitido(dto.getTutorId());
        Dono dono = donoRepository.findById(tutorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tutor não encontrado com o ID " + tutorId));
        pet.setDono(dono);

        return pet;
    }
}
