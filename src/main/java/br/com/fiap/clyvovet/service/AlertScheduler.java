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

    /**
     * Executa automaticamente a cada 2 minutos (com delay inicial de 15 segundos após o boot da aplicação)
     * para fins de demonstração acadêmica e testes práticos do professor.
     * Em ambiente de produção, este serviço rodaria diariamente (ex: cron = "0 0 8 * * *").
     */
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
                    if (!v.isAplicada() && v.getProximaDose() != null) {
                        if (v.getProximaDose().isBefore(hoje)) {
                            // Alerta de Vacina Atrasada
                            logger.warn(">>> [DISPARO PREVENTIVO - ATRASO] Tutor: {} | Pet: {} | Vacina: {} atrasada desde {} | Canal de Envio: E-mail para [{}]",
                                    pet.getTutor().getNome(), pet.getNome(), v.getNome(), v.getProximaDose(), pet.getTutor().getEmail());
                            totalAlertasDisparados++;
                        } else if (v.getProximaDose().isBefore(hoje.plusDays(8))) {
                            // Alerta de Próxima Vacina (7 dias antes)
                            logger.info(">>> [DISPARO PREVENTIVO - AGENDA] Tutor: {} | Pet: {} | Vacina: {} agendada para {} | Canal de Envio: SMS para [{}]",
                                    pet.getTutor().getNome(), pet.getNome(), v.getNome(), v.getProximaDose(), pet.getTutor().getTelefone());
                            totalAlertasDisparados++;
                        }
                    }
                }
            }
        }
        
        logger.info("=== [CLYVOVET CARE INTELLIGENCE] Varredura preventiva concluída. Notificações disparadas: {} ===", totalAlertasDisparados);
    }
}
