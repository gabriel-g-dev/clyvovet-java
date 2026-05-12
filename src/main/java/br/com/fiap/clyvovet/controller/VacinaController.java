package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.VacinaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vacinas")
public class VacinaController {

    @Autowired
    private VacinaRepository repository;

    @GetMapping
    public Page<Vacina> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacina> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Vacina não encontrada"));
    }

    @GetMapping("/pendentes")
    public List<Vacina> buscarPendentes() {
        return repository.findByAplicadaFalse();
    }

    @GetMapping("/nome/{nome}")
    public List<Vacina> buscarPorNome(@PathVariable String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    @GetMapping("/proximas")
    public List<Vacina> buscarProximas(@RequestParam(defaultValue = "30") int dias) {
        return repository.findByProximaDoseLessThanEqualAndAplicadaFalse(
                LocalDate.now().plusDays(dias));
    }

    @GetMapping("/pet/{petId}")
    public List<Vacina> buscarPorPet(@PathVariable Long petId) {
        return repository.findByPetId(petId);
    }

    @PostMapping
    public ResponseEntity<Vacina> criar(@Valid @RequestBody Vacina vacina) {
        return ResponseEntity.status(201).body(repository.save(vacina));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vacina> atualizar(@PathVariable Long id, @Valid @RequestBody Vacina vacina) {
        if (!repository.existsById(id))
            throw new RuntimeException("Vacina não encontrada");
        vacina.setId(id);
        return ResponseEntity.ok(repository.save(vacina));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Vacina não encontrada");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}