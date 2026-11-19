package br.com.fichario.dto;

import br.com.fichario.domain.model.CanalOrigem;
import br.com.fichario.domain.model.EventoPedido;
import br.com.fichario.domain.model.MaterialPedido;
import br.com.fichario.domain.model.Pedido;
import br.com.fichario.domain.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Ficha completa do pedido: a fonte unica que o atendente consulta para responder o cliente. */
public record PedidoDetalheResponse(
        Long id,
        ClienteResponse cliente,
        String produto,
        int quantidade,
        String personalizacao,
        String observacoes,
        LocalDate dataEntrega,
        CanalOrigem canal,
        StatusPedido status,
        boolean confirmadoPeloCliente,
        BigDecimal valorTotal,
        BigDecimal valorPago,
        BigDecimal saldo,
        boolean alteracoesPendentes,
        List<MaterialItem> materiais,
        String criadoPor,
        LocalDateTime criadoEm,
        List<EventoItem> historico) {

    public record MaterialItem(String nome, boolean disponivel) {

        static MaterialItem de(MaterialPedido item) {
            return new MaterialItem(item.getMaterial().getNome(), item.isDisponivel());
        }
    }

    public record EventoItem(LocalDateTime ocorridoEm, String descricao, String autor) {

        static EventoItem de(EventoPedido evento) {
            return new EventoItem(evento.getOcorridoEm(), evento.getDescricao(), evento.getAutor().getNome());
        }
    }

    public static PedidoDetalheResponse de(Pedido pedido) {
        return new PedidoDetalheResponse(
                pedido.getId(),
                ClienteResponse.de(pedido.getCliente()),
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
                pedido.getMateriais().stream().map(MaterialItem::de).toList(),
                pedido.getCriadoPor().getNome(),
                pedido.getCriadoEm(),
                pedido.getHistorico().stream().map(EventoItem::de).toList());
    }
}
