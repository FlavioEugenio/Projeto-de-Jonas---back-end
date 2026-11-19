package br.com.fichario.internal.material.dto;

import java.time.LocalDate;

/** Um material que falta para um pedido. */
public record ItemCompraResponse(String material, Long pedidoId, LocalDate dataEntrega) {
}
