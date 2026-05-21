package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.ConsultaRequestDTO;
import br.com.fiap.clyvovet.dto.ConsultaResponseDTO;
import br.com.fiap.clyvovet.model.Consulta;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.repository.ConsultaRepository;
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

    @GetMapping("/veterinario/{nome}")
    @Cacheable(value = "consultasVet", key = "#nome")
    public List<ConsultaResponseDTO> buscarPorVeterinario(@PathVariable String nome) {
        return repository.findByVeterinarioContainingIgnoreCase(nome).stream()
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

    @GetMapping("/pet/{petId}")
    @Cacheable(value = "consultasPet", key = "#petId")
    public List<ConsultaResponseDTO> buscarPorPet(@PathVariable Long petId) {
        return repository.findByPetId(petId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @PostMapping
    @CacheEvict(value = {"consultas", "consulta", "consultasVet", "consultasPeriodo", "consultasPet"}, allEntries = true)
    public ResponseEntity<ConsultaResponseDTO> criar(@Valid @RequestBody ConsultaRequestDTO dto) {
        Consulta consulta = mapToEntity(dto);
        Consulta salva = repository.save(consulta);
        return ResponseEntity.status(201).body(mapToResponseDTO(salva));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"consultas", "consulta", "consultasVet", "consultasPeriodo", "consultasPet"}, allEntries = true)
    public ResponseEntity<ConsultaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ConsultaRequestDTO dto) {
        if (!repository.existsById(id))
            throw new RuntimeException("Consulta não encontrada");
        Consulta consulta = mapToEntity(dto);
        consulta.setId(id);
        Consulta salva = repository.save(consulta);
        return ResponseEntity.ok(mapToResponseDTO(salva));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"consultas", "consulta", "consultasVet", "consultasPeriodo", "consultasPet"}, allEntries = true)
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
                consulta.getDescricao(),
                consulta.getVeterinario(),
                consulta.getObservacoes(),
                consulta.getPet() != null ? consulta.getPet().getId() : null
        );
    }

    private Consulta mapToEntity(ConsultaRequestDTO dto) {
        if (dto == null) return null;
        Consulta consulta = new Consulta();
        consulta.setData(dto.getData());
        consulta.setDescricao(dto.getDescricao());
        consulta.setVeterinario(dto.getVeterinario());
        consulta.setObservacoes(dto.getObservacoes());

        if (dto.getPetId() != null) {
            Pet pet = petRepository.findById(dto.getPetId())
                    .orElseThrow(() -> new RuntimeException("Pet não encontrado com o ID informado"));
            consulta.setPet(pet);
        }
        return consulta;
    }
}