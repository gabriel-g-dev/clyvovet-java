package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.ConsultaRequestDTO;
import br.com.fiap.clyvovet.dto.ConsultaResponseDTO;
import br.com.fiap.clyvovet.model.Consulta;
import br.com.fiap.clyvovet.model.Funcionario;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.repository.ConsultaRepository;
import br.com.fiap.clyvovet.repository.FuncionarioRepository;
import br.com.fiap.clyvovet.repository.PetRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @GetMapping
    @Cacheable(value = "consultas")
    public Page<ConsultaResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "consulta", key = "#id")
    public ResponseEntity<ConsultaResponseDTO> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(consulta -> ResponseEntity.ok(mapToResponseDTO(consulta)))
                .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));
    }

    @GetMapping("/status/{status}")
    @Cacheable(value = "consultasStatus", key = "#status")
    public List<ConsultaResponseDTO> buscarPorStatus(@PathVariable String status) {
        return repository.findByStatus(status).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @GetMapping("/periodo")
    @Cacheable(value = "consultasPeriodo", key = "#inicio.toString() + '-' + #fim.toString()")
    public List<ConsultaResponseDTO> buscarPorPeriodo(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {
        return repository.findByDataBetween(inicio, fim).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @PostMapping
    @CacheEvict(value = {"consultas", "consulta", "consultasStatus", "consultasPeriodo", "inteligenciaPreventiva"}, allEntries = true)
    public ResponseEntity<ConsultaResponseDTO> criar(@Valid @RequestBody ConsultaRequestDTO dto) {
        Consulta consulta = mapToEntity(dto);
        Consulta salva = repository.save(consulta);
        return ResponseEntity.status(201).body(mapToResponseDTO(salva));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"consultas", "consulta", "consultasStatus", "consultasPeriodo", "inteligenciaPreventiva"}, allEntries = true)
    public ResponseEntity<ConsultaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ConsultaRequestDTO dto) {
        if (!repository.existsById(id))
            throw new RuntimeException("Consulta não encontrada");
        Consulta consulta = mapToEntity(dto);
        consulta.setId(id);
        Consulta salva = repository.save(consulta);
        return ResponseEntity.ok(mapToResponseDTO(salva));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"consultas", "consulta", "consultasStatus", "consultasPeriodo", "inteligenciaPreventiva"}, allEntries = true)
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Consulta não encontrada");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ConsultaResponseDTO mapToResponseDTO(Consulta consulta) {
        if (consulta == null) return null;
        return new ConsultaResponseDTO(
                consulta.getId(),
                consulta.getData(),
                consulta.getTipo(),
                consulta.getDescricao(),
                consulta.getValor(),
                consulta.getStatus(),
                consulta.getPet() != null ? consulta.getPet().getId() : null,
                consulta.getFuncionario() != null ? consulta.getFuncionario().getId() : null
        );
    }

    private Consulta mapToEntity(ConsultaRequestDTO dto) {
        if (dto == null) return null;
        Consulta consulta = new Consulta();
        consulta.setData(dto.getData());
        consulta.setTipo(dto.getTipo());
        consulta.setDescricao(dto.getDescricao());
        consulta.setValor(dto.getValor());
        consulta.setStatus(dto.getStatus());

        if (dto.getPetId() != null) {
            Pet pet = petRepository.findById(dto.getPetId())
                    .orElseThrow(() -> new RuntimeException("Pet não encontrado"));
            consulta.setPet(pet);
        }

        if (dto.getFuncionarioId() != null) {
            Funcionario func = funcionarioRepository.findById(dto.getFuncionarioId())
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
            consulta.setFuncionario(func);
        }
        return consulta;
    }
}
