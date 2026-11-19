package br.com.fichario.internal.pedido.dto;

import br.com.fichario.internal.pedido.entity.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Situacao do pedido. E a resposta de todos os endpoints que mexem num pedido. */
public record PedidoResponse(
        Long id,
        String cliente,
        String produto,
        int quantidade,
        String personalizacao,
        LocalDate dataEntrega,
        StatusPedido status,
        BigDecimal valorTotal,
        BigDecimal saldo,
        boolean alteracoesPendentes,
        List<MaterialItem> materiais) {

    public record MaterialItem(Long materialId, String nome, boolean disponivel) {
    }
}
