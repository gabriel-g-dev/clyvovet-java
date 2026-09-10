package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.model.Vacina;

public record BaixaVacinaResultado(Vacina aplicada, Vacina proximaDose) {

    public boolean temProximaDose() {
        return proximaDose != null;
    }
}
