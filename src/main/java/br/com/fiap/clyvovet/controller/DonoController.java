package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.DonoRequestDTO;
import br.com.fiap.clyvovet.dto.DonoResponseDTO;
import br.com.fiap.clyvovet.dto.PetResponseDTO;
import br.com.fiap.clyvovet.exception.RecursoNaoEncontradoException;
import br.com.fiap.clyvovet.model.Dono;
import br.com.fiap.clyvovet.repository.DonoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutores")
@CacheConfig(cacheNames = {"Donoes", "Dono", "DonoEmail", "DonoPets"})
public class DonoController {

    @Autowired
    private DonoRepository repository;

    @GetMapping
    @Cacheable(value = "Donoes")
    @Transactional(readOnly = true)
    public Page<DonoResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "Dono", key = "#id")
    @Transactional(readOnly = true)
    public ResponseEntity<DonoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(buscarTutor(id));
    }

    @GetMapping("/email/{email}")
    @Cacheable(value = "DonoEmail", key = "#email")
    @Transactional(readOnly = true)
    public ResponseEntity<DonoResponseDTO> buscarPorEmail(@PathVariable String email) {
        return repository.findByEmail(email)
                .map(dono -> ResponseEntity.ok(mapToResponseDTO(dono)))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Dono não encontrado"));
    }

    // A resposta do tutor já traz a lista de pets; esta rota existe para deixar
    // essa intenção explícita no contrato e para ter o seu próprio cache.
    @GetMapping("/{id}/pets")
    @Cacheable(value = "DonoPets", key = "#id")
    @Transactional(readOnly = true)
    public ResponseEntity<DonoResponseDTO> buscarComPets(@PathVariable Long id) {
        return ResponseEntity.ok(buscarTutor(id));
    }

    @PostMapping
    @CacheEvict(allEntries = true)
    public ResponseEntity<DonoResponseDTO> criar(@Valid @RequestBody DonoRequestDTO dto) {
        Dono dono = mapToEntity(dto);
        Dono salvo = repository.save(dono);
        return ResponseEntity.status(201).body(mapToResponseDTO(salvo));
    }

    @PutMapping("/{id}")
    @CacheEvict(allEntries = true)
    public ResponseEntity<DonoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody DonoRequestDTO dto) {
        if (!repository.existsById(id))
            throw new RecursoNaoEncontradoException("Dono não encontrado");
        Dono dono = mapToEntity(dto);
        dono.setId(id);
        Dono salvo = repository.save(dono);
        return ResponseEntity.ok(mapToResponseDTO(salvo));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id))
            throw new RecursoNaoEncontradoException("Dono não encontrado");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private DonoResponseDTO buscarTutor(Long id) {
        return repository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Dono não encontrado"));
    }

    private DonoResponseDTO mapToResponseDTO(Dono dono) {
        if (dono == null) return null;
        List<PetResponseDTO> petDTOs = null;
        if (dono.getPets() != null) {
            petDTOs = dono.getPets().stream()
                    .map(pet -> new PetResponseDTO(
                            pet.getId(),
                            pet.getNome(),
                            pet.getEspecie(),
                            pet.getRaca(),
                            pet.getDataNascimento(),
                            pet.getPeso(),
                            dono.getId()
                    ))
                    .toList();
        }
        return new DonoResponseDTO(
                dono.getId(),
                dono.getNome(),
                dono.getEmail(),
                dono.getTelefone(),
                petDTOs
        );
    }

    private Dono mapToEntity(DonoRequestDTO dto) {
        if (dto == null) return null;
        Dono dono = new Dono();
        dono.setNome(dto.getNome());
        dono.setEmail(dto.getEmail());
        dono.setTelefone(dto.getTelefone());
        return dono;
    }
}
