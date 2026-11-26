package br.com.fichario.internal.pedido.entity;

import br.com.fichario.internal.cliente.entity.Cliente;
import br.com.fichario.internal.material.entity.Material;
import br.com.fichario.internal.produto.entity.Produto;
import br.com.fichario.internal.usuario.entity.Perfil;
import br.com.fichario.internal.usuario.entity.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MapeamentoJpaTest {

    @Autowired
    private EntityManager em;

    @Test
    void salvaERecarregaPedidoComTodosOsRelacionamentos() {
        Usuario ana = new Usuario("Ana", Perfil.ATENDIMENTO);
        Usuario beto = new Usuario("Beto", Perfil.PRODUCAO);
        Material fita = new Material("Fita de cetim");
        Produto kit = new Produto("Kit festa");
        kit.adicionarMaterialPadrao(fita);
        Cliente maria = new Cliente("Maria Souza", "11900001142");
        em.persist(ana);
        em.persist(beto);
        em.persist(fita);
        em.persist(kit);
        em.persist(maria);

        Pedido pedido = new Pedido(maria, kit, 50, "Tema jardim", LocalDate.now().plusDays(3),
                new BigDecimal("450.00"), CanalOrigem.WHATSAPP, ana);
        pedido.registrarPagamento(new BigDecimal("225.00"), FormaPagamento.PIX, ana);
        pedido.alterarPersonalizacao("Tema praia", ana);
        pedido.confirmarCiencia(beto);
        pedido.marcarMaterialDisponivel(fita, beto);
        em.persist(pedido);
        em.flush();
        em.clear();

        Pedido lido = em.find(Pedido.class, pedido.getId());
        assertThat(lido.getCliente().getNome()).isEqualTo("Maria Souza");
        assertThat(lido.getMateriais()).singleElement().matches(MaterialPedido::isDisponivel);
        assertThat(lido.getSaldo()).isEqualByComparingTo("225.00");
        assertThat(lido.getAlteracoes()).singleElement().matches(a -> !a.isPendente());

        assertThat(lido.getHistorico()).hasSize(5);
    }
}
