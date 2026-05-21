package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.TutorRequestDTO;
import br.com.fiap.clyvovet.dto.TutorResponseDTO;
import br.com.fiap.clyvovet.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/tutores")
@Tag(name = "Tutores", description = "Endpoints para gerenciamento do cadastro de tutores de pets")
public class TutorController {

    private final TutorService service;

    public TutorController(TutorService service) {
        this.service = service;
    }

    @GetMapping
    @Cacheable(value = "tutores", key = "#pageable")
    @Operation(summary = "Listar todos os tutores", description = "Retorna uma lista paginada de todos os tutores cadastrados no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos", content = @Content)
    })
    public Page<TutorResponseDTO> listar(Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "tutor", key = "#id")
    @Operation(summary = "Buscar tutor por ID", description = "Retorna os detalhes de um tutor específico com base no seu ID único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tutor encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tutor não encontrado", content = @Content)
    })
    public ResponseEntity<TutorResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/email/{email}")
    @Cacheable(value = "tutorEmail", key = "#email")
    @Operation(summary = "Buscar tutor por e-mail", description = "Retorna os detalhes do tutor associado ao e-mail informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tutor encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tutor não encontrado", content = @Content)
    })
    public ResponseEntity<TutorResponseDTO> buscarPorEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.buscarPorEmail(email));
    }

    @GetMapping("/{id}/pets")
    @Cacheable(value = "tutorPets", key = "#id")
    @Operation(summary = "Listar pets do tutor", description = "Retorna os detalhes do tutor juntamente com a lista de todos os seus pets vinculados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pets retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tutor não encontrado", content = @Content)
    })
    public ResponseEntity<TutorResponseDTO> buscarComPets(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarComPets(id));
    }

    @PostMapping
    @CacheEvict(value = {"tutores", "tutor", "tutorEmail", "tutorPets"}, allEntries = true)
    @Operation(summary = "Cadastrar novo tutor", description = "Cria um novo tutor no sistema. O e-mail informado deve ser único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tutor cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados cadastrais inválidos", content = @Content)
    })
    public ResponseEntity<TutorResponseDTO> criar(@Valid @RequestBody TutorRequestDTO dto) {
        TutorResponseDTO criado = service.criar(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"tutores", "tutor", "tutorEmail", "tutorPets"}, allEntries = true)
    @Operation(summary = "Atualizar dados do tutor", description = "Atualiza as informações cadastrais de um tutor existente com base no ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tutor atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tutor não encontrado", content = @Content)
    })
    public ResponseEntity<TutorResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody TutorRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"tutores", "tutor", "tutorEmail", "tutorPets"}, allEntries = true)
    @Operation(summary = "Excluir tutor", description = "Remove o tutor e todos os seus pets, consultas e vacinas associados em cascata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tutor removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tutor não encontrado", content = @Content)
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}