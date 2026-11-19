package br.com.fichario.internal.pedido.dto;

import br.com.fichario.internal.cliente.dto.ClienteResponse;
import br.com.fichario.internal.pedido.entity.CanalOrigem;
import br.com.fichario.internal.pedido.entity.StatusPedido;
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
    }

    public record EventoItem(LocalDateTime ocorridoEm, String descricao, String autor) {
    }
}
