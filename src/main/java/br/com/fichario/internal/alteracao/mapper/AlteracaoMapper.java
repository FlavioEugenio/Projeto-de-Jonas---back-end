package br.com.fichario.internal.alteracao.mapper;

import br.com.fichario.internal.alteracao.dto.AlteracaoResponse;
import br.com.fichario.internal.pedido.entity.Alteracao;
import br.com.fichario.internal.usuario.entity.Usuario;

/** Converte a Alteracao para o DTO de saida. */
public final class AlteracaoMapper {

    private AlteracaoMapper() {
    }

    public static AlteracaoResponse paraResposta(Alteracao alteracao) {
        return new AlteracaoResponse(
                alteracao.getId(),
                alteracao.getCampo(),
                alteracao.getValorAnterior(),
                alteracao.getValorNovo(),
                alteracao.getAlteradoPor().getNome(),
                alteracao.getAlteradoEm(),
                alteracao.isPendente(),
                nome(alteracao.getCienciaPor()),
                alteracao.getCienciaEm()
        );
    }

    private static String nome(Usuario usuario) {
        return usuario == null ? null : usuario.getNome();
    }
}
