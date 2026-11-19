package br.com.fichario.shared.exception;

/**
 * Lancada quando uma operacao viola uma regra de negocio do dominio.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
