package br.com.fichario.internal.pedido.entity;

import br.com.fichario.internal.cliente.entity.Cliente;
import br.com.fichario.internal.material.entity.Material;
import br.com.fichario.internal.produto.entity.Produto;
import br.com.fichario.internal.usuario.entity.Perfil;
import br.com.fichario.internal.usuario.entity.Usuario;
import br.com.fichario.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Regras de negocio do Pedido, testadas sem banco e sem servidor. */
class PedidoTest {

    private final Usuario ana = new Usuario("Ana", Perfil.ATENDIMENTO);
    private final Usuario beto = new Usuario("Beto", Perfil.PRODUCAO);
    private final Material fita = new Material("Fita de cetim");
    private Pedido pedido;

    @BeforeEach
    void criarPedido() {
        Produto kit = new Produto("Kit festa");
        kit.adicionarMaterialPadrao(fita);
        pedido = new Pedido(new Cliente("Maria", "(85) 99999-1142"), kit, 50, "Tema jardim",
                LocalDate.now().plusDays(3), new BigDecimal("450.00"), CanalOrigem.WHATSAPP, ana);
    }

    @Test
    void pedidoNovoComecaRecebidoComOsMateriaisDoProduto() {
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.RECEBIDO);
        assertThat(pedido.getMateriaisEmFalta()).hasSize(1);
        assertThat(pedido.getSaldo()).isEqualByComparingTo("450.00");
    }

    @Test
    void producaoNaoPodeCriarPedido() {
        assertThatThrownBy(() -> new Pedido(pedido.getCliente(), pedido.getProduto(), 1, "x",
                LocalDate.now(), BigDecimal.TEN, CanalOrigem.PRESENCIAL, beto))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void alteracaoPendenteImpedeIniciarProducao() {
        pedido.marcarMaterialDisponivel(fita, beto);
        pedido.alterarPersonalizacao("Tema praia", ana);

        assertThatThrownBy(() -> pedido.iniciarProducao(beto)).isInstanceOf(RegraDeNegocioException.class);

        pedido.confirmarCiencia(beto);
        pedido.iniciarProducao(beto);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.EM_PRODUCAO);
    }

    @Test
    void naoIniciaProducaoComMaterialFaltando() {
        assertThatThrownBy(() -> pedido.iniciarProducao(beto)).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void pagamentoNaoPassaDoSaldo() {
        pedido.registrarPagamento(new BigDecimal("200.00"), FormaPagamento.PIX, ana);

        assertThat(pedido.getSaldo()).isEqualByComparingTo("250.00");
        assertThatThrownBy(() -> pedido.registrarPagamento(new BigDecimal("300.00"), FormaPagamento.PIX, ana))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void entregaSoFechaComSaldoQuitado() {
        pedido.marcarMaterialDisponivel(fita, beto);
        pedido.iniciarProducao(beto);
        pedido.marcarPronto(beto);

        assertThatThrownBy(() -> pedido.registrarEntrega(new BigDecimal("100.00"), FormaPagamento.DINHEIRO, "Ana", ana))
                .isInstanceOf(RegraDeNegocioException.class);

        pedido.registrarEntrega(new BigDecimal("450.00"), FormaPagamento.DINHEIRO, "Ana", ana);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(pedido.getSaldo()).isEqualByComparingTo("0.00");
    }
}
