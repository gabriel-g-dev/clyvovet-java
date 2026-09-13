package br.com.fiap.clyvovet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * O {@link GlobalExceptionHandler} monta o corpo JSON desta exceção para a API.
 * O @ResponseStatus cobre o painel web, que não passa pelo advice: sem ele, um
 * ID inexistente na URL viraria 500 em vez de 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
