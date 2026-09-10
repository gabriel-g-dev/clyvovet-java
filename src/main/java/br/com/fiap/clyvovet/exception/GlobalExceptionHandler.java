package br.com.fiap.clyvovet.exception;

import br.com.fiap.clyvovet.dto.ErroResponseDTO;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> handleValidacao(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.merge(erro.getField(), erro.getDefaultMessage(), (a, b) -> a + "; " + b);
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(erro -> campos.put(erro.getObjectName(), erro.getDefaultMessage()));

        return montar(HttpStatus.BAD_REQUEST,
                "Falha de validação em " + campos.size() + " campo(s)",
                request, campos);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResponseDTO> handleConstraintViolation(ConstraintViolationException ex,
                                                                     HttpServletRequest request) {
        Map<String, String> campos = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violacao -> violacao.getPropertyPath().toString(),
                        jakarta.validation.ConstraintViolation::getMessage,
                        (a, b) -> a + "; " + b,
                        LinkedHashMap::new));

        return montar(HttpStatus.BAD_REQUEST, "Falha de validação nos parâmetros", request, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> handleCorpoIlegivel(HttpMessageNotReadableException ex,
                                                               HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, descreverCorpoIlegivel(ex), request, null);
    }

    /** Aponta o campo culpado quando o Jackson sabe qual é, em vez de um erro genérico. */
    private String descreverCorpoIlegivel(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException formato && !formato.getPath().isEmpty()) {
            String campo = formato.getPath().get(formato.getPath().size() - 1).getFieldName();
            String esperado = LocalDate.class.equals(formato.getTargetType())
                    ? " Datas usam o formato ISO (aaaa-MM-dd)."
                    : "";
            return "Campo '" + campo + "' recebeu um valor em formato inválido." + esperado;
        }
        return "Corpo da requisição ausente ou malformado";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> handleTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                              HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST,
                "Parâmetro '" + ex.getName() + "' recebeu um valor inválido", request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponseDTO> handleAutenticacao(AuthenticationException ex,
                                                              HttpServletRequest request) {
        return montar(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponseDTO> handleAcessoNegado(AccessDeniedException ex,
                                                              HttpServletRequest request) {
        return montar(HttpStatus.FORBIDDEN, "Acesso negado para este recurso", request, null);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> handleNaoEncontrado(RecursoNaoEncontradoException ex,
                                                               HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> handleRegraNegocio(RegraNegocioException ex,
                                                              HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> handleIntegridade(DataIntegrityViolationException ex,
                                                             HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT,
                "Operação viola uma restrição de integridade do banco de dados", request, null);
    }

    private ResponseEntity<ErroResponseDTO> montar(HttpStatus status, String mensagem,
                                                   HttpServletRequest request, Map<String, String> campos) {
        ErroResponseDTO corpo = new ErroResponseDTO(
                status.value(), status.getReasonPhrase(), mensagem, request.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
