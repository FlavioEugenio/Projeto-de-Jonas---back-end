package br.com.fichario.internal.pedido.entity;

import br.com.fichario.internal.agenda.entity.ConfiguracaoAgenda;
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

class PedidoTest {

    private final Usuario ana = new Usuario("Ana", "ana", "hash", Perfil.ATENDIMENTO);
    private final Usuario beto = new Usuario("Beto", "beto", "hash", Perfil.PRODUCAO);
    private final Usuario marcos = new Usuario("Marcos", "marcos", "hash", Perfil.DONO);
    private final Material fita = new Material("Fita de cetim rosa");
    private final Material caixa = new Material("Caixa kraft");
    private Pedido pedido;

    @BeforeEach
    void criarPedido() {
        Produto kit = new Produto("Kit festa");
        kit.adicionarMaterialPadrao(fita);
        kit.adicionarMaterialPadrao(caixa);
        Cliente maria = new Cliente("Maria Souza", "(11) 90000-1142");
        pedido = new Pedido(maria, kit, 50, "Tema jardim, fita azul", LocalDate.now().plusDays(3),
                new BigDecimal("450.00"), CanalOrigem.WHATSAPP, ana);
    }

    private void deixarProntoComMateriais() {
        pedido.marcarMaterialDisponivel(fita, beto);
        pedido.marcarMaterialDisponivel(caixa, beto);
        pedido.iniciarProducao(beto);
        pedido.marcarPronto(beto);
    }

    @Test
    void novoPedidoCopiaMateriaisDoProdutoERegistraHistorico() {
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.RECEBIDO);
        assertThat(pedido.getMateriais()).hasSize(2).noneMatch(MaterialPedido::isDisponivel);
        assertThat(pedido.getHistorico()).hasSize(1);
        assertThat(pedido.getCliente().getTelefone()).isEqualTo("11900001142");
    }

    @Test
    void producaoNaoCriaPedido() {
        assertThatThrownBy(() -> new Pedido(pedido.getCliente(), pedido.getProduto(), 1, "x",
                LocalDate.now(), BigDecimal.TEN, CanalOrigem.PRESENCIAL, beto))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void alteracaoFicaPendenteAteACienciaDaProducao() {
        pedido.alterarPersonalizacao("Tema jardim, fita rosa", ana);

        assertThat(pedido.possuiAlteracoesPendentes()).isTrue();
        assertThat(pedido.isConfirmadoPeloCliente()).isFalse();
        pedido.marcarMaterialDisponivel(fita, beto);
        pedido.marcarMaterialDisponivel(caixa, beto);
        assertThatThrownBy(() -> pedido.iniciarProducao(beto)).hasMessageContaining("confirmar a alteracao");
        assertThatThrownBy(() -> pedido.confirmarCiencia(ana)).isInstanceOf(RegraDeNegocioException.class);

        pedido.confirmarCiencia(beto);

        assertThat(pedido.possuiAlteracoesPendentes()).isFalse();
        pedido.iniciarProducao(beto);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.EM_PRODUCAO);
    }

    @Test
    void mudancaDeValorNaoExigeCiencia() {
        pedido.alterarValorTotal(new BigDecimal("500"), ana);

        assertThat(pedido.possuiAlteracoesPendentes()).isFalse();
        assertThat(pedido.getAlteracoes()).hasSize(1);
        assertThat(pedido.getSaldo()).isEqualByComparingTo("500.00");
    }

    @Test
    void naoIniciaProducaoComMaterialEmFalta() {
        pedido.marcarMaterialDisponivel(caixa, beto);

        assertThatThrownBy(() -> pedido.iniciarProducao(beto)).hasMessageContaining("Fita de cetim rosa");
        pedido.aguardarMaterial(beto);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.AGUARDANDO_MATERIAL);
    }

    @Test
    void saldoECalculadoEPagamentoNaoPassaDoTotal() {
        pedido.registrarPagamento(new BigDecimal("225"), FormaPagamento.PIX, ana);

        assertThat(pedido.getSaldo()).isEqualByComparingTo("225.00");
        assertThatThrownBy(() -> pedido.registrarPagamento(new BigDecimal("300"), FormaPagamento.PIX, ana))
                .hasMessageContaining("saldo");
        assertThatThrownBy(() -> pedido.alterarValorTotal(new BigDecimal("200"), ana))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void entregaSoFechaComSaldoCompletoEQuemRecebeu() {
        pedido.registrarPagamento(new BigDecimal("225"), FormaPagamento.PIX, ana);
        deixarProntoComMateriais();

        assertThatThrownBy(() -> pedido.registrarEntrega(new BigDecimal("100"), FormaPagamento.DINHEIRO, "Ana", ana))
                .hasMessageContaining("R$ 225,00");
        assertThatThrownBy(() -> pedido.registrarEntrega(new BigDecimal("225"), FormaPagamento.DINHEIRO, " ", ana))
                .isInstanceOf(RegraDeNegocioException.class);

        pedido.registrarEntrega(new BigDecimal("225"), FormaPagamento.DINHEIRO, "Ana", ana);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(pedido.getSaldo()).isEqualByComparingTo("0");
        assertThat(pedido.getPagamentos().get(1).getRecebidoPor()).isEqualTo("Ana");
        assertThatThrownBy(() -> pedido.alterarQuantidade(10, ana)).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void somenteODonoCorrigePagamentoECancela() {
        Pagamento sinal = pedido.registrarPagamento(new BigDecimal("225"), FormaPagamento.PIX, ana);

        assertThatThrownBy(() -> pedido.corrigirPagamento(sinal, new BigDecimal("200"), "erro", ana))
                .isInstanceOf(RegraDeNegocioException.class);
        pedido.corrigirPagamento(sinal, new BigDecimal("200"), "Valor digitado errado", marcos);
        assertThat(sinal.getValorOriginal()).isEqualByComparingTo("225.00");
        assertThat(pedido.getSaldo()).isEqualByComparingTo("250.00");

        assertThatThrownBy(() -> pedido.cancelar("desistiu", ana)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> pedido.cancelar(" ", marcos)).isInstanceOf(RegraDeNegocioException.class);
        pedido.cancelar("Cliente desistiu", marcos);
        assertThat(pedido.ocupaAgenda()).isFalse();
    }

    @Test
    void materialAdicionadoPeloAtendimentoExigeCienciaMasPelaProducaoNao() {
        Material laco = new Material("Laco dourado");
        Material etiqueta = new Material("Etiqueta");

        pedido.adicionarMaterial(laco, beto);
        assertThat(pedido.possuiAlteracoesPendentes()).isFalse();

        pedido.adicionarMaterial(etiqueta, ana);
        assertThat(pedido.possuiAlteracoesPendentes()).isTrue();
        assertThatThrownBy(() -> pedido.adicionarMaterial(etiqueta, ana)).hasMessageContaining("ja esta");
    }

    @Test
    void agendaAvisaQuandoODiaLota() {
        ConfiguracaoAgenda agenda = new ConfiguracaoAgenda(5);

        assertThat(agenda.comportaMaisUmPedido(4)).isTrue();
        assertThat(agenda.comportaMaisUmPedido(5)).isFalse();
        assertThatThrownBy(() -> agenda.alterarLimite(8, ana)).isInstanceOf(RegraDeNegocioException.class);
        agenda.alterarLimite(8, marcos);
        assertThat(agenda.getLimitePedidosPorDia()).isEqualTo(8);
    }
}
