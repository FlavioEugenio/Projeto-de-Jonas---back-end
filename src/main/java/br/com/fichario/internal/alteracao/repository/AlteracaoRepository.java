package br.com.fichario.internal.alteracao.repository;

import br.com.fichario.internal.pedido.entity.Alteracao;
import br.com.fichario.internal.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlteracaoRepository extends JpaRepository<Alteracao, Long> {

    /** Pedidos com alguma alteracao que a producao ainda nao confirmou. */
    @Query("select distinct a.pedido from Alteracao a where a.exigeCiencia = true and a.cienciaEm is null")
    List<Pedido> pedidosComAlteracaoPendente();
}
