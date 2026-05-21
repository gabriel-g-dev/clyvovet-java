package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.TutorRequestDTO;
import br.com.fiap.clyvovet.dto.TutorResponseDTO;
import br.com.fiap.clyvovet.dto.PetResponseDTO;
import br.com.fiap.clyvovet.model.Tutor;
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
@RequestMapping("/api/tutores")
public class TutorController {

    @Autowired
    private TutorRepository repository;

    @GetMapping
    @Cacheable(value = "tutores")
    public Page<TutorResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "tutor", key = "#id")
    public ResponseEntity<TutorResponseDTO> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(tutor -> ResponseEntity.ok(mapToResponseDTO(tutor)))
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado"));
    }

    @GetMapping("/email/{email}")
    @Cacheable(value = "tutorEmail", key = "#email")
    public ResponseEntity<TutorResponseDTO> buscarPorEmail(@PathVariable String email) {
        return repository.findByEmail(email)
                .map(tutor -> ResponseEntity.ok(mapToResponseDTO(tutor)))
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado"));
    }

    @GetMapping("/{id}/pets")
    @Cacheable(value = "tutorPets", key = "#id")
    public ResponseEntity<TutorResponseDTO> buscarComPets(@PathVariable Long id) {
        return repository.findById(id)
                .map(tutor -> ResponseEntity.ok(mapToResponseDTO(tutor)))
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado"));
    }

    @PostMapping
    @CacheEvict(value = {"tutores", "tutor", "tutorEmail", "tutorPets"}, allEntries = true)
    public ResponseEntity<TutorResponseDTO> criar(@Valid @RequestBody TutorRequestDTO dto) {
        Tutor tutor = mapToEntity(dto);
        Tutor salvo = repository.save(tutor);
        return ResponseEntity.status(201).body(mapToResponseDTO(salvo));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"tutores", "tutor", "tutorEmail", "tutorPets"}, allEntries = true)
    public ResponseEntity<TutorResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody TutorRequestDTO dto) {
        if (!repository.existsById(id))
            throw new RuntimeException("Tutor não encontrado");
        Tutor tutor = mapToEntity(dto);
        tutor.setId(id);
        Tutor salvo = repository.save(tutor);
        return ResponseEntity.ok(mapToResponseDTO(salvo));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"tutores", "tutor", "tutorEmail", "tutorPets"}, allEntries = true)
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Tutor não encontrado");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private TutorResponseDTO mapToResponseDTO(Tutor tutor) {
        if (tutor == null) return null;
        List<PetResponseDTO> petDTOs = null;
        if (tutor.getPets() != null) {
            petDTOs = tutor.getPets().stream()
                    .map(pet -> new PetResponseDTO(
                            pet.getId(),
                            pet.getNome(),
                            pet.getEspecie(),
                            pet.getRaca(),
                            pet.getDataNascimento(),
                            tutor.getId()
                    ))
                    .toList();
        }
        return new TutorResponseDTO(
                tutor.getId(),
                tutor.getNome(),
                tutor.getEmail(),
                tutor.getTelefone(),
                petDTOs
        );
    }

    private Tutor mapToEntity(TutorRequestDTO dto) {
        if (dto == null) return null;
        Tutor tutor = new Tutor();
        tutor.setNome(dto.getNome());
        tutor.setEmail(dto.getEmail());
        tutor.setTelefone(dto.getTelefone());
        return tutor;
    }
}