package br.com.fichario.domain.model;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Garante que as entidades e relacionamentos sao salvos e lidos de volta pelo JPA. */
@DataJpaTest
class MapeamentoJpaTest {

    @Autowired
    private EntityManager em;

    @Test
    void salvaERecarregaPedidoComTodosOsRelacionamentos() {
        Usuario ana = new Usuario("Ana", "ana", "hash", Perfil.ATENDIMENTO);
        Usuario beto = new Usuario("Beto", "beto", "hash", Perfil.PRODUCAO);
        Material fita = new Material("Fita de cetim");
        Produto kit = new Produto("Kit festa");
        kit.adicionarMaterialPadrao(fita);
        Cliente maria = new Cliente("Maria Souza", "11900001142");
        ConfiguracaoAgenda agenda = new ConfiguracaoAgenda(5);
        em.persist(ana);
        em.persist(beto);
        em.persist(fita);
        em.persist(kit);
        em.persist(maria);
        em.persist(agenda);

        Pedido pedido = new Pedido(maria, kit, 50, "Tema jardim", LocalDate.now().plusDays(3),
                new BigDecimal("450.00"), CanalOrigem.WHATSAPP, ana);
        pedido.registrarPagamento(new BigDecimal("225.00"), FormaPagamento.PIX, ana);
        pedido.alterarQuantidade(60, ana);
        pedido.confirmarCiencia(beto);
        pedido.marcarMaterialDisponivel(fita, beto);
        em.persist(pedido);
        em.flush();
        em.clear();

        Pedido lido = em.find(Pedido.class, pedido.getId());
        assertThat(lido.getCliente().getNome()).isEqualTo("Maria Souza");
        assertThat(lido.getProduto().getMateriaisPadrao()).extracting(Material::getNome).containsExactly("Fita de cetim");
        assertThat(lido.getMateriais()).singleElement().matches(MaterialPedido::isDisponivel);
        assertThat(lido.getPagamentos()).hasSize(1);
        assertThat(lido.getSaldo()).isEqualByComparingTo("225.00");
        assertThat(lido.getAlteracoes()).singleElement().matches(a -> !a.isPendente());
        // criado, pagamento, quantidade, ciencia e material disponivel
        assertThat(lido.getHistorico()).hasSize(5);
        assertThat(lido.getVersao()).isNotNull();
        assertThat(em.find(ConfiguracaoAgenda.class, agenda.getId()).getLimitePedidosPorDia()).isEqualTo(5);
    }
}
