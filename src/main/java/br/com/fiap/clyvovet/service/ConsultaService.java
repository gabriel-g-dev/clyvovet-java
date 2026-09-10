package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.ConsultaRequestDTO;
import br.com.fiap.clyvovet.dto.ConsultaResponseDTO;
import br.com.fiap.clyvovet.exception.RecursoNaoEncontradoException;
import br.com.fiap.clyvovet.model.Consulta;
import br.com.fiap.clyvovet.model.Funcionario;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.repository.ConsultaRepository;
import br.com.fiap.clyvovet.repository.FuncionarioRepository;
import br.com.fiap.clyvovet.security.EscopoUsuarioService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@CacheConfig(cacheNames = "inteligenciaPreventiva")
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PetService petService;
    private final EscopoUsuarioService escopo;

    public ConsultaService(ConsultaRepository consultaRepository,
                           FuncionarioRepository funcionarioRepository,
                           PetService petService,
                           EscopoUsuarioService escopo) {
        this.consultaRepository = consultaRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.petService = petService;
        this.escopo = escopo;
    }

    @Transactional(readOnly = true)
    public Page<ConsultaResponseDTO> listar(Pageable pageable) {
        Page<Consulta> consultas = escopo.ehAdmin()
                ? consultaRepository.findAll(pageable)
                : consultaRepository.findByPetDonoEmail(escopo.emailDoUsuarioLogado(), pageable);
        return consultas.map(this::paraResposta);
    }

    @Transactional(readOnly = true)
    public ConsultaResponseDTO buscarPorId(Long id) {
        return paraResposta(buscarConsultaPermitida(id));
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponseDTO> buscarPorStatus(String status) {
        List<Consulta> consultas = escopo.ehAdmin()
                ? consultaRepository.findByStatus(status)
                : consultaRepository.findByStatusAndPetDonoEmail(status, escopo.emailDoUsuarioLogado());
        return consultas.stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponseDTO> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        List<Consulta> consultas = escopo.ehAdmin()
                ? consultaRepository.findByDataBetween(inicio, fim)
                : consultaRepository.findByDataBetweenAndPetDonoEmail(inicio, fim, escopo.emailDoUsuarioLogado());
        return consultas.stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponseDTO> buscarPorPet(Long petId) {
        petService.buscarPetPermitido(petId);
        return consultaRepository.findByPetId(petId).stream().map(this::paraResposta).toList();
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public ConsultaResponseDTO criar(ConsultaRequestDTO dto) {
        return paraResposta(consultaRepository.save(paraEntidade(dto, new Consulta())));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public ConsultaResponseDTO atualizar(Long id, ConsultaRequestDTO dto) {
        Consulta existente = buscarConsultaPermitida(id);
        return paraResposta(consultaRepository.save(paraEntidade(dto, existente)));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void excluir(Long id) {
        consultaRepository.delete(buscarConsultaPermitida(id));
    }

    private Consulta buscarConsultaPermitida(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada com o ID " + id));
        escopo.garantirAcessoAConsulta(consulta);
        return consulta;
    }

    private ConsultaResponseDTO paraResposta(Consulta consulta) {
        if (consulta == null) {
            return null;
        }
        Pet pet = consulta.getPet();
        Funcionario funcionario = consulta.getFuncionario();

        return new ConsultaResponseDTO(
                consulta.getId(),
                consulta.getData(),
                consulta.getTipo(),
                consulta.getDescricao(),
                consulta.getValor(),
                consulta.getStatus(),
                pet != null ? pet.getId() : null,
                funcionario != null ? funcionario.getId() : null,
                pet != null ? pet.getNome() : null,
                funcionario != null ? funcionario.getNome() : null);
    }

    /**
     * Preço é decisão da clínica: o valor enviado pelo TUTOR é ignorado. Na edição
     * fica o valor já gravado, para o tutor não apagar o preço definido pelo ADMIN.
     */
    private BigDecimal valorDaConsulta(ConsultaRequestDTO dto, Consulta consulta) {
        if (escopo.ehAdmin() && dto.getValor() != null) {
            return dto.getValor();
        }
        return consulta.getValor() != null ? consulta.getValor() : BigDecimal.ZERO;
    }

    private Consulta paraEntidade(ConsultaRequestDTO dto, Consulta consulta) {
        consulta.setData(dto.getData());
        consulta.setTipo(dto.getTipo());
        consulta.setDescricao(dto.getDescricao());
        consulta.setValor(valorDaConsulta(dto, consulta));
        consulta.setStatus(dto.getStatus());

        consulta.setPet(petService.buscarPetPermitido(dto.getPetId()));

        Funcionario funcionario = funcionarioRepository.findById(dto.getFuncionarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Funcionário não encontrado com o ID " + dto.getFuncionarioId()));
        consulta.setFuncionario(funcionario);

        return consulta;
    }
}
