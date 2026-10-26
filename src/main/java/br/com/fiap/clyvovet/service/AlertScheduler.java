package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.PetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Registra pendências no log. Não envia e-mail nem SMS. */
@Component
public class AlertScheduler {

    private static final int DIAS_AVISO_PREVIO = 8;
    private static final Logger logger = LoggerFactory.getLogger(AlertScheduler.class);

    private final PetRepository petRepository;

    public AlertScheduler(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Scheduled(initialDelay = 15000, fixedRate = 120000)
    @Transactional(readOnly = true)
    public void processarAlertasPreventivosAgendados() {
        logger.info("[ClyvoVet] Iniciando varredura preventiva de rotina");

        int alertas = 0;
        for (Pet pet : petRepository.findAll()) {
            alertas += notificarPendenciasDoPet(pet);
        }

        logger.info("[ClyvoVet] Varredura preventiva concluída. Avisos registrados no log: {}", alertas);
    }

    private int notificarPendenciasDoPet(Pet pet) {
        List<Vacina> vacinas = pet.getVacinas();
        if (vacinas == null) {
            return 0;
        }

        int alertas = 0;
        for (Vacina vacina : vacinas) {
            if (vacina.estaAtrasada()) {
                logger.warn("[ATRASO] petId={} vacinaId={} vencimento={}",
                        pet.getId(), vacina.getId(), vacina.getData());
                alertas++;
            } else if (vacina.venceEm(DIAS_AVISO_PREVIO)) {
                logger.info("[AGENDA] petId={} vacinaId={} dataPrevista={}",
                        pet.getId(), vacina.getId(), vacina.getData());
                alertas++;
            }
        }
        return alertas;
    }
}
