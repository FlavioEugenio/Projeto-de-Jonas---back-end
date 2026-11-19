package br.com.fichario.internal.pedido.service;

import br.com.fichario.internal.cliente.entity.Cliente;
import br.com.fichario.internal.cliente.service.ClienteService;
import br.com.fichario.internal.pedido.dto.NovoPedidoRequest;
import br.com.fichario.internal.pedido.dto.PedidoDetalheResponse;
import br.com.fichario.internal.pedido.dto.PedidoResumoResponse;
import br.com.fichario.internal.pedido.entity.Pedido;
import br.com.fichario.internal.pedido.entity.StatusPedido;
import br.com.fichario.internal.pedido.mapper.PedidoMapper;
import br.com.fichario.internal.pedido.repository.PedidoRepository;
import br.com.fichario.internal.produto.entity.Produto;
import br.com.fichario.internal.produto.repository.ProdutoRepository;
import br.com.fichario.internal.usuario.entity.Usuario;
import br.com.fichario.internal.usuario.repository.UsuarioRepository;
import br.com.fichario.shared.exception.RecursoNaoEncontradoException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cadastro e consulta de pedidos. O service busca no banco o que o pedido precisa,
 * chama o dominio e salva. As regras (perfil, quantidade, data, produto ativo) ficam em {@link Pedido}.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final ClienteService clienteService;
    private final ProdutoRepository produtos;
    private final UsuarioRepository usuarios;

    public PedidoService(PedidoRepository pedidos, ClienteService clienteService,
                         ProdutoRepository produtos, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.clienteService = clienteService;
        this.produtos = produtos;
        this.usuarios = usuarios;
    }

    @Transactional
    public PedidoDetalheResponse cadastrar(NovoPedidoRequest dados, Long usuarioId) {
        Usuario autor = usuarios.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
        Cliente cliente = clienteService.buscarEntidade(dados.clienteId());
        Produto produto = produtos.findById(dados.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", dados.produtoId()));

        Pedido pedido = new Pedido(cliente, produto, dados.quantidade(), dados.personalizacao(),
                dados.dataEntrega(), dados.valorTotal(), dados.canal(), autor);
        return PedidoMapper.paraDetalhe(pedidos.save(pedido));
    }

    /** Lista com filtros opcionais, ordenada pela data de entrega. */
    @Transactional(readOnly = true)
    public List<PedidoResumoResponse> listar(StatusPedido status, LocalDate dataEntrega, Long clienteId) {
        Specification<Pedido> filtro = (raiz, consulta, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (status != null) {
                condicoes.add(cb.equal(raiz.get("status"), status));
            }
            if (dataEntrega != null) {
                condicoes.add(cb.equal(raiz.get("dataEntrega"), dataEntrega));
            }
            if (clienteId != null) {
                condicoes.add(cb.equal(raiz.get("cliente").get("id"), clienteId));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };
        return pedidos.findAll(filtro, Sort.by("dataEntrega", "id")).stream()
                .map(PedidoMapper::paraResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoDetalheResponse buscarPorId(Long id) {
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
        return PedidoMapper.paraDetalhe(pedido);
    }
}
