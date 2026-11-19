package br.com.fichario.service;

/** Lancada quando o id informado nao existe no banco. Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " " + id + " nao encontrado.");
    }

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
