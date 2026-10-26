package br.com.fiap.clyvovet;

import br.com.fiap.clyvovet.dto.HealthDashboardResponseDTO;
import br.com.fiap.clyvovet.model.Consulta;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.service.IntelligenceService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntelligenceServiceTest {

    private final IntelligenceService service = new IntelligenceService(null);

    @Test
    void consultaCanceladaNaoContaComoCheckup() {
        Pet pet = petComVacinaEmDia();
        pet.setConsultas(List.of(consulta("C", LocalDateTime.now().minusDays(2))));

        HealthDashboardResponseDTO resultado = service.calcularInteligenciaPreventiva(pet);

        assertEquals(70, resultado.getScoreSaude());
        assertEquals("ATENÇÃO", resultado.getStatusEngajamento());
        assertTrue(resultado.getRecomendacoes().stream().anyMatch(r -> r.contains("Nenhuma consulta realizada")));
    }

    @Test
    void consultaAgendadaNaoSubstituiConsultaRealizada() {
        Pet pet = petComVacinaEmDia();
        pet.setConsultas(List.of(consulta("A", LocalDateTime.now().plusDays(5))));

        HealthDashboardResponseDTO resultado = service.calcularInteligenciaPreventiva(pet);

        assertEquals(70, resultado.getScoreSaude());
        assertTrue(resultado.getRecomendacoes().stream().anyMatch(r -> r.contains("AGENDA")));
    }

    @Test
    void cancelamentoRecenteNaoOcultaCheckupVencido() {
        Pet pet = petComVacinaEmDia();
        pet.setConsultas(List.of(consulta("R", LocalDateTime.now().minusYears(2)),
                consulta("C", LocalDateTime.now().minusDays(1)),
                consulta("A", LocalDateTime.now().minusDays(3))));

        HealthDashboardResponseDTO resultado = service.calcularInteligenciaPreventiva(pet);

        assertEquals(80, resultado.getScoreSaude());
        assertTrue(resultado.getRecomendacoes().stream().anyMatch(r -> r.contains("há mais de um ano")));
    }

    @Test
    void vacinaAtrasadaImpedeExcelenteMesmoComScore85() {
        Pet pet = petComVacinaEmDia();
        pet.setVacinas(List.of(vacina("P", LocalDate.now().minusDays(1))));
        pet.setConsultas(List.of(consulta("R", LocalDateTime.now().minusDays(1))));

        HealthDashboardResponseDTO resultado = service.calcularInteligenciaPreventiva(pet);

        assertEquals(85, resultado.getScoreSaude());
        assertEquals("ATENÇÃO", resultado.getStatusEngajamento());
    }

    @Test
    void historicoRealizadoRecentePermiteExcelente() {
        Pet pet = petComVacinaEmDia();
        pet.setConsultas(List.of(consulta("R", LocalDateTime.now().minusDays(30))));

        HealthDashboardResponseDTO resultado = service.calcularInteligenciaPreventiva(pet);

        assertEquals(100, resultado.getScoreSaude());
        assertEquals("EXCELENTE", resultado.getStatusEngajamento());
    }

    @Test
    void scoreNaoFicaNegativoComMuitasPendencias() {
        Pet pet = petComVacinaEmDia();
        pet.setVacinas(java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> vacina("P", LocalDate.now().minusDays(1))).toList());
        pet.setConsultas(List.of());

        HealthDashboardResponseDTO resultado = service.calcularInteligenciaPreventiva(pet);

        assertEquals(0, resultado.getScoreSaude());
        assertEquals("CRÍTICO", resultado.getStatusEngajamento());
    }

    private Pet petComVacinaEmDia() {
        Pet pet = new Pet();
        pet.setId(99L);
        pet.setNome("Teste");
        pet.setVacinas(List.of(vacina("A", LocalDate.now().minusDays(20))));
        return pet;
    }

    private Vacina vacina(String status, LocalDate data) {
        Vacina vacina = new Vacina();
        vacina.setNome("Antirrábica");
        vacina.setStatus(status);
        vacina.setData(data);
        return vacina;
    }

    private Consulta consulta(String status, LocalDateTime data) {
        Consulta consulta = new Consulta();
        consulta.setStatus(status);
        consulta.setData(data);
        return consulta;
    }
}
