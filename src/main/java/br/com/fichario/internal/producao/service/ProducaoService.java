package br.com.fichario.internal.producao.service;

import br.com.fichario.internal.pedido.dto.PedidoDetalheResponse;
import br.com.fichario.internal.pedido.entity.Pedido;
import br.com.fichario.internal.pedido.entity.StatusPedido;
import br.com.fichario.internal.pedido.mapper.PedidoMapper;
import br.com.fichario.internal.pedido.repository.PedidoRepository;
import br.com.fichario.internal.producao.dto.FilaProducaoItem;
import br.com.fichario.internal.producao.mapper.ProducaoMapper;
import br.com.fichario.internal.usuario.entity.Usuario;
import br.com.fichario.internal.usuario.repository.UsuarioRepository;
import br.com.fichario.shared.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Andamento do pedido na producao. O status deixa de depender de perguntar aos funcionarios:
 * cada mudanca e registrada por quem fez. As regras de transicao ficam em {@link Pedido}.
 */
@Service
public class ProducaoService {

    /** Status que ainda estao na mao da producao. */
    private static final List<StatusPedido> NA_PRODUCAO =
            List.of(StatusPedido.RECEBIDO, StatusPedido.AGUARDANDO_MATERIAL, StatusPedido.EM_PRODUCAO);

    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;

    public ProducaoService(PedidoRepository pedidos, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.usuarios = usuarios;
    }

    /** So inicia sem alteracao pendente e com todos os materiais disponiveis. */
    @Transactional
    public PedidoDetalheResponse iniciarProducao(Long pedidoId, Long usuarioId) {
        return executar(pedidoId, usuarioId, Pedido::iniciarProducao);
    }

    @Transactional
    public PedidoDetalheResponse aguardarMaterial(Long pedidoId, Long usuarioId) {
        return executar(pedidoId, usuarioId, Pedido::aguardarMaterial);
    }

    @Transactional
    public PedidoDetalheResponse marcarPronto(Long pedidoId, Long usuarioId) {
        return executar(pedidoId, usuarioId, Pedido::marcarPronto);
    }

    @Transactional
    public PedidoDetalheResponse voltarParaProducao(Long pedidoId, Long usuarioId) {
        return executar(pedidoId, usuarioId, Pedido::voltarParaProducao);
    }

    /** Somente o dono cancela, sempre com motivo. */
    @Transactional
    public PedidoDetalheResponse cancelar(Long pedidoId, String motivo, Long usuarioId) {
        return executar(pedidoId, usuarioId, (pedido, autor) -> pedido.cancelar(motivo, autor));
    }

    /** Fila da producao: pedidos ainda nao prontos, do mais urgente para o menos urgente. */
    @Transactional(readOnly = true)
    public List<FilaProducaoItem> listarFila() {
        return pedidos.findAll((raiz, consulta, cb) -> raiz.get("status").in(NA_PRODUCAO),
                        Sort.by("dataEntrega", "id")).stream()
                .map(ProducaoMapper::paraItemFila)
                .toList();
    }

    private PedidoDetalheResponse executar(Long pedidoId, Long usuarioId, BiConsumer<Pedido, Usuario> acao) {
        Usuario autor = usuarios.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
        Pedido pedido = pedidos.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", pedidoId));
        acao.accept(pedido, autor);
        return PedidoMapper.paraDetalhe(pedidos.saveAndFlush(pedido));
    }
}
