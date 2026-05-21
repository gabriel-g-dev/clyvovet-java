package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.HealthDashboardResponseDTO;
import br.com.fiap.clyvovet.model.Consulta;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.model.Vacina;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class IntelligenceService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public HealthDashboardResponseDTO calcularInteligenciaPreventiva(Pet pet) {
        Long petId = pet.getId();
        String petNome = pet.getNome();
        int score = 100;
        List<String> recomendacoes = new ArrayList<>();
        LocalDate hoje = LocalDate.now();

        // 1. Análise do Calendário Vacinal
        List<Vacina> vacinas = pet.getVacinas();
        if (vacinas == null || vacinas.isEmpty()) {
            score -= 30;
            recomendacoes.add("Nenhuma vacina registrada. É fundamental registrar o cartão vacinal de " + petNome + " para habilitar o monitoramento de imunidade.");
        } else {
            int vacinasAtrasadas = 0;
            int vacinasPendentesProximas = 0;

            for (Vacina v : vacinas) {
                if (!v.isAplicada()) {
                    if (v.getProximaDose() != null) {
                        if (v.getProximaDose().isBefore(hoje)) {
                            vacinasAtrasadas++;
                            score -= 15;
                            recomendacoes.add("⚠️ ALERTA: A vacina '" + v.getNome() + "' está ATRASADA desde " + v.getProximaDose().format(DATE_FORMATTER) + ". Agende a aplicação imediatamente!");
                        } else if (v.getProximaDose().isBefore(hoje.plusDays(8))) {
                            vacinasPendentesProximas++;
                            recomendacoes.add("📅 AVISO PREVENTIVO: A vacina '" + v.getNome() + "' deve ser aplicada nos próximos dias (prevista para " + v.getProximaDose().format(DATE_FORMATTER) + ").");
                        }
                    }
                }
            }

            if (vacinasAtrasadas == 0 && vacinasPendentesProximas == 0) {
                recomendacoes.add("✅ Todas as vacinas cadastradas de " + petNome + " estão em dia.");
            }
        }

        // 2. Análise do Histórico de Consultas
        List<Consulta> consultas = pet.getConsultas();
        if (consultas == null || consultas.isEmpty()) {
            score -= 30;
            recomendacoes.add("Nenhuma consulta registrada. Recomendamos agendar uma consulta inicial de rotina para avaliação geral de " + petNome + ".");
        } else {
            LocalDateTime agora = LocalDateTime.now();
            LocalDateTime ultimaConsultaData = null;
            Consulta proximaConsulta = null;

            for (Consulta c : consultas) {
                if (c.getData() != null) {
                    if (c.getData().isBefore(agora)) {
                        if (ultimaConsultaData == null || c.getData().isAfter(ultimaConsultaData)) {
                            ultimaConsultaData = c.getData();
                        }
                    } else {
                        if (proximaConsulta == null || c.getData().isBefore(proximaConsulta.getData())) {
                            proximaConsulta = c;
                        }
                    }
                }
            }

            if (ultimaConsultaData != null) {
                LocalDate ultimaConsultaDataLD = ultimaConsultaData.toLocalDate();
                if (ultimaConsultaDataLD.isBefore(hoje.minusYears(1))) {
                    score -= 20;
                    recomendacoes.add("⚠️ PREVENÇÃO: A última consulta de rotina de " + petNome + " foi em " + ultimaConsultaData.format(DATETIME_FORMATTER) + " (há mais de 1 ano). Agende um check-up anual preventivo.");
                } else {
                    recomendacoes.add("✅ Check-up preventivo de " + petNome + " está recente (última consulta em " + ultimaConsultaData.format(DATETIME_FORMATTER) + ").");
                }
            }

            if (proximaConsulta != null) {
                recomendacoes.add("📅 AGENDA: Há uma consulta agendada para " + proximaConsulta.getData().format(DATETIME_FORMATTER) + " com o(a) Dr(a). " + proximaConsulta.getVeterinario() + ".");
            }
        }

        // Normalização do score (min 0, max 100)
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        // Status de engajamento preventivo
        String status;
        if (score >= 85) {
            status = "EXCELENTE";
            if (recomendacoes.stream().noneMatch(r -> r.startsWith("⚠️") || r.startsWith("Nenhuma"))) {
                recomendacoes.add("🎉 Parabéns! O tutor está prestando um cuidado contínuo, preventivo e inteligente excelente para " + petNome + "!");
            }
        } else if (score >= 50) {
            status = "ATENÇÃO";
        } else {
            status = "CRÍTICO";
        }

        return new HealthDashboardResponseDTO(petId, petNome, score, status, recomendacoes);
    }
}
