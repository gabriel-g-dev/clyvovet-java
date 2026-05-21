package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.PetRequestDTO;
import br.com.fiap.clyvovet.dto.PetResponseDTO;
import br.com.fiap.clyvovet.dto.HealthDashboardResponseDTO;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.model.Tutor;
import br.com.fiap.clyvovet.repository.PetRepository;
import br.com.fiap.clyvovet.repository.TutorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PetService {

    private final PetRepository repository;
    private final TutorRepository tutorRepository;
    private final IntelligenceService intelligenceService;

    public PetService(PetRepository repository, TutorRepository tutorRepository, IntelligenceService intelligenceService) {
        this.repository = repository;
        this.tutorRepository = tutorRepository;
        this.intelligenceService = intelligenceService;
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public PetResponseDTO buscarPorId(Long id) {
        Pet pet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com o ID: " + id));
        return mapToResponseDTO(pet);
    }

    @Transactional(readOnly = true)
    public HealthDashboardResponseDTO obterInteligenciaPreventiva(Long id) {
        Pet pet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com o ID: " + id));
        return intelligenceService.calcularInteligenciaPreventiva(pet);
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> buscarPorEspecie(String especie) {
        return repository.findByEspecieIgnoreCase(especie).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> buscarPorRaca(String raca) {
        return repository.findByRacaIgnoreCase(raca).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> buscarPorTutor(Long tutorId) {
        if (!tutorRepository.existsById(tutorId)) {
            throw new ResourceNotFoundException("Tutor não encontrado com o ID: " + tutorId);
        }
        return repository.findByTutorId(tutorId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional
    public PetResponseDTO criar(PetRequestDTO dto) {
        Pet pet = mapToEntity(dto);
        Pet salvo = repository.save(pet);
        return mapToResponseDTO(salvo);
    }

    @Transactional
    public PetResponseDTO atualizar(Long id, PetRequestDTO dto) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Pet não encontrado com o ID: " + id);
        }
        Pet pet = mapToEntity(dto);
        pet.setId(id);
        Pet salvo = repository.save(pet);
        return mapToResponseDTO(salvo);
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Pet não encontrado com o ID: " + id);
        }
        repository.deleteById(id);
    }

    public PetResponseDTO mapToResponseDTO(Pet pet) {
        if (pet == null) return null;
        return new PetResponseDTO(
                pet.getId(),
                pet.getNome(),
                pet.getEspecie(),
                pet.getRaca(),
                pet.getDataNascimento(),
                pet.getTutor() != null ? pet.getTutor().getId() : null
        );
    }

    public Pet mapToEntity(PetRequestDTO dto) {
        if (dto == null) return null;
        Pet pet = new Pet();
        pet.setNome(dto.getNome());
        pet.setEspecie(dto.getEspecie());
        pet.setRaca(dto.getRaca());
        pet.setDataNascimento(dto.getDataNascimento());

        if (dto.getTutorId() != null) {
            Tutor tutor = tutorRepository.findById(dto.getTutorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado com o ID: " + dto.getTutorId()));
            pet.setTutor(tutor);
        }
        return pet;
    }
}
