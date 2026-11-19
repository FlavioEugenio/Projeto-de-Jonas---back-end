package br.com.fichario.internal.producao.dto;

import br.com.fichario.internal.pedido.entity.StatusPedido;
import java.time.LocalDate;
import java.util.List;

/**
 * Linha do quadro da producao. Mostra o que impede o pedido de andar:
 * alteracao nao conferida ou material faltando.
 */
public record FilaProducaoItem(
        Long id,
        String cliente,
        String produto,
        int quantidade,
        String personalizacao,
        LocalDate dataEntrega,
        StatusPedido status,
        boolean atrasado,
        boolean alteracoesPendentes,
        List<String> materiaisEmFalta) {
}
