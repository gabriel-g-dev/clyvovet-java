package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.exception.RecursoNaoEncontradoException;

import br.com.fiap.clyvovet.dto.FuncionarioRequestDTO;
import br.com.fiap.clyvovet.dto.FuncionarioResponseDTO;
import br.com.fiap.clyvovet.model.Funcionario;
import br.com.fiap.clyvovet.repository.FuncionarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    @Autowired
    private FuncionarioRepository repository;

    @GetMapping
    public List<FuncionarioResponseDTO> listar() {
        return repository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(func -> ResponseEntity.ok(mapToResponseDTO(func)))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado"));
    }

    @GetMapping("/setor/{setor}")
    public List<FuncionarioResponseDTO> buscarPorSetor(@PathVariable String setor) {
        return repository.findBySetor(setor).stream().map(this::mapToResponseDTO).toList();
    }

    @PostMapping
    public ResponseEntity<FuncionarioResponseDTO> criar(@Valid @RequestBody FuncionarioRequestDTO dto) {
        Funcionario func = mapToEntity(dto);
        Funcionario salvo = repository.save(func);
        return ResponseEntity.status(201).body(mapToResponseDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody FuncionarioRequestDTO dto) {
        if (!repository.existsById(id)) throw new RecursoNaoEncontradoException("Funcionário não encontrado");
        Funcionario func = mapToEntity(dto);
        func.setId(id);
        return ResponseEntity.ok(mapToResponseDTO(repository.save(func)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) throw new RecursoNaoEncontradoException("Funcionário não encontrado");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private FuncionarioResponseDTO mapToResponseDTO(Funcionario func) {
        if (func == null) return null;
        return new FuncionarioResponseDTO(func.getId(), func.getNome(), func.getSetor(), func.getCargo(), func.getEmail(), func.getTelefone());
    }

    private Funcionario mapToEntity(FuncionarioRequestDTO dto) {
        if (dto == null) return null;
        Funcionario func = new Funcionario();
        func.setNome(dto.getNome());
        func.setSetor(dto.getSetor());
        func.setCargo(dto.getCargo());
        func.setEmail(dto.getEmail());
        func.setTelefone(dto.getTelefone());
        return func;
    }
}
