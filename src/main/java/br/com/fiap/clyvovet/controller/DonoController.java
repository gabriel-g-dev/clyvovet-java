package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.DonoRequestDTO;
import br.com.fiap.clyvovet.dto.DonoResponseDTO;
import br.com.fiap.clyvovet.dto.PetResponseDTO;
import br.com.fiap.clyvovet.model.Dono;
import br.com.fiap.clyvovet.repository.DonoRepository;
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
public class DonoController {

    @Autowired
    private DonoRepository repository;

    @GetMapping
    @Cacheable(value = "Donoes")
    public Page<DonoResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "Dono", key = "#id")
    public ResponseEntity<DonoResponseDTO> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(Dono -> ResponseEntity.ok(mapToResponseDTO(Dono)))
                .orElseThrow(() -> new RuntimeException("Dono não encontrado"));
    }

    @GetMapping("/email/{email}")
    @Cacheable(value = "DonoEmail", key = "#email")
    public ResponseEntity<DonoResponseDTO> buscarPorEmail(@PathVariable String email) {
        return repository.findByEmail(email)
                .map(Dono -> ResponseEntity.ok(mapToResponseDTO(Dono)))
                .orElseThrow(() -> new RuntimeException("Dono não encontrado"));
    }

    @GetMapping("/{id}/pets")
    @Cacheable(value = "DonoPets", key = "#id")
    public ResponseEntity<DonoResponseDTO> buscarComPets(@PathVariable Long id) {
        return repository.findById(id)
                .map(Dono -> ResponseEntity.ok(mapToResponseDTO(Dono)))
                .orElseThrow(() -> new RuntimeException("Dono não encontrado"));
    }

    @PostMapping
    @CacheEvict(value = {"Donoes", "Dono", "DonoEmail", "DonoPets"}, allEntries = true)
    public ResponseEntity<DonoResponseDTO> criar(@Valid @RequestBody DonoRequestDTO dto) {
        Dono Dono = mapToEntity(dto);
        Dono salvo = repository.save(Dono);
        return ResponseEntity.status(201).body(mapToResponseDTO(salvo));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"Donoes", "Dono", "DonoEmail", "DonoPets"}, allEntries = true)
    public ResponseEntity<DonoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody DonoRequestDTO dto) {
        if (!repository.existsById(id))
            throw new RuntimeException("Dono não encontrado");
        Dono Dono = mapToEntity(dto);
        Dono.setId(id);
        Dono salvo = repository.save(Dono);
        return ResponseEntity.ok(mapToResponseDTO(salvo));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"Donoes", "Dono", "DonoEmail", "DonoPets"}, allEntries = true)
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Dono não encontrado");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private DonoResponseDTO mapToResponseDTO(Dono Dono) {
        if (Dono == null) return null;
        List<PetResponseDTO> petDTOs = null;
        if (Dono.getPets() != null) {
            petDTOs = Dono.getPets().stream()
                    .map(pet -> new PetResponseDTO(
                            pet.getId(),
                            pet.getNome(),
                            pet.getEspecie(),
                            pet.getRaca(),
                            pet.getDataNascimento(),
                            pet.getPeso(),
                            Dono.getId()
                    ))
                    .toList();
        }
        return new DonoResponseDTO(
                Dono.getId(),
                Dono.getNome(),
                Dono.getEmail(),
                Dono.getTelefone(),
                petDTOs
        );
    }

    private Dono mapToEntity(DonoRequestDTO dto) {
        if (dto == null) return null;
        Dono Dono = new Dono();
        Dono.setNome(dto.getNome());
        Dono.setEmail(dto.getEmail());
        Dono.setTelefone(dto.getTelefone());
        return Dono;
    }
}
