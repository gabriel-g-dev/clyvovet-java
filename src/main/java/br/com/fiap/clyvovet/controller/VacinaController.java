package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.VacinaRequestDTO;
import br.com.fiap.clyvovet.dto.VacinaResponseDTO;
import br.com.fiap.clyvovet.service.VacinaService;
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
@RequestMapping("/api/vacinas")
@Tag(name = "Vacinas", description = "Endpoints para gerenciamento do cartão vacinal dos pets e agendamento de doses")
public class VacinaController {

    private final VacinaService service;

    public VacinaController(VacinaService service) {
        this.service = service;
    }

    @GetMapping
    @Cacheable(value = "vacinas", key = "#pageable")
    @Operation(summary = "Listar todas as vacinas", description = "Retorna uma lista paginada de todos os registros vacinais do sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    })
    public Page<VacinaResponseDTO> listar(Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "vacina", key = "#id")
    @Operation(summary = "Buscar vacina por ID", description = "Retorna os detalhes de um registro de vacina específico com base no ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vacina encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Vacina não encontrada", content = @Content)
    })
    public ResponseEntity<VacinaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/pendentes")
    @Cacheable(value = "vacinasPendentes")
    @Operation(summary = "Listar vacinas pendentes", description = "Retorna todas as doses de vacinas agendadas que ainda não foram aplicadas (aplicada = false).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de vacinas pendentes recuperada com sucesso")
    })
    public List<VacinaResponseDTO> buscarPendentes() {
        return service.buscarPendentes();
    }

    @GetMapping("/nome/{nome}")
    @Cacheable(value = "vacinasNome", key = "#nome")
    @Operation(summary = "Buscar vacinas por nome", description = "Filtra vacinas pelo nome comercial ou científico contido (case-insensitive).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vacinas retornadas com sucesso")
    })
    public List<VacinaResponseDTO> buscarPorNome(@PathVariable String nome) {
        return service.buscarPorNome(nome);
    }

    @GetMapping("/proximas")
    @Cacheable(value = "vacinasProximas", key = "#dias")
    @Operation(summary = "Filtrar próximas vacinas agendadas", description = "Retorna todas as vacinas pendentes com dose agendada para os próximos N dias (default: 30 dias).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de próximas doses recuperada com sucesso")
    })
    public List<VacinaResponseDTO> buscarProximas(@RequestParam(defaultValue = "30") int dias) {
        return service.buscarProximas(dias);
    }

    @GetMapping("/pet/{petId}")
    @Cacheable(value = "vacinasPet", key = "#petId")
    @Operation(summary = "Cartão de vacinas do pet", description = "Lista todas as vacinas (aplicadas ou agendadas) vinculadas ao prontuário de um pet específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cartão vacinal retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado", content = @Content)
    })
    public List<VacinaResponseDTO> buscarPorPet(@PathVariable Long petId) {
        return service.buscarPorPet(petId);
    }

    @PostMapping
    @CacheEvict(value = {"vacinas", "vacina", "vacinasPendentes", "vacinasNome", "vacinasProximas", "vacinasPet", "inteligenciaPreventiva"}, allEntries = true)
    @Operation(summary = "Registrar/Agendar vacina", description = "Registra uma vacina já aplicada ou agenda a data da próxima dose para um pet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vacina registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pet de vinculação não encontrado", content = @Content)
    })
    public ResponseEntity<VacinaResponseDTO> criar(@Valid @RequestBody VacinaRequestDTO dto) {
        VacinaResponseDTO criado = service.criar(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"vacinas", "vacina", "vacinasPendentes", "vacinasNome", "vacinasProximas", "vacinasPet", "inteligenciaPreventiva"}, allEntries = true)
    @Operation(summary = "Atualizar registro de vacina", description = "Atualiza as informações de status cadastral ou de aplicação de uma vacina existente com base no ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro de vacina atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados informados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Vacina ou Pet não encontrado", content = @Content)
    })
    public ResponseEntity<VacinaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody VacinaRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"vacinas", "vacina", "vacinasPendentes", "vacinasNome", "vacinasProximas", "vacinasPet", "inteligenciaPreventiva"}, allEntries = true)
    @Operation(summary = "Excluir vacina", description = "Remove o registro da vacina da ficha médica do pet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vacina excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Vacina não encontrada", content = @Content)
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}