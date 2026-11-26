package br.com.fichario.internal.producao.service;

import br.com.fichario.internal.pedido.dto.PedidoResponse;
import br.com.fichario.internal.pedido.entity.Pedido;
import br.com.fichario.internal.pedido.entity.StatusPedido;
import br.com.fichario.internal.pedido.mapper.PedidoMapper;
import br.com.fichario.internal.pedido.repository.PedidoRepository;
import br.com.fichario.internal.pedido.service.PedidoService;
import br.com.fichario.internal.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProducaoService {

    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarFila() {
        List<StatusPedido> naProducao = List.of(StatusPedido.RECEBIDO, StatusPedido.EM_PRODUCAO);
        return pedidoRepository.findByStatusInOrderByDataEntregaAsc(naProducao).stream()
                .map(PedidoMapper::paraResposta)
                .toList();
    }

    @Transactional
    public PedidoResponse iniciarProducao(Long pedidoId, Long usuarioId) {
        Usuario usuario = pedidoService.buscarUsuario(usuarioId);
        Pedido pedido = pedidoService.buscarPedido(pedidoId);
        pedido.iniciarProducao(usuario);
        return PedidoMapper.paraResposta(pedido);
    }

    @Transactional
    public PedidoResponse marcarPronto(Long pedidoId, Long usuarioId) {
        Usuario usuario = pedidoService.buscarUsuario(usuarioId);
        Pedido pedido = pedidoService.buscarPedido(pedidoId);
        pedido.marcarPronto(usuario);
        return PedidoMapper.paraResposta(pedido);
    }
}
