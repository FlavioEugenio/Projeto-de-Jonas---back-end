package br.com.fichario.internal.alteracao.service;

import br.com.fichario.internal.alteracao.dto.AlteracaoResponse;
import br.com.fichario.internal.alteracao.mapper.AlteracaoMapper;
import br.com.fichario.internal.alteracao.repository.AlteracaoRepository;
import br.com.fichario.internal.pedido.dto.PedidoDetalheResponse;
import br.com.fichario.internal.pedido.dto.PedidoResumoResponse;
import br.com.fichario.internal.pedido.entity.Pedido;
import br.com.fichario.internal.pedido.mapper.PedidoMapper;
import br.com.fichario.internal.pedido.repository.PedidoRepository;
import br.com.fichario.internal.usuario.entity.Usuario;
import br.com.fichario.internal.usuario.repository.UsuarioRepository;
import br.com.fichario.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Alteracoes pedidas pelo cliente depois do pedido registrado.
 * Cada mudanca fica pendente ate a producao confirmar ciencia: assim a producao
 * nunca trabalha com a informacao antiga. As regras ficam em {@link Pedido}.
 */
@Service
public class AlteracaoService {

    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final AlteracaoRepository alteracoes;

    public AlteracaoService(PedidoRepository pedidos, UsuarioRepository usuarios, AlteracaoRepository alteracoes) {
        this.pedidos = pedidos;
        this.usuarios = usuarios;
        this.alteracoes = alteracoes;
    }

    @Transactional
    public PedidoDetalheResponse alterarPersonalizacao(Long pedidoId, String valor, Long usuarioId) {
        return executar(pedidoId, usuarioId, (pedido, autor) -> pedido.alterarPersonalizacao(valor, autor));
    }

    @Transactional
    public PedidoDetalheResponse alterarQuantidade(Long pedidoId, int quantidade, Long usuarioId) {
        return executar(pedidoId, usuarioId, (pedido, autor) -> pedido.alterarQuantidade(quantidade, autor));
    }

    @Transactional
    public PedidoDetalheResponse alterarDataEntrega(Long pedidoId, LocalDate data, Long usuarioId) {
        return executar(pedidoId, usuarioId, (pedido, autor) -> pedido.alterarDataEntrega(data, autor));
    }

    @Transactional
    public PedidoDetalheResponse alterarObservacoes(Long pedidoId, String valor, Long usuarioId) {
        return executar(pedidoId, usuarioId, (pedido, autor) -> pedido.alterarObservacoes(valor, autor));
    }

    /** O cliente confirmou no WhatsApp o resumo da versao atual do pedido. */
    @Transactional
    public PedidoDetalheResponse confirmarPeloCliente(Long pedidoId, Long usuarioId) {
        return executar(pedidoId, usuarioId, Pedido::confirmarPeloCliente);
    }

    /** A producao confirma que viu todas as alteracoes pendentes do pedido. */
    @Transactional
    public PedidoDetalheResponse confirmarCiencia(Long pedidoId, Long usuarioId) {
        return executar(pedidoId, usuarioId, Pedido::confirmarCiencia);
    }

    @Transactional(readOnly = true)
    public List<AlteracaoResponse> listarDoPedido(Long pedidoId) {
        return buscarPedido(pedidoId).getAlteracoes().stream().map(AlteracaoMapper::paraResposta).toList();
    }

    /** Lista que a producao confere no inicio do dia: pedidos com mudancas que ela ainda nao viu. */
    @Transactional(readOnly = true)
    public List<PedidoResumoResponse> listarPedidosComPendencia() {
        return alteracoes.pedidosComAlteracaoPendente().stream()
                .sorted(Comparator.comparing(Pedido::getDataEntrega).thenComparing(Pedido::getId))
                .map(PedidoMapper::paraResumo)
                .toList();
    }

    private PedidoDetalheResponse executar(Long pedidoId, Long usuarioId, BiConsumer<Pedido, Usuario> acao) {
        Usuario autor = usuarios.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
        Pedido pedido = buscarPedido(pedidoId);
        acao.accept(pedido, autor);
        return PedidoMapper.paraDetalhe(pedidos.saveAndFlush(pedido));
    }

    private Pedido buscarPedido(Long id) {
        return pedidos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }
}
