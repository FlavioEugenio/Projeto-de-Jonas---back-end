package br.com.fichario.internal.producao.mapper;

import br.com.fichario.internal.pedido.entity.Pedido;
import br.com.fichario.internal.producao.dto.FilaProducaoItem;
import java.time.LocalDate;

/** Converte o Pedido para a linha da fila da producao. */
public final class ProducaoMapper {

    private ProducaoMapper() {
    }

    public static FilaProducaoItem paraItemFila(Pedido pedido) {
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
                pedido.getMateriaisEmFalta().stream().map(m -> m.getMaterial().getNome()).toList()
        );
    }
}
