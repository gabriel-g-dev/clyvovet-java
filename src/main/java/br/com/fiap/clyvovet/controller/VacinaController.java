package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.AplicacaoVacinaRequestDTO;
import br.com.fiap.clyvovet.dto.BaixaVacinaResponseDTO;
import br.com.fiap.clyvovet.dto.VacinaRequestDTO;
import br.com.fiap.clyvovet.dto.VacinaResponseDTO;
import br.com.fiap.clyvovet.service.BaixaVacinaResultado;
import br.com.fiap.clyvovet.service.BaixaVacinaService;
import br.com.fiap.clyvovet.service.VacinaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vacinas")
@Tag(name = "Vacinas", description = "Calendário vacinal e baixa de aplicação")
public class VacinaController {

    private final VacinaService vacinaService;
    private final BaixaVacinaService baixaVacinaService;

    public VacinaController(VacinaService vacinaService, BaixaVacinaService baixaVacinaService) {
        this.vacinaService = vacinaService;
        this.baixaVacinaService = baixaVacinaService;
    }

    @GetMapping
    public Page<VacinaResponseDTO> listar(Pageable pageable) {
        return vacinaService.listar(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VacinaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vacinaService.buscarPorId(id));
    }

    @GetMapping("/pendentes")
    public List<VacinaResponseDTO> buscarPendentes() {
        return vacinaService.buscarPendentes();
    }

    @GetMapping("/nome/{nome}")
    public List<VacinaResponseDTO> buscarPorNome(@PathVariable String nome) {
        return vacinaService.buscarPorNome(nome);
    }

    @GetMapping("/pet/{petId}")
    public List<VacinaResponseDTO> buscarPorPet(@PathVariable Long petId) {
        return vacinaService.buscarPorPet(petId);
    }

    @PostMapping("/{id}/aplicar")
    @Operation(summary = "Registra a aplicação da vacina e agenda automaticamente a próxima dose")
    @CacheEvict(value = "inteligenciaPreventiva", allEntries = true)
    public ResponseEntity<BaixaVacinaResponseDTO> aplicar(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AplicacaoVacinaRequestDTO dto) {

        LocalDate dataAplicacao = dto != null ? dto.getDataAplicacao() : null;
        BaixaVacinaResultado resultado = baixaVacinaService.aplicar(id, dataAplicacao);

        String mensagem = switch (resultado.reforco()) {
            case AGENDADO_AGORA -> "Vacina aplicada e próxima dose agendada automaticamente";
            case JA_AGENDADO -> "Vacina aplicada. A próxima dose já estava agendada";
            case SEM_INTERVALO -> "Vacina aplicada. Esta vacina não possui intervalo de reforço cadastrado";
        };

        BaixaVacinaResponseDTO resposta = new BaixaVacinaResponseDTO(
                mensagem,
                vacinaService.paraResposta(resultado.aplicada()),
                vacinaService.paraResposta(resultado.proximaDose()));

        return ResponseEntity.ok(resposta);
    }

    @PostMapping
    public ResponseEntity<VacinaResponseDTO> criar(@Valid @RequestBody VacinaRequestDTO dto) {
        return ResponseEntity.status(201).body(vacinaService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VacinaResponseDTO> atualizar(@PathVariable Long id,
                                                       @Valid @RequestBody VacinaRequestDTO dto) {
        return ResponseEntity.ok(vacinaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        vacinaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
