package br.com.fichario.internal.material.service;

import br.com.fichario.internal.material.dto.ItemCompraResponse;
import br.com.fichario.internal.material.dto.MaterialResponse;
import br.com.fichario.internal.material.entity.Material;
import br.com.fichario.internal.material.mapper.MaterialMapper;
import br.com.fichario.internal.material.repository.MaterialRepository;
import br.com.fichario.internal.pedido.dto.PedidoDetalheResponse;
import br.com.fichario.internal.pedido.entity.MaterialPedido;
import br.com.fichario.internal.pedido.entity.Pedido;
import br.com.fichario.internal.pedido.entity.StatusPedido;
import br.com.fichario.internal.pedido.mapper.PedidoMapper;
import br.com.fichario.internal.pedido.repository.PedidoRepository;
import br.com.fichario.internal.usuario.entity.Usuario;
import br.com.fichario.internal.usuario.repository.UsuarioRepository;
import br.com.fichario.shared.exception.RecursoNaoEncontradoException;
import br.com.fichario.shared.exception.RegraDeNegocioException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Materiais de cada pedido e lista de compras. A producao marca o que ja esta disponivel;
 * o que falta vira a lista de compras, ligada aos pedidos que precisam dele.
 * Assim se compra so o necessario e a producao nao comeca sem material.
 */
@Service
public class MaterialService {

    private final MaterialRepository materiais;
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;

    public MaterialService(MaterialRepository materiais, PedidoRepository pedidos, UsuarioRepository usuarios) {
        this.materiais = materiais;
        this.pedidos = pedidos;
        this.usuarios = usuarios;
    }

    @Transactional
    public MaterialResponse cadastrar(String nome) {
        if (nome != null && materiais.existsByNomeIgnoreCase(nome.strip())) {
            throw new RegraDeNegocioException("O material " + nome.strip() + " ja esta cadastrado.");
        }
        return MaterialMapper.paraResposta(materiais.save(new Material(nome)));
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> listar() {
        return materiais.findAllByOrderByNomeAsc().stream().map(MaterialMapper::paraResposta).toList();
    }

    @Transactional
    public PedidoDetalheResponse adicionarAoPedido(Long pedidoId, Long materialId, Long usuarioId) {
        Material material = buscarMaterial(materialId);
        return executar(pedidoId, usuarioId, (pedido, autor) -> pedido.adicionarMaterial(material, autor));
    }

    @Transactional
    public PedidoDetalheResponse removerDoPedido(Long pedidoId, Long materialId, Long usuarioId) {
        Material material = buscarMaterial(materialId);
        return executar(pedidoId, usuarioId, (pedido, autor) -> pedido.removerMaterial(material, autor));
    }

    @Transactional
    public PedidoDetalheResponse marcarDisponivel(Long pedidoId, Long materialId, Long usuarioId) {
        Material material = buscarMaterial(materialId);
        return executar(pedidoId, usuarioId, (pedido, autor) -> pedido.marcarMaterialDisponivel(material, autor));
    }

    @Transactional
    public PedidoDetalheResponse marcarEmFalta(Long pedidoId, Long materialId, Long usuarioId) {
        Material material = buscarMaterial(materialId);
        return executar(pedidoId, usuarioId, (pedido, autor) -> pedido.marcarMaterialEmFalta(material, autor));
    }

    /** Materiais em falta nos pedidos abertos, do mais urgente para o menos urgente. */
    @Transactional(readOnly = true)
    public List<ItemCompraResponse> listarCompras() {
        List<Pedido> abertos = pedidos.findAll((raiz, consulta, cb) -> cb.not(raiz.get("status")
                        .in(List.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO))),
                Sort.by("dataEntrega", "id"));

        Map<Long, List<Pedido>> pedidosPorMaterial = new LinkedHashMap<>();
        Map<Long, Material> materialPorId = new HashMap<>();
        for (Pedido pedido : abertos) {
            for (MaterialPedido item : pedido.getMateriaisEmFalta()) {
                Material material = item.getMaterial();
                materialPorId.put(material.getId(), material);
                pedidosPorMaterial.computeIfAbsent(material.getId(), id -> new ArrayList<>()).add(pedido);
            }
        }
        // como os pedidos ja vem por data, o primeiro de cada lista e o mais urgente
        return pedidosPorMaterial.entrySet().stream()
                .map(e -> new ItemCompraResponse(e.getKey(), materialPorId.get(e.getKey()).getNome(),
                        e.getValue().get(0).getDataEntrega(), e.getValue().stream().map(Pedido::getId).toList()))
                .sorted(Comparator.comparing(ItemCompraResponse::precisaAte)
                        .thenComparing(ItemCompraResponse::material))
                .toList();
    }

    private PedidoDetalheResponse executar(Long pedidoId, Long usuarioId,
                                           BiConsumer<Pedido, Usuario> acao) {
        Usuario autor = usuarios.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
        Pedido pedido = pedidos.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", pedidoId));
        acao.accept(pedido, autor);
        return PedidoMapper.paraDetalhe(pedidos.saveAndFlush(pedido));
    }

    private Material buscarMaterial(Long id) {
        return materiais.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Material", id));
    }
}
