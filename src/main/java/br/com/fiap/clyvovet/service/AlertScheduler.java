package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.VacinaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Registra pendências no log. Não envia e-mail nem SMS. */
@Component
public class AlertScheduler {

    private static final int DIAS_AVISO_PREVIO = 8;
    private static final Logger logger = LoggerFactory.getLogger(AlertScheduler.class);

    private final VacinaRepository vacinaRepository;

    public AlertScheduler(VacinaRepository vacinaRepository) {
        this.vacinaRepository = vacinaRepository;
    }

    @Scheduled(initialDelay = 15000, fixedRate = 120000)
    @Transactional(readOnly = true)
    public void processarAlertasPreventivosAgendados() {
        logger.info("[ClyvoVet] Iniciando varredura preventiva de rotina");

        int alertas = 0;
        for (Vacina vacina : vacinaRepository.findByStatusAndDataBefore(
                BaixaVacinaService.STATUS_PENDENTE, LocalDate.now().plusDays(DIAS_AVISO_PREVIO))) {
            if (vacina.estaAtrasada()) {
                logger.warn("[ATRASO] petId={} vacinaId={} vencimento={}",
                        vacina.getPet().getId(), vacina.getId(), vacina.getData());
                alertas++;
            } else if (vacina.venceEm(DIAS_AVISO_PREVIO)) {
                logger.info("[AGENDA] petId={} vacinaId={} dataPrevista={}",
                        vacina.getPet().getId(), vacina.getId(), vacina.getData());
                alertas++;
            }
        }
        logger.info("[ClyvoVet] Varredura preventiva concluída. Avisos registrados no log: {}", alertas);
    }
}
