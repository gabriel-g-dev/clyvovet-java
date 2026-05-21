package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.MedicamentoRequestDTO;
import br.com.fiap.clyvovet.dto.MedicamentoResponseDTO;
import br.com.fiap.clyvovet.model.Medicamento;
import br.com.fiap.clyvovet.repository.MedicamentoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentoController {

    @Autowired
    private MedicamentoRepository repository;

    @GetMapping
    public List<MedicamentoResponseDTO> listar() {
        return repository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(med -> ResponseEntity.ok(mapToResponseDTO(med)))
                .orElseThrow(() -> new RuntimeException("Medicamento não encontrado"));
    }

    @PostMapping
    public ResponseEntity<MedicamentoResponseDTO> criar(@Valid @RequestBody MedicamentoRequestDTO dto) {
        Medicamento med = new Medicamento();
        med.setNome(dto.getNome());
        Medicamento salvo = repository.save(med);
        return ResponseEntity.status(201).body(mapToResponseDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody MedicamentoRequestDTO dto) {
        if (!repository.existsById(id)) throw new RuntimeException("Medicamento não encontrado");
        Medicamento med = new Medicamento();
        med.setId(id);
        med.setNome(dto.getNome());
        return ResponseEntity.ok(mapToResponseDTO(repository.save(med)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) throw new RuntimeException("Medicamento não encontrado");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private MedicamentoResponseDTO mapToResponseDTO(Medicamento med) {
        if (med == null) return null;
        return new MedicamentoResponseDTO(med.getId(), med.getNome());
    }
}
