package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.model.Vacina;

/**
 * O reforço tem três desfechos possíveis e cada um dá uma mensagem diferente a
 * quem aplicou a dose: a vacina não tem intervalo cadastrado, a próxima dose já
 * constava na agenda, ou ela acabou de ser criada.
 */
public record BaixaVacinaResultado(Vacina aplicada, Vacina proximaDose, Reforco reforco) {

    public enum Reforco {
        SEM_INTERVALO,
        JA_AGENDADO,
        AGENDADO_AGORA
    }

    public boolean temProximaDose() {
        return proximaDose != null;
    }
}
