package br.com.fichario.dto;

import br.com.fichario.domain.model.Pedido;
import br.com.fichario.domain.model.StatusPedido;

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

    public static FilaProducaoItem de(Pedido pedido) {
        return new FilaProducaoItem(
                pedido.getId(),
                pedido.getCliente().getNome(),
                pedido.getProduto().getNome(),
                pedido.getQuantidade(),
                pedido.getPersonalizacao(),
                pedido.getDataEntrega(),
                pedido.getStatus(),
                pedido.estaAtrasado(LocalDate.now()),
                pedido.possuiAlteracoesPendentes(),
                pedido.getMateriaisEmFalta().stream().map(m -> m.getMaterial().getNome()).toList());
    }
}
