package br.com.fichario.config.exception;

import br.com.fichario.shared.exception.RecursoNaoEncontradoException;
import br.com.fichario.shared.exception.RegraDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/** Transforma as excecoes em respostas JSON com mensagem em portugues. */
@RestControllerAdvice
public class TratadorDeErros {

    public record Erro(int status, String mensagem, List<String> detalhes) {
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Erro> regraDeNegocio(RegraDeNegocioException e) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), List.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Erro> naoEncontrado(RecursoNaoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> camposInvalidos(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Dados invalidos.", detalhes);
    }

    private ResponseEntity<Erro> responder(HttpStatus status, String mensagem, List<String> detalhes) {
        return ResponseEntity.status(status).body(new Erro(status.value(), mensagem, detalhes));
    }
}
