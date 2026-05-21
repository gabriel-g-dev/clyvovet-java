package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.VacinaRequestDTO;
import br.com.fiap.clyvovet.dto.VacinaResponseDTO;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.PetRepository;
import br.com.fiap.clyvovet.repository.VacinaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class VacinaService {

    private final VacinaRepository repository;
    private final PetRepository petRepository;

    public VacinaService(VacinaRepository repository, PetRepository petRepository) {
        this.repository = repository;
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    public Page<VacinaResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public VacinaResponseDTO buscarPorId(Long id) {
        Vacina vacina = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacina não encontrada com o ID: " + id));
        return mapToResponseDTO(vacina);
    }

    @Transactional(readOnly = true)
    public List<VacinaResponseDTO> buscarPendentes() {
        return repository.findByAplicadaFalse().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VacinaResponseDTO> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VacinaResponseDTO> buscarProximas(int dias) {
        return repository.findByProximaDoseLessThanEqualAndAplicadaFalse(
                LocalDate.now().plusDays(dias)).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VacinaResponseDTO> buscarPorPet(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new ResourceNotFoundException("Pet não encontrado com o ID: " + petId);
        }
        return repository.findByPetId(petId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional
    public VacinaResponseDTO criar(VacinaRequestDTO dto) {
        Vacina vacina = mapToEntity(dto);
        Vacina salva = repository.save(vacina);
        return mapToResponseDTO(salva);
    }

    @Transactional
    public VacinaResponseDTO atualizar(Long id, VacinaRequestDTO dto) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Vacina não encontrada com o ID: " + id);
        }
        Vacina vacina = mapToEntity(dto);
        vacina.setId(id);
        Vacina salva = repository.save(vacina);
        return mapToResponseDTO(salva);
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Vacina não encontrada com o ID: " + id);
        }
        repository.deleteById(id);
    }

    public VacinaResponseDTO mapToResponseDTO(Vacina vacina) {
        if (vacina == null) return null;
        return new VacinaResponseDTO(
                vacina.getId(),
                vacina.getNome(),
                vacina.getDataAplicacao(),
                vacina.getProximaDose(),
                vacina.isAplicada(),
                vacina.getPet() != null ? vacina.getPet().getId() : null
        );
    }

    public Vacina mapToEntity(VacinaRequestDTO dto) {
        if (dto == null) return null;
        Vacina vacina = new Vacina();
        vacina.setNome(dto.getNome());
        vacina.setDataAplicacao(dto.getDataAplicacao());
        vacina.setProximaDose(dto.getProximaDose());
        vacina.setAplicada(dto.isAplicada());

        if (dto.getPetId() != null) {
            Pet pet = petRepository.findById(dto.getPetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com o ID: " + dto.getPetId()));
            vacina.setPet(pet);
        }
        return vacina;
    }
}
