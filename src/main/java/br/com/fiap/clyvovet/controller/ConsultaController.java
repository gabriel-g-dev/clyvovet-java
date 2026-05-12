package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.model.Consulta;
import br.com.fiap.clyvovet.repository.ConsultaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaRepository repository;

    @GetMapping
    public Page<Consulta> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Consulta> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));
    }

    @GetMapping("/veterinario/{nome}")
    public List<Consulta> buscarPorVeterinario(@PathVariable String nome) {
        return repository.findByVeterinarioContainingIgnoreCase(nome);
    }

    @GetMapping("/periodo")
    public List<Consulta> buscarPorPeriodo(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {
        return repository.findByDataBetween(inicio, fim);
    }

    @GetMapping("/pet/{petId}")
    public List<Consulta> buscarPorPet(@PathVariable Long petId) {
        return repository.findByPetId(petId);
    }

    @PostMapping
    public ResponseEntity<Consulta> criar(@Valid @RequestBody Consulta consulta) {
        return ResponseEntity.status(201).body(repository.save(consulta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Consulta> atualizar(@PathVariable Long id, @Valid @RequestBody Consulta consulta) {
        if (!repository.existsById(id))
            throw new RuntimeException("Consulta não encontrada");
        consulta.setId(id);
        return ResponseEntity.ok(repository.save(consulta));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Consulta não encontrada");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}