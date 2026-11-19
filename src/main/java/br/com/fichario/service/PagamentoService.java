package br.com.fichario.service;

import br.com.fichario.domain.model.Pagamento;
import br.com.fichario.domain.model.Pedido;
import br.com.fichario.domain.model.StatusPedido;
import br.com.fichario.domain.model.Usuario;
import br.com.fichario.dto.CorrecaoPagamentoRequest;
import br.com.fichario.dto.EntregaRequest;
import br.com.fichario.dto.ExtratoPagamentosResponse;
import br.com.fichario.dto.PagamentoRequest;
import br.com.fichario.repository.PedidoRepository;
import br.com.fichario.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Pagamentos (sinal, parcial e saldo na entrega) e fechamento do pedido.
 * O dominio garante que a entrega so fecha com o saldo completo recebido.
 */
@Service
public class PagamentoService {

    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;

    public PagamentoService(PedidoRepository pedidos, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.usuarios = usuarios;
    }

    @Transactional
    public ExtratoPagamentosResponse registrar(Long pedidoId, PagamentoRequest dados, Long usuarioId) {
        return executar(pedidoId, usuarioId,
                (pedido, autor) -> pedido.registrarPagamento(dados.valor(), dados.forma(), autor));
    }

    /** Somente o dono corrige. O valor original e o motivo ficam guardados. */
    @Transactional
    public ExtratoPagamentosResponse corrigir(Long pedidoId, Long pagamentoId, CorrecaoPagamentoRequest dados,
                                              Long usuarioId) {
        return executar(pedidoId, usuarioId, (pedido, autor) -> {
            Pagamento pagamento = pedido.getPagamentos().stream()
                    .filter(p -> p.getId().equals(pagamentoId))
                    .findFirst()
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento", pagamentoId));
            pedido.corrigirPagamento(pagamento, dados.valor(), dados.motivo(), autor);
        });
    }

    @Transactional
    public ExtratoPagamentosResponse alterarValorTotal(Long pedidoId, BigDecimal valorTotal, Long usuarioId) {
        return executar(pedidoId, usuarioId, (pedido, autor) -> pedido.alterarValorTotal(valorTotal, autor));
    }

    /** Fecha o pedido. Com saldo em aberto, exige o valor completo e o nome de quem recebeu. */
    @Transactional
    public ExtratoPagamentosResponse registrarEntrega(Long pedidoId, EntregaRequest dados, Long usuarioId) {
        EntregaRequest entrega = dados == null ? new EntregaRequest(null, null, null) : dados;
        return executar(pedidoId, usuarioId, (pedido, autor) -> pedido.registrarEntrega(
                entrega.valorRecebido(), entrega.forma(), entrega.recebidoPor(), autor));
    }

    @Transactional(readOnly = true)
    public ExtratoPagamentosResponse consultar(Long pedidoId) {
        return ExtratoPagamentosResponse.de(buscarPedido(pedidoId));
    }

    /** Pedidos ainda nao entregues com dinheiro a receber, pela data de entrega. */
    @Transactional(readOnly = true)
    public List<ExtratoPagamentosResponse> listarAReceber() {
        return pedidos.findAll((raiz, consulta, cb) -> cb.not(raiz.get("status")
                                .in(List.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO))),
                        Sort.by("dataEntrega", "id")).stream()
                .filter(p -> p.getSaldo().signum() > 0)
                .map(ExtratoPagamentosResponse::de)
                .toList();
    }

    private ExtratoPagamentosResponse executar(Long pedidoId, Long usuarioId, BiConsumer<Pedido, Usuario> acao) {
        Usuario autor = usuarios.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
        Pedido pedido = buscarPedido(pedidoId);
        acao.accept(pedido, autor);
        return ExtratoPagamentosResponse.de(pedidos.saveAndFlush(pedido));
    }

    private Pedido buscarPedido(Long id) {
        return pedidos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }
}
