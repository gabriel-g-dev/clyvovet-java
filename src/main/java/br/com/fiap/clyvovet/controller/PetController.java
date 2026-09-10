package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.HealthDashboardResponseDTO;
import br.com.fiap.clyvovet.dto.PetRequestDTO;
import br.com.fiap.clyvovet.dto.PetResponseDTO;
import br.com.fiap.clyvovet.service.IntelligenceService;
import br.com.fiap.clyvovet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Pets", description = "Cadastro de pets e inteligência preventiva")
public class PetController {

    private final PetService petService;
    private final IntelligenceService intelligenceService;

    public PetController(PetService petService, IntelligenceService intelligenceService) {
        this.petService = petService;
        this.intelligenceService = intelligenceService;
    }

    @GetMapping
    public Page<PetResponseDTO> listar(Pageable pageable) {
        return petService.listar(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(petService.buscarPorId(id));
    }

    @GetMapping("/{id}/inteligencia-preventiva")
    @Operation(summary = "Calcula o score de saúde preventiva e as recomendações do pet")
    public ResponseEntity<HealthDashboardResponseDTO> obterInteligenciaPreventiva(@PathVariable Long id) {
        // A permissão é conferida fora do cache: o resultado é o mesmo para
        // qualquer usuário, mas nem todo usuário pode pedir o deste pet.
        petService.buscarPetPermitido(id);
        return ResponseEntity.ok(intelligenceService.calcularPorPetId(id));
    }

    @GetMapping("/especie/{especie}")
    public List<PetResponseDTO> buscarPorEspecie(@PathVariable String especie) {
        return petService.buscarPorEspecie(especie);
    }

    @GetMapping("/raca/{raca}")
    public List<PetResponseDTO> buscarPorRaca(@PathVariable String raca) {
        return petService.buscarPorRaca(raca);
    }

    @GetMapping("/tutor/{tutorId}")
    public List<PetResponseDTO> buscarPorDono(@PathVariable("tutorId") Long tutorId) {
        return petService.buscarPorTutor(tutorId);
    }

    @PostMapping
    public ResponseEntity<PetResponseDTO> criar(@Valid @RequestBody PetRequestDTO dto) {
        return ResponseEntity.status(201).body(petService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PetRequestDTO dto) {
        return ResponseEntity.ok(petService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        petService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
