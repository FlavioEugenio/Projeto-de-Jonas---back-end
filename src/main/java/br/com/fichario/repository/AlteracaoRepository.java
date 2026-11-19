package br.com.fichario.repository;

import br.com.fichario.domain.model.Alteracao;
import br.com.fichario.domain.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlteracaoRepository extends JpaRepository<Alteracao, Long> {

    /** Pedidos com alguma alteracao que a producao ainda nao confirmou. */
    @Query("select distinct a.pedido from Alteracao a where a.exigeCiencia = true and a.cienciaEm is null")
    List<Pedido> pedidosComAlteracaoPendente();
}
