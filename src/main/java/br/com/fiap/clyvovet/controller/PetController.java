package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.PetRequestDTO;
import br.com.fiap.clyvovet.dto.PetResponseDTO;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.model.Tutor;
import br.com.fiap.clyvovet.repository.PetRepository;
import br.com.fiap.clyvovet.repository.TutorRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    @Autowired
    private PetRepository repository;

    @Autowired
    private TutorRepository tutorRepository;

    @GetMapping
    @Cacheable(value = "pets")
    public Page<PetResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "pet", key = "#id")
    public ResponseEntity<PetResponseDTO> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(pet -> ResponseEntity.ok(mapToResponseDTO(pet)))
                .orElseThrow(() -> new RuntimeException("Pet não encontrado"));
    }

    @GetMapping("/especie/{especie}")
    @Cacheable(value = "petsEspecie", key = "#especie")
    public List<PetResponseDTO> buscarPorEspecie(@PathVariable String especie) {
        return repository.findByEspecieIgnoreCase(especie).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @GetMapping("/raca/{raca}")
    @Cacheable(value = "petsRaca", key = "#raca")
    public List<PetResponseDTO> buscarPorRaca(@PathVariable String raca) {
        return repository.findByRacaIgnoreCase(raca).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @GetMapping("/tutor/{tutorId}")
    @Cacheable(value = "petsTutor", key = "#tutorId")
    public List<PetResponseDTO> buscarPorTutor(@PathVariable Long tutorId) {
        return repository.findByTutorId(tutorId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @PostMapping
    @CacheEvict(value = {"pets", "pet", "petsEspecie", "petsRaca", "petsTutor", "tutores", "tutor", "tutorEmail", "tutorPets"}, allEntries = true)
    public ResponseEntity<PetResponseDTO> criar(@Valid @RequestBody PetRequestDTO dto) {
        Pet pet = mapToEntity(dto);
        Pet salvo = repository.save(pet);
        return ResponseEntity.status(201).body(mapToResponseDTO(salvo));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"pets", "pet", "petsEspecie", "petsRaca", "petsTutor", "tutores", "tutor", "tutorEmail", "tutorPets"}, allEntries = true)
    public ResponseEntity<PetResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PetRequestDTO dto) {
        if (!repository.existsById(id))
            throw new RuntimeException("Pet não encontrado");
        Pet pet = mapToEntity(dto);
        pet.setId(id);
        Pet salvo = repository.save(pet);
        return ResponseEntity.ok(mapToResponseDTO(salvo));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"pets", "pet", "petsEspecie", "petsRaca", "petsTutor", "tutores", "tutor", "tutorEmail", "tutorPets"}, allEntries = true)
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Pet não encontrado");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private PetResponseDTO mapToResponseDTO(Pet pet) {
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

    private Pet mapToEntity(PetRequestDTO dto) {
        if (dto == null) return null;
        Pet pet = new Pet();
        pet.setNome(dto.getNome());
        pet.setEspecie(dto.getEspecie());
        pet.setRaca(dto.getRaca());
        pet.setDataNascimento(dto.getDataNascimento());

        if (dto.getTutorId() != null) {
            Tutor tutor = tutorRepository.findById(dto.getTutorId())
                    .orElseThrow(() -> new RuntimeException("Tutor não encontrado com o ID informado"));
            pet.setTutor(tutor);
        }
        return pet;
    }
}