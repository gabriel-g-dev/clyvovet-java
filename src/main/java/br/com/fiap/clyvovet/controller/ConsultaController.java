package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.ConsultaRequestDTO;
import br.com.fiap.clyvovet.dto.ConsultaResponseDTO;
import br.com.fiap.clyvovet.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@Tag(name = "Consultas", description = "Endpoints para gerenciamento e agendamento de consultas clínicas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @GetMapping
    @Cacheable(value = "consultas", key = "#pageable")
    @Operation(summary = "Listar todas as consultas", description = "Retorna uma lista paginada de todas as consultas agendadas ou realizadas no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    })
    public Page<ConsultaResponseDTO> listar(Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "consulta", key = "#id")
    @Operation(summary = "Buscar consulta por ID", description = "Retorna os detalhes de uma consulta específica com base no seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada", content = @Content)
    })
    public ResponseEntity<ConsultaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/veterinario/{nome}")
    @Cacheable(value = "consultasVet", key = "#nome")
    @Operation(summary = "Buscar consultas por veterinário", description = "Filtra todas as consultas com base no nome do médico veterinário responsável (case-insensitive).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultas retornadas com sucesso")
    })
    public List<ConsultaResponseDTO> buscarPorVeterinario(@PathVariable String nome) {
        return service.buscarPorVeterinario(nome);
    }

    @GetMapping("/periodo")
    @Cacheable(value = "consultasPeriodo", key = "#inicio.toString() + '-' + #fim.toString()")
    @Operation(summary = "Filtrar consultas por período", description = "Retorna o histórico de consultas ocorridas ou agendadas dentro de uma faixa de datas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultas recuperadas para o período informado")
    })
    public List<ConsultaResponseDTO> buscarPorPeriodo(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {
        return service.buscarPorPeriodo(inicio, fim);
    }

    @GetMapping("/pet/{petId}")
    @Cacheable(value = "consultasPet", key = "#petId")
    @Operation(summary = "Histórico de consultas do pet", description = "Lista todas as consultas realizadas ou agendadas para um determinado pet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico de consultas retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado", content = @Content)
    })
    public List<ConsultaResponseDTO> buscarPorPet(@PathVariable Long petId) {
        return service.buscarPorPet(petId);
    }

    @PostMapping
    @CacheEvict(value = {"consultas", "consulta", "consultasVet", "consultasPeriodo", "consultasPet", "inteligenciaPreventiva"}, allEntries = true)
    @Operation(summary = "Agendar/Cadastrar nova consulta", description = "Cadastra uma nova consulta clínica vinculada a um pet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consulta cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pet informado não encontrado", content = @Content)
    })
    public ResponseEntity<ConsultaResponseDTO> criar(@Valid @RequestBody ConsultaRequestDTO dto) {
        ConsultaResponseDTO criado = service.criar(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"consultas", "consulta", "consultasVet", "consultasPeriodo", "consultasPet", "inteligenciaPreventiva"}, allEntries = true)
    @Operation(summary = "Atualizar informações da consulta", description = "Atualiza os dados de uma consulta existente com base no ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados informados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Consulta ou Pet não encontrado", content = @Content)
    })
    public ResponseEntity<ConsultaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ConsultaRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"consultas", "consulta", "consultasVet", "consultasPeriodo", "consultasPet", "inteligenciaPreventiva"}, allEntries = true)
    @Operation(summary = "Excluir consulta", description = "Remove a consulta do histórico médico do pet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Consulta excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada", content = @Content)
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}