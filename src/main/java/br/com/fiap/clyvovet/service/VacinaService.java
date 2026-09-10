package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.VacinaRequestDTO;
import br.com.fiap.clyvovet.dto.VacinaResponseDTO;
import br.com.fiap.clyvovet.exception.RecursoNaoEncontradoException;
import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.VacinaRepository;
import br.com.fiap.clyvovet.security.EscopoUsuarioService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Calendário vacinal.
 *
 * A leitura segue o dono do pet: o TUTOR acompanha o cartão de vacinas dos seus
 * animais. Registrar, alterar e dar baixa continua sendo ato da clínica, e a
 * chain de segurança restringe essas rotas ao ADMIN.
 */
@Service
@CacheConfig(cacheNames = "inteligenciaPreventiva")
public class VacinaService {

    private final VacinaRepository vacinaRepository;
    private final PetService petService;
    private final EscopoUsuarioService escopo;

    public VacinaService(VacinaRepository vacinaRepository, PetService petService, EscopoUsuarioService escopo) {
        this.vacinaRepository = vacinaRepository;
        this.petService = petService;
        this.escopo = escopo;
    }

    @Transactional(readOnly = true)
    public Page<VacinaResponseDTO> listar(Pageable pageable) {
        Page<Vacina> vacinas = escopo.ehAdmin()
                ? vacinaRepository.findAll(pageable)
                : vacinaRepository.findByPetDonoEmail(escopo.emailDoUsuarioLogado(), pageable);
        return vacinas.map(this::paraResposta);
    }

    @Transactional(readOnly = true)
    public VacinaResponseDTO buscarPorId(Long id) {
        return paraResposta(buscarVacinaPermitida(id));
    }

    @Transactional(readOnly = true)
    public List<VacinaResponseDTO> buscarPendentes() {
        List<Vacina> vacinas = escopo.ehAdmin()
                ? vacinaRepository.findByStatus(BaixaVacinaService.STATUS_PENDENTE)
                : vacinaRepository.findByStatusAndPetDonoEmail(
                        BaixaVacinaService.STATUS_PENDENTE, escopo.emailDoUsuarioLogado());
        return vacinas.stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<VacinaResponseDTO> buscarPorNome(String nome) {
        List<Vacina> vacinas = escopo.ehAdmin()
                ? vacinaRepository.findByNomeContainingIgnoreCase(nome)
                : vacinaRepository.findByNomeContainingIgnoreCaseAndPetDonoEmail(
                        nome, escopo.emailDoUsuarioLogado());
        return vacinas.stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<VacinaResponseDTO> buscarPorPet(Long petId) {
        petService.buscarPetPermitido(petId);
        return vacinaRepository.findByPetId(petId).stream().map(this::paraResposta).toList();
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public VacinaResponseDTO criar(VacinaRequestDTO dto) {
        return paraResposta(vacinaRepository.save(paraEntidade(dto, new Vacina())));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public VacinaResponseDTO atualizar(Long id, VacinaRequestDTO dto) {
        Vacina existente = buscarVacinaPermitida(id);
        return paraResposta(vacinaRepository.save(paraEntidade(dto, existente)));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void excluir(Long id) {
        vacinaRepository.delete(buscarVacinaPermitida(id));
    }

    private Vacina buscarVacinaPermitida(Long id) {
        Vacina vacina = vacinaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vacina não encontrada com o ID " + id));
        if (vacina.getPet() != null) {
            escopo.garantirAcessoAoPet(vacina.getPet());
        }
        return vacina;
    }

    public VacinaResponseDTO paraResposta(Vacina vacina) {
        if (vacina == null) {
            return null;
        }
        return new VacinaResponseDTO(
                vacina.getId(),
                vacina.getNome(),
                vacina.getData(),
                vacina.getStatus(),
                vacina.getPet() != null ? vacina.getPet().getId() : null,
                vacina.getIntervaloReforcoDias());
    }

    private Vacina paraEntidade(VacinaRequestDTO dto, Vacina vacina) {
        vacina.setNome(dto.getNome());
        vacina.setData(dto.getDataAplicacao());
        vacina.setStatus(dto.getStatus() == null ? BaixaVacinaService.STATUS_PENDENTE : dto.getStatus());
        vacina.setIntervaloReforcoDias(dto.getIntervaloReforcoDias());
        vacina.setPet(petService.buscarPetPermitido(dto.getPetId()));
        return vacina;
    }
}
