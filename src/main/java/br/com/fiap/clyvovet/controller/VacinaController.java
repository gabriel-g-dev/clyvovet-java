package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.VacinaRequestDTO;
import br.com.fiap.clyvovet.dto.VacinaResponseDTO;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.PetRepository;
import br.com.fiap.clyvovet.repository.VacinaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Autowired
    private PetRepository petRepository;

    @GetMapping
    @Cacheable(value = "vacinas")
    public Page<VacinaResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "vacina", key = "#id")
    public ResponseEntity<VacinaResponseDTO> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(vacina -> ResponseEntity.ok(mapToResponseDTO(vacina)))
                .orElseThrow(() -> new RuntimeException("Vacina não encontrada"));
    }

    @GetMapping("/pendentes")
    @Cacheable(value = "vacinasPendentes")
    public List<VacinaResponseDTO> buscarPendentes() {
        return repository.findByStatus("P").stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @GetMapping("/nome/{nome}")
    @Cacheable(value = "vacinasNome", key = "#nome")
    public List<VacinaResponseDTO> buscarPorNome(@PathVariable String nome) {
        return repository.findByNomeContainingIgnoreCase(nome).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @GetMapping("/pet/{petId}")
    @Cacheable(value = "vacinasPet", key = "#petId")
    public List<VacinaResponseDTO> buscarPorPet(@PathVariable Long petId) {
        return repository.findByPetId(petId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @PostMapping
    @CacheEvict(value = {"vacinas", "vacina", "vacinasPendentes", "vacinasNome", "vacinasPet", "inteligenciaPreventiva"}, allEntries = true)
    public ResponseEntity<VacinaResponseDTO> criar(@Valid @RequestBody VacinaRequestDTO dto) {
        Vacina vacina = mapToEntity(dto);
        Vacina salva = repository.save(vacina);
        return ResponseEntity.status(201).body(mapToResponseDTO(salva));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"vacinas", "vacina", "vacinasPendentes", "vacinasNome", "vacinasPet", "inteligenciaPreventiva"}, allEntries = true)
    public ResponseEntity<VacinaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody VacinaRequestDTO dto) {
        if (!repository.existsById(id))
            throw new RuntimeException("Vacina não encontrada");
        Vacina vacina = mapToEntity(dto);
        vacina.setId(id);
        Vacina salva = repository.save(vacina);
        return ResponseEntity.ok(mapToResponseDTO(salva));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"vacinas", "vacina", "vacinasPendentes", "vacinasNome", "vacinasPet", "inteligenciaPreventiva"}, allEntries = true)
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Vacina não encontrada");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private VacinaResponseDTO mapToResponseDTO(Vacina vacina) {
        if (vacina == null) return null;
        return new VacinaResponseDTO(
                vacina.getId(),
                vacina.getNome(),
                vacina.getData(),
                vacina.getStatus(),
                vacina.getPet() != null ? vacina.getPet().getId() : null
        );
    }

    private Vacina mapToEntity(VacinaRequestDTO dto) {
        if (dto == null) return null;
        Vacina vacina = new Vacina();
        vacina.setNome(dto.getNome());
        vacina.setData(dto.getDataAplicacao());
        vacina.setStatus(dto.getStatus() == null ? "P" : dto.getStatus());

        if (dto.getPetId() != null) {
            Pet pet = petRepository.findById(dto.getPetId())
                    .orElseThrow(() -> new RuntimeException("Pet não encontrado com o ID informado"));
            vacina.setPet(pet);
        }
        return vacina;
    }
}
