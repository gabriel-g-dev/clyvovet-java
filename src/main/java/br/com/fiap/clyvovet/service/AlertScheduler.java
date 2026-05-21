package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.PetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class AlertScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AlertScheduler.class);

    @Autowired
    private PetRepository petRepository;

    @Scheduled(initialDelay = 15000, fixedRate = 120000)
    @Transactional
    public void processarAlertasPreventivosAgendados() {
        logger.info("=== [CLYVOVET CARE INTELLIGENCE] Iniciando varredura preventiva de rotina... ===");

        List<Pet> pets = petRepository.findAll();
        LocalDate hoje = LocalDate.now();
        int totalAlertasDisparados = 0;

        for (Pet pet : pets) {
            if (pet.getVacinas() != null) {
                for (Vacina v : pet.getVacinas()) {
                    if ("P".equals(v.getStatus()) && v.getData() != null) {
                        if (v.getData().isBefore(hoje)) {
                            // Alerta de Vacina Atrasada
                            logger.warn(">>> [DISPARO PREVENTIVO - ATRASO] Dono: {} | Pet: {} | Vacina: {} atrasada desde {} | Canal de Envio: E-mail para [{}]",
                                    pet.getDono().getNome(), pet.getNome(), v.getNome(), v.getData(), pet.getDono().getEmail());
                            totalAlertasDisparados++;
                        } else if (v.getData().isBefore(hoje.plusDays(8))) {
                            // Alerta de Próxima Vacina (7 dias antes)
                            logger.info(">>> [DISPARO PREVENTIVO - AGENDA] Dono: {} | Pet: {} | Vacina: {} agendada para {} | Canal de Envio: SMS para [{}]",
                                    pet.getDono().getNome(), pet.getNome(), v.getNome(), v.getData(), pet.getDono().getTelefone());
                            totalAlertasDisparados++;
                        }
                    }
                }
            }
        }

        logger.info("=== [CLYVOVET CARE INTELLIGENCE] Varredura preventiva concluída. Notificações disparadas: {} ===", totalAlertasDisparados);
    }
}
