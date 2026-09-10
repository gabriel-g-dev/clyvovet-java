package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.HealthDashboardResponseDTO;
import br.com.fiap.clyvovet.exception.RecursoNaoEncontradoException;
import br.com.fiap.clyvovet.model.Consulta;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.PetRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor de prevenção do ClyvoVet: cruza o calendário vacinal com o histórico
 * de consultas do pet e devolve um score de 0 a 100 com as recomendações que
 * o tutor precisa ver.
 */
@Service
public class IntelligenceService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final int DIAS_AVISO_PREVIO = 8;
    private static final int PENALIDADE_SEM_REGISTRO = 30;
    private static final int PENALIDADE_VACINA_ATRASADA = 15;
    private static final int PENALIDADE_CHECKUP_VENCIDO = 20;
    private static final int SCORE_MINIMO_EXCELENTE = 85;
    private static final int SCORE_MINIMO_ATENCAO = 50;

    private final PetRepository petRepository;

    public IntelligenceService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "inteligenciaPreventiva", key = "#petId")
    public HealthDashboardResponseDTO calcularPorPetId(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet não encontrado com o ID " + petId));
        return calcularInteligenciaPreventiva(pet);
    }

    public HealthDashboardResponseDTO calcularInteligenciaPreventiva(Pet pet) {
        Analise analise = new Analise();

        avaliarCalendarioVacinal(pet, analise);
        avaliarHistoricoDeConsultas(pet, analise);

        String status = classificarEngajamento(analise);
        if ("EXCELENTE".equals(status)) {
            analise.recomendar("🎉 Parabéns! O cuidado preventivo de " + pet.getNome() + " está em dia.");
        }

        return new HealthDashboardResponseDTO(
                pet.getId(), pet.getNome(), analise.score(), status, analise.recomendacoes());
    }

    private void avaliarCalendarioVacinal(Pet pet, Analise analise) {
        List<Vacina> vacinas = pet.getVacinas();

        if (vacinas == null || vacinas.isEmpty()) {
            analise.penalizar(PENALIDADE_SEM_REGISTRO);
            analise.recomendar("Nenhuma vacina registrada. Cadastre o cartão vacinal de " + pet.getNome()
                    + " para habilitar o monitoramento de imunidade.");
            return;
        }

        boolean algumAlerta = false;

        for (Vacina vacina : vacinas) {
            if (vacina.estaAtrasada()) {
                analise.penalizar(PENALIDADE_VACINA_ATRASADA);
                analise.marcarVacinaAtrasada();
                analise.recomendar("⚠️ ALERTA: a vacina '" + vacina.getNome() + "' está ATRASADA desde "
                        + vacina.getData().format(DATA) + ". Agende a aplicação imediatamente.");
                algumAlerta = true;
            } else if (vacina.venceEm(DIAS_AVISO_PREVIO)) {
                analise.recomendar("📅 AVISO PREVENTIVO: a vacina '" + vacina.getNome()
                        + "' está prevista para " + vacina.getData().format(DATA) + ".");
                algumAlerta = true;
            }
        }

        if (!algumAlerta) {
            analise.recomendar("✅ Todas as vacinas cadastradas de " + pet.getNome() + " estão em dia.");
        }
    }

    private void avaliarHistoricoDeConsultas(Pet pet, Analise analise) {
        List<Consulta> consultas = pet.getConsultas();

        if (consultas == null || consultas.isEmpty()) {
            analise.penalizar(PENALIDADE_SEM_REGISTRO);
            analise.recomendar("Nenhuma consulta registrada. Agende uma avaliação inicial de rotina para "
                    + pet.getNome() + ".");
            return;
        }

        LocalDateTime ultima = ultimaConsultaRealizada(consultas);
        if (ultima != null) {
            if (ultima.toLocalDate().isBefore(LocalDate.now().minusYears(1))) {
                analise.penalizar(PENALIDADE_CHECKUP_VENCIDO);
                analise.recomendar("⚠️ PREVENÇÃO: a última consulta de " + pet.getNome() + " foi em "
                        + ultima.format(DATA_HORA) + ", há mais de um ano. Agende o check-up anual.");
            } else {
                analise.recomendar("✅ Check-up de " + pet.getNome() + " está recente (última consulta em "
                        + ultima.format(DATA_HORA) + ").");
            }
        }

        Consulta proxima = proximaConsultaAgendada(consultas);
        if (proxima != null) {
            analise.recomendar("📅 AGENDA: há consulta marcada para " + proxima.getData().format(DATA_HORA)
                    + " com " + proxima.getFuncionario().getNome() + ".");
        }
    }

    private LocalDateTime ultimaConsultaRealizada(List<Consulta> consultas) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime ultima = null;

        for (Consulta consulta : consultas) {
            LocalDateTime data = consulta.getData();
            if (data != null && data.isBefore(agora) && (ultima == null || data.isAfter(ultima))) {
                ultima = data;
            }
        }
        return ultima;
    }

    private Consulta proximaConsultaAgendada(List<Consulta> consultas) {
        LocalDateTime agora = LocalDateTime.now();
        Consulta proxima = null;

        for (Consulta consulta : consultas) {
            LocalDateTime data = consulta.getData();
            boolean agendadaNoFuturo = data != null && !data.isBefore(agora) && "A".equals(consulta.getStatus());
            if (agendadaNoFuturo && (proxima == null || data.isBefore(proxima.getData()))) {
                proxima = consulta;
            }
        }
        return proxima;
    }

    /**
     * Vacina atrasada impede o status EXCELENTE mesmo com score alto: um pet
     * com pendência vencida não pode ser apresentado ao tutor como "em dia".
     */
    private String classificarEngajamento(Analise analise) {
        if (analise.score() < SCORE_MINIMO_ATENCAO) {
            return "CRÍTICO";
        }
        if (analise.temVacinaAtrasada() || analise.score() < SCORE_MINIMO_EXCELENTE) {
            return "ATENÇÃO";
        }
        return "EXCELENTE";
    }

    private static class Analise {

        private int score = 100;
        private boolean vacinaAtrasada;
        private final List<String> recomendacoes = new ArrayList<>();

        void penalizar(int pontos) {
            score = Math.max(0, score - pontos);
        }

        void marcarVacinaAtrasada() {
            vacinaAtrasada = true;
        }

        void recomendar(String recomendacao) {
            recomendacoes.add(recomendacao);
        }

        int score() {
            return score;
        }

        boolean temVacinaAtrasada() {
            return vacinaAtrasada;
        }

        List<String> recomendacoes() {
            return recomendacoes;
        }
    }
}
