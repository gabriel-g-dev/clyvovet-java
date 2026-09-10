package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.ConsultaRequestDTO;
import br.com.fiap.clyvovet.dto.ConsultaResponseDTO;
import br.com.fiap.clyvovet.service.ConsultaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@Tag(name = "Consultas", description = "Agenda e histórico clínico do pet")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public Page<ConsultaResponseDTO> listar(Pageable pageable) {
        return consultaService.listar(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.buscarPorId(id));
    }

    @GetMapping("/status/{status}")
    public List<ConsultaResponseDTO> buscarPorStatus(@PathVariable String status) {
        return consultaService.buscarPorStatus(status);
    }

    @GetMapping("/periodo")
    public List<ConsultaResponseDTO> buscarPorPeriodo(@RequestParam LocalDateTime inicio,
                                                      @RequestParam LocalDateTime fim) {
        return consultaService.buscarPorPeriodo(inicio, fim);
    }

    @GetMapping("/pet/{petId}")
    public List<ConsultaResponseDTO> buscarPorPet(@PathVariable Long petId) {
        return consultaService.buscarPorPet(petId);
    }

    @PostMapping
    public ResponseEntity<ConsultaResponseDTO> criar(@Valid @RequestBody ConsultaRequestDTO dto) {
        return ResponseEntity.status(201).body(consultaService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaResponseDTO> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody ConsultaRequestDTO dto) {
        return ResponseEntity.ok(consultaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        consultaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
