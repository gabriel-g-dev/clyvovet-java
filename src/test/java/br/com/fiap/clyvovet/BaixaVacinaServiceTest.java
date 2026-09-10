package br.com.fiap.clyvovet;

import br.com.fiap.clyvovet.exception.RegraNegocioException;
import br.com.fiap.clyvovet.model.Vacina;
import br.com.fiap.clyvovet.repository.VacinaRepository;
import br.com.fiap.clyvovet.service.BaixaVacinaResultado;
import br.com.fiap.clyvovet.service.BaixaVacinaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fluxo de negócio da baixa de vacina: aplicar dá baixa, agenda o reforço pelo
 * intervalo da própria vacina e impede aplicar duas vezes.
 */
@SpringBootTest
@Transactional
class BaixaVacinaServiceTest {

    @Autowired
    private BaixaVacinaService baixaVacinaService;

    @Autowired
    private VacinaRepository vacinaRepository;

    @Test
    @DisplayName("aplicar dá baixa e agenda a próxima dose pelo intervalo de reforço")
    void aplicarAgendaProximaDose() {
        Vacina pendente = primeiraPendenteComReforco();
        LocalDate hoje = LocalDate.now();

        BaixaVacinaResultado resultado = baixaVacinaService.aplicar(pendente.getId(), hoje);

        assertEquals(BaixaVacinaService.STATUS_APLICADA, resultado.aplicada().getStatus());
        assertEquals(hoje, resultado.aplicada().getData());

        assertTrue(resultado.temProximaDose());
        Vacina proxima = resultado.proximaDose();
        assertNotNull(proxima.getId());
        assertEquals(BaixaVacinaService.STATUS_PENDENTE, proxima.getStatus());
        assertEquals(hoje.plusDays(pendente.getIntervaloReforcoDias()), proxima.getData());
    }

    @Test
    @DisplayName("aplicar duas vezes é bloqueado pela regra de negócio")
    void naoAplicaDuasVezes() {
        Vacina pendente = primeiraPendenteComReforco();
        baixaVacinaService.aplicar(pendente.getId(), LocalDate.now());

        assertThrows(RegraNegocioException.class,
                () -> baixaVacinaService.aplicar(pendente.getId(), LocalDate.now()));
    }

    @Test
    @DisplayName("data de aplicação no futuro é recusada")
    void naoAceitaDataFutura() {
        Vacina pendente = primeiraPendenteComReforco();

        assertThrows(RegraNegocioException.class,
                () -> baixaVacinaService.aplicar(pendente.getId(), LocalDate.now().plusDays(1)));
    }

    private Vacina primeiraPendenteComReforco() {
        List<Vacina> pendentes = vacinaRepository.findByStatus(BaixaVacinaService.STATUS_PENDENTE);
        return pendentes.stream()
                .filter(v -> v.getIntervaloReforcoDias() != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "A migration V2/V3 deve deixar ao menos uma vacina pendente com reforço"));
    }
}
