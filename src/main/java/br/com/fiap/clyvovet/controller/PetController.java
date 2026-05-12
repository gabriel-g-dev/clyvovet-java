package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.repository.PetRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping
    public Page<Pet> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pet> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Pet não encontrado"));
    }

    @GetMapping("/especie/{especie}")
    public List<Pet> buscarPorEspecie(@PathVariable String especie) {
        return repository.findByEspecieIgnoreCase(especie);
    }

    @GetMapping("/raca/{raca}")
    public List<Pet> buscarPorRaca(@PathVariable String raca) {
        return repository.findByRacaIgnoreCase(raca);
    }

    @GetMapping("/tutor/{tutorId}")
    public List<Pet> buscarPorTutor(@PathVariable Long tutorId) {
        return repository.findByTutorId(tutorId);
    }

    @PostMapping
    public ResponseEntity<Pet> criar(@Valid @RequestBody Pet pet) {
        return ResponseEntity.status(201).body(repository.save(pet));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pet> atualizar(@PathVariable Long id, @Valid @RequestBody Pet pet) {
        if (!repository.existsById(id))
            throw new RuntimeException("Pet não encontrado");
        pet.setId(id);
        return ResponseEntity.ok(repository.save(pet));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Pet não encontrado");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}