package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.exception.RecursoNaoEncontradoException;
import br.com.fiap.clyvovet.exception.RegraNegocioException;
import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.VacinaRepository;
import br.com.fiap.clyvovet.service.BaixaVacinaResultado.Reforco;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class BaixaVacinaService {

    public static final String STATUS_APLICADA = "A";
    public static final String STATUS_PENDENTE = "P";

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Logger logger = LoggerFactory.getLogger(BaixaVacinaService.class);

    private final VacinaRepository vacinaRepository;

    public BaixaVacinaService(VacinaRepository vacinaRepository) {
        this.vacinaRepository = vacinaRepository;
    }

    @Transactional
    @CacheEvict(value = "inteligenciaPreventiva", allEntries = true)
    public BaixaVacinaResultado aplicar(Long vacinaId, LocalDate dataAplicacao) {
        Vacina vacina = vacinaRepository.buscarParaAplicacao(vacinaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vacina não encontrada com o ID " + vacinaId));

        if (STATUS_APLICADA.equals(vacina.getStatus())) {
            throw new RegraNegocioException(
                    "Vacina '" + vacina.getNome() + "' já consta como aplicada em "
                            + vacina.getData().format(FORMATO_DATA));
        }

        LocalDate data = dataAplicacao != null ? dataAplicacao : LocalDate.now();

        if (data.isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data de aplicação não pode estar no futuro");
        }

        vacina.setStatus(STATUS_APLICADA);
        vacina.setData(data);
        Vacina aplicada = vacinaRepository.save(vacina);

        BaixaVacinaResultado resultado = resolverReforco(aplicada, data);

        logger.info("Baixa de vacina: id={} nome='{}' petId={} aplicada em {} | proxima dose: {}",
                aplicada.getId(), aplicada.getNome(), aplicada.getPet().getId(), data.format(FORMATO_DATA),
                resultado.temProximaDose()
                        ? resultado.proximaDose().getData().format(FORMATO_DATA)
                        : resultado.reforco());

        return resultado;
    }

    /**
     * Agenda a próxima dose quando ela cabe e informa qual dos três desfechos
     * ocorreu, para que a mensagem ao usuário não confunda "sem intervalo de
     * reforço" com "reforço que já estava na agenda".
     */
    private BaixaVacinaResultado resolverReforco(Vacina aplicada, LocalDate dataAplicacao) {
        Integer intervalo = aplicada.getIntervaloReforcoDias();
        if (intervalo == null || intervalo <= 0) {
            return new BaixaVacinaResultado(aplicada, null, Reforco.SEM_INTERVALO);
        }

        LocalDate dataProximaDose = dataAplicacao.plusDays(intervalo);
        Long petId = aplicada.getPet().getId();

        boolean jaAgendada = vacinaRepository.existsByPetIdAndNomeAndStatusAndData(
                petId, aplicada.getNome(), STATUS_PENDENTE, dataProximaDose);
        if (jaAgendada) {
            return new BaixaVacinaResultado(aplicada, null, Reforco.JA_AGENDADO);
        }

        Vacina proximaDose = new Vacina();
        proximaDose.setNome(aplicada.getNome());
        proximaDose.setPet(aplicada.getPet());
        proximaDose.setData(dataProximaDose);
        proximaDose.setStatus(STATUS_PENDENTE);
        proximaDose.setIntervaloReforcoDias(intervalo);

        return new BaixaVacinaResultado(aplicada, vacinaRepository.save(proximaDose),
                Reforco.AGENDADO_AGORA);
    }
}
