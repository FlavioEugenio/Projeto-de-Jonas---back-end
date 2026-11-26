package br.com.fichario.internal.material.dto;

import java.time.LocalDate;

public record ItemCompraResponse(String material, Long pedidoId, LocalDate dataEntrega) {
}
