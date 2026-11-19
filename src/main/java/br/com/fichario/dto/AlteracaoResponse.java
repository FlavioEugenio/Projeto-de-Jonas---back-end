package br.com.fichario.dto;

import br.com.fichario.domain.model.Alteracao;
import br.com.fichario.domain.model.Usuario;

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

    public static AlteracaoResponse de(Alteracao a) {
        return new AlteracaoResponse(a.getId(), a.getCampo(), a.getValorAnterior(), a.getValorNovo(),
                a.getAlteradoPor().getNome(), a.getAlteradoEm(), a.isPendente(),
                nome(a.getCienciaPor()), a.getCienciaEm());
    }

    private static String nome(Usuario usuario) {
        return usuario == null ? null : usuario.getNome();
    }
}
