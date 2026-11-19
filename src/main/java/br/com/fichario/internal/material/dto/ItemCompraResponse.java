package br.com.fichario.internal.material.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Material que precisa ser comprado, com os pedidos que dependem dele.
 * {@code precisaAte} e a entrega mais proxima entre esses pedidos.
 */
public record ItemCompraResponse(Long materialId, String material, LocalDate precisaAte, List<Long> pedidos) {
}
