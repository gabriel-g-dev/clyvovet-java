package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.PetRequestDTO;
import br.com.fiap.clyvovet.dto.PetResponseDTO;
import br.com.fiap.clyvovet.dto.HealthDashboardResponseDTO;
import br.com.fiap.clyvovet.service.PetService;
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
import java.util.List;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Pets", description = "Endpoints para gerenciamento do cadastro dos pets e motor de saúde preventiva")
public class PetController {

    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    @GetMapping
    @Cacheable(value = "pets", key = "#pageable")
    @Operation(summary = "Listar todos os pets", description = "Retorna uma lista paginada de todos os pets cadastrados no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    })
    public Page<PetResponseDTO> listar(Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "pet", key = "#id")
    @Operation(summary = "Buscar pet por ID", description = "Retorna os detalhes cadastrais de um pet específico com base no seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado", content = @Content)
    })
    public ResponseEntity<PetResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/{id}/inteligencia-preventiva")
    @Cacheable(value = "inteligenciaPreventiva", key = "#id")
    @Operation(summary = "Mecanismo de Inteligência Preventiva do Pet [INOVAÇÃO]", 
               description = "Calcula o Score Preventivo de Saúde (0-100) do animal com base no seu prontuário cadastrado e histórico de vacinas e consultas. Gera alertas e recomendações de cuidado inteligentes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard preventivo calculado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado", content = @Content)
    })
    public ResponseEntity<HealthDashboardResponseDTO> obterInteligenciaPreventiva(@PathVariable Long id) {
        return ResponseEntity.ok(service.obterInteligenciaPreventiva(id));
    }

    @GetMapping("/especie/{especie}")
    @Cacheable(value = "petsEspecie", key = "#especie")
    @Operation(summary = "Buscar pets por espécie", description = "Filtra todos os pets do sistema cuja espécie coincide com o termo informado (case-insensitive).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pets filtrados retornados com sucesso")
    })
    public List<PetResponseDTO> buscarPorEspecie(@PathVariable String especie) {
        return service.buscarPorEspecie(especie);
    }

    @GetMapping("/raca/{raca}")
    @Cacheable(value = "petsRaca", key = "#raca")
    @Operation(summary = "Buscar pets por raça", description = "Filtra todos os pets do sistema cuja raça coincide com o termo informado (case-insensitive).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pets filtrados retornados com sucesso")
    })
    public List<PetResponseDTO> buscarPorRaca(@PathVariable String raca) {
        return service.buscarPorRaca(raca);
    }

    @GetMapping("/tutor/{tutorId}")
    @Cacheable(value = "petsTutor", key = "#tutorId")
    @Operation(summary = "Listar pets por Tutor ID", description = "Lista todos os pets que pertencem a um tutor específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pets do tutor retornados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tutor não encontrado", content = @Content)
    })
    public List<PetResponseDTO> buscarPorTutor(@PathVariable Long tutorId) {
        return service.buscarPorTutor(tutorId);
    }

    @PostMapping
    @CacheEvict(value = {"pets", "pet", "petsEspecie", "petsRaca", "petsTutor", "tutores", "tutor", "tutorEmail", "tutorPets", "inteligenciaPreventiva"}, allEntries = true)
    @Operation(summary = "Cadastrar novo pet", description = "Vincula um novo pet a um tutor existente pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pet cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados cadastrais inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tutor de vinculação não encontrado", content = @Content)
    })
    public ResponseEntity<PetResponseDTO> criar(@Valid @RequestBody PetRequestDTO dto) {
        PetResponseDTO criado = service.criar(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"pets", "pet", "petsEspecie", "petsRaca", "petsTutor", "tutores", "tutor", "tutorEmail", "tutorPets", "inteligenciaPreventiva"}, allEntries = true)
    @Operation(summary = "Atualizar informações do pet", description = "Atualiza os dados de um pet existente no sistema com base no seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados informados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pet ou Tutor não encontrado", content = @Content)
    })
    public ResponseEntity<PetResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PetRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"pets", "pet", "petsEspecie", "petsRaca", "petsTutor", "tutores", "tutor", "tutorEmail", "tutorPets", "inteligenciaPreventiva"}, allEntries = true)
    @Operation(summary = "Excluir pet", description = "Exclui um pet do sistema juntamente com seu histórico vacinal e de consultas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pet removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado", content = @Content)
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}