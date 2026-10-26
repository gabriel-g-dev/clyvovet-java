package br.com.fiap.clyvovet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(assignableTypes = br.com.fiap.clyvovet.controller.WebController.class)
public class WebExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView recursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return erro(HttpStatus.NOT_FOUND, "Registro não encontrado", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView acessoNegado(AccessDeniedException ex) {
        return erro(HttpStatus.FORBIDDEN, "Acesso restrito", "Seu perfil não permite acessar este registro.");
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ModelAndView conflito(RegraNegocioException ex) {
        return erro(HttpStatus.CONFLICT, "Não foi possível concluir a operação", ex.getMessage());
    }

    private ModelAndView erro(HttpStatus status, String titulo, String mensagem) {
        ModelAndView pagina = new ModelAndView("erro");
        pagina.setStatus(status);
        pagina.addObject("status", status.value());
        pagina.addObject("titulo", titulo);
        pagina.addObject("mensagem", mensagem);
        return pagina;
    }
}
