package br.com.fichario.internal.pedido.mapper;

import br.com.fichario.internal.pedido.dto.PedidoResponse;
import br.com.fichario.internal.pedido.entity.MaterialPedido;
import br.com.fichario.internal.pedido.entity.Pedido;

public final class PedidoMapper {

    private PedidoMapper() {
    }

    public static PedidoResponse paraResposta(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCliente().getNome(),
                pedido.getProduto().getNome(),
                pedido.getQuantidade(),
                pedido.getPersonalizacao(),
                pedido.getDataEntrega(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getSaldo(),
                pedido.possuiAlteracoesPendentes(),
                pedido.getMateriais().stream().map(PedidoMapper::paraItem).toList()
        );
    }

    private static PedidoResponse.MaterialItem paraItem(MaterialPedido item) {
        return new PedidoResponse.MaterialItem(
                item.getMaterial().getId(), item.getMaterial().getNome(), item.isDisponivel());
    }
}
