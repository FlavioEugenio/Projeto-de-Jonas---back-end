package br.com.fichario.internal.pedido.mapper;

import br.com.fichario.internal.cliente.mapper.ClienteMapper;
import br.com.fichario.internal.pedido.dto.PedidoDetalheResponse.EventoItem;
import br.com.fichario.internal.pedido.dto.PedidoDetalheResponse.MaterialItem;
import br.com.fichario.internal.pedido.dto.PedidoDetalheResponse;
import br.com.fichario.internal.pedido.dto.PedidoResumoResponse;
import br.com.fichario.internal.pedido.entity.EventoPedido;
import br.com.fichario.internal.pedido.entity.MaterialPedido;
import br.com.fichario.internal.pedido.entity.Pedido;

import java.time.LocalDate;

/** Converte o Pedido para a ficha completa e para a linha da listagem. */
public final class PedidoMapper {

    private PedidoMapper() {
    }

    public static PedidoDetalheResponse paraDetalhe(Pedido pedido) {
        return new PedidoDetalheResponse(
                pedido.getId(),
                ClienteMapper.paraResposta(pedido.getCliente()),
                pedido.getProduto().getNome(),
                pedido.getQuantidade(),
                pedido.getPersonalizacao(),
                pedido.getObservacoes(),
                pedido.getDataEntrega(),
                pedido.getCanal(),
                pedido.getStatus(),
                pedido.isConfirmadoPeloCliente(),
                pedido.getValorTotal(),
                pedido.getValorPago(),
                pedido.getSaldo(),
                pedido.possuiAlteracoesPendentes(),
                pedido.getMateriais().stream().map(PedidoMapper::paraMaterialItem).toList(),
                pedido.getCriadoPor().getNome(),
                pedido.getCriadoEm(),
                pedido.getHistorico().stream().map(PedidoMapper::paraEventoItem).toList()
        );
    }

    public static PedidoResumoResponse paraResumo(Pedido pedido) {
        return new PedidoResumoResponse(
                pedido.getId(),
                pedido.getCliente().getNome(),
                pedido.getProduto().getNome(),
                pedido.getQuantidade(),
                pedido.getDataEntrega(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getSaldo(),
                pedido.possuiAlteracoesPendentes(),
                pedido.estaAtrasado(LocalDate.now())
        );
    }

    private static MaterialItem paraMaterialItem(MaterialPedido item) {
        return new MaterialItem(item.getMaterial().getNome(), item.isDisponivel());
    }

    private static EventoItem paraEventoItem(EventoPedido evento) {
        return new EventoItem(evento.getOcorridoEm(), evento.getDescricao(), evento.getAutor().getNome());
    }
}
