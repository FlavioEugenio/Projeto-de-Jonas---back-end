package br.com.fichario.internal.pedido.service;

import br.com.fichario.internal.cliente.entity.Cliente;
import br.com.fichario.internal.cliente.service.ClienteService;
import br.com.fichario.internal.pedido.dto.NovoPedidoRequest;
import br.com.fichario.internal.pedido.dto.PedidoResponse;
import br.com.fichario.internal.pedido.entity.Pedido;
import br.com.fichario.internal.pedido.mapper.PedidoMapper;
import br.com.fichario.internal.pedido.repository.PedidoRepository;
import br.com.fichario.internal.produto.entity.Produto;
import br.com.fichario.internal.produto.repository.ProdutoRepository;
import br.com.fichario.internal.usuario.entity.Usuario;
import br.com.fichario.internal.usuario.repository.UsuarioRepository;
import br.com.fichario.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteService clienteService;

    @Transactional
    public PedidoResponse cadastrar(NovoPedidoRequest request, Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Cliente cliente = clienteService.buscarEntidade(request.clienteId());
        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", request.produtoId()));

        Pedido pedido = new Pedido(cliente, produto, request.quantidade(), request.personalizacao(),
                request.dataEntrega(), request.valorTotal(), request.canal(), usuario);
        pedidoRepository.save(pedido);
        return PedidoMapper.paraResposta(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAllByOrderByDataEntregaAsc().stream()
                .map(PedidoMapper::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        return PedidoMapper.paraResposta(buscarPedido(id));
    }

    public Pedido buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    public Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", id));
    }
}
