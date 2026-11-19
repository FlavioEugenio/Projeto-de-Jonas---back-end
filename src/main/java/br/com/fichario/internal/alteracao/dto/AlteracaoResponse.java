package br.com.fichario.internal.alteracao.dto;

import java.time.LocalDateTime;

/** Uma mudanca feita no pedido e se a producao ja confirmou que viu. */
public record AlteracaoResponse(
        Long id,
        String campo,
        String valorAnterior,
        String valorNovo,
        String alteradoPor,
        LocalDateTime alteradoEm,
        boolean pendente,
        String cienciaPor,
        LocalDateTime cienciaEm) {
}
