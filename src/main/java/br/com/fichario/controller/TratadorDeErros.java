package br.com.fichario.controller;

import br.com.fichario.domain.exception.RegraDeNegocioException;
import br.com.fichario.service.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/** Transforma as excecoes em respostas JSON com mensagem em portugues. Vale para todos os controllers. */
@RestControllerAdvice
public class TratadorDeErros {

    public record Erro(int status, String mensagem, List<String> detalhes) {
    }

    /** Regra do dominio violada: perfil sem permissao, telefone repetido, data no passado... */
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Erro> regraDeNegocio(RegraDeNegocioException e) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), List.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Erro> naoEncontrado(RecursoNaoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    /** Campos que falharam no Bean Validation dos DTOs (@NotNull, @Size...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> camposInvalidos(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .sorted()
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Dados invalidos.", detalhes);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Erro> cabecalhoAusente(MissingRequestHeaderException e) {
        return responder(HttpStatus.BAD_REQUEST,
                "Informe o cabecalho " + e.getHeaderName() + " com o id do usuario.", List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Erro> jsonInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "JSON invalido. Datas no formato AAAA-MM-DD e canal "
                + "WHATSAPP, INSTAGRAM ou PRESENCIAL.", List.of());
    }

    private static ResponseEntity<Erro> responder(HttpStatus status, String mensagem, List<String> detalhes) {
        return ResponseEntity.status(status).body(new Erro(status.value(), mensagem, detalhes));
    }
}
