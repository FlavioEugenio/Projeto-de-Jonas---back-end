package br.com.fichario.domain.model;

import br.com.fichario.domain.exception.RegraDeNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Validacoes de entrada compartilhadas pelas entidades do dominio. */
final class Validacao {

    private Validacao() {
    }

    static <T> T obrigatorio(T valor, String campo) {
        if (valor == null) {
            throw new RegraDeNegocioException(campo + " e obrigatorio.");
        }
        return valor;
    }

    static String texto(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException(campo + " e obrigatorio.");
        }
        return textoOpcional(valor, campo, tamanhoMaximo);
    }

    static String textoOpcional(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpo = valor.strip();
        if (limpo.length() > tamanhoMaximo) {
            throw new RegraDeNegocioException(campo + " passa de " + tamanhoMaximo + " caracteres.");
        }
        return limpo;
    }

    /** Valor em reais com no maximo duas casas decimais. */
    static BigDecimal dinheiro(BigDecimal valor, String campo) {
        obrigatorio(valor, campo);
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new RegraDeNegocioException(campo + " aceita no maximo duas casas decimais.");
        }
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }

    static BigDecimal dinheiroPositivo(BigDecimal valor, String campo) {
        BigDecimal v = dinheiro(valor, campo);
        if (v.signum() <= 0) {
            throw new RegraDeNegocioException(campo + " deve ser maior que zero.");
        }
        return v;
    }
}
