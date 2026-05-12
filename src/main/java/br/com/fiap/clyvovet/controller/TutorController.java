package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.model.Tutor;
import br.com.fiap.clyvovet.repository.TutorRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Page<Tutor> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tutor> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado"));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Tutor> buscarPorEmail(@PathVariable String email) {
        return repository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado"));
    }

    @GetMapping("/{id}/pets")
    public ResponseEntity<Tutor> buscarComPets(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado"));
    }

    @PostMapping
    public ResponseEntity<Tutor> criar(@Valid @RequestBody Tutor tutor) {
        return ResponseEntity.status(201).body(repository.save(tutor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tutor> atualizar(@PathVariable Long id, @Valid @RequestBody Tutor tutor) {
        if (!repository.existsById(id))
            throw new RuntimeException("Tutor não encontrado");
        tutor.setId(id);
        return ResponseEntity.ok(repository.save(tutor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Tutor não encontrado");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}