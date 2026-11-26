package br.com.fichario.shared.util;

import br.com.fichario.shared.exception.RegraDeNegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Validacao {

    private Validacao() {
    }

    public static <T> T obrigatorio(T valor, String campo) {
        if (valor == null) {
            throw new RegraDeNegocioException(campo + " e obrigatorio.");
        }
        return valor;
    }

    public static String texto(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException(campo + " e obrigatorio.");
        }
        return textoOpcional(valor, campo, tamanhoMaximo);
    }

    private static String textoOpcional(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpo = valor.strip();
        if (limpo.length() > tamanhoMaximo) {
            throw new RegraDeNegocioException(campo + " passa de " + tamanhoMaximo + " caracteres.");
        }
        return limpo;
    }

    public static BigDecimal dinheiro(BigDecimal valor, String campo) {
        obrigatorio(valor, campo);
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new RegraDeNegocioException(campo + " aceita no maximo duas casas decimais.");
        }
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal dinheiroPositivo(BigDecimal valor, String campo) {
        BigDecimal v = dinheiro(valor, campo);
        if (v.signum() <= 0) {
            throw new RegraDeNegocioException(campo + " deve ser maior que zero.");
        }
        return v;
    }
}
