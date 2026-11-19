package br.com.fichario.internal.material.service;

import br.com.fichario.internal.material.dto.ItemCompraResponse;
import br.com.fichario.internal.material.dto.MaterialResponse;
import br.com.fichario.internal.material.entity.Material;
import br.com.fichario.internal.material.mapper.MaterialMapper;
import br.com.fichario.internal.material.repository.MaterialRepository;
import br.com.fichario.internal.pedido.dto.PedidoResponse;
import br.com.fichario.internal.pedido.entity.Pedido;
import br.com.fichario.internal.pedido.entity.StatusPedido;
import br.com.fichario.internal.pedido.mapper.PedidoMapper;
import br.com.fichario.internal.pedido.repository.PedidoRepository;
import br.com.fichario.internal.pedido.service.PedidoService;
import br.com.fichario.internal.usuario.entity.Usuario;
import br.com.fichario.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cada pedido nasce com os materiais do produto, todos "falta conferir".
 * A producao marca o que ja tem; o que continua faltando vira a lista de compras.
 */
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;

    @Transactional(readOnly = true)
    public List<MaterialResponse> listar() {
        return materialRepository.findAllByOrderByNomeAsc().stream()
                .map(MaterialMapper::paraResposta)
                .toList();
    }

    @Transactional
    public PedidoResponse marcarDisponivel(Long pedidoId, Long materialId, Long usuarioId) {
        Usuario usuario = pedidoService.buscarUsuario(usuarioId);
        Pedido pedido = pedidoService.buscarPedido(pedidoId);
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material", materialId));
        pedido.marcarMaterialDisponivel(material, usuario);
        return PedidoMapper.paraResposta(pedido);
    }

    /** Materiais em falta nos pedidos que ainda nao entraram em producao. */
    @Transactional(readOnly = true)
    public List<ItemCompraResponse> listarCompras() {
        List<Pedido> pedidos = pedidoRepository.findByStatusInOrderByDataEntregaAsc(
                List.of(StatusPedido.RECEBIDO, StatusPedido.AGUARDANDO_MATERIAL));
        return pedidos.stream()
                .flatMap(pedido -> pedido.getMateriaisEmFalta().stream()
                        .map(item -> new ItemCompraResponse(
                                item.getMaterial().getNome(), pedido.getId(), pedido.getDataEntrega())))
                .toList();
    }
}
