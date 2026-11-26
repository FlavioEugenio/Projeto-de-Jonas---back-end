package br.com.fichario.internal.pagamento.service;

import br.com.fichario.internal.pagamento.dto.EntregaRequest;
import br.com.fichario.internal.pagamento.dto.PagamentoRequest;
import br.com.fichario.internal.pedido.dto.PedidoResponse;
import br.com.fichario.internal.pedido.entity.Pedido;
import br.com.fichario.internal.pedido.mapper.PedidoMapper;
import br.com.fichario.internal.pedido.service.PedidoService;
import br.com.fichario.internal.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PedidoService pedidoService;

    @Transactional
    public PedidoResponse registrarPagamento(Long pedidoId, PagamentoRequest request, Long usuarioId) {
        Usuario usuario = pedidoService.buscarUsuario(usuarioId);
        Pedido pedido = pedidoService.buscarPedido(pedidoId);
        pedido.registrarPagamento(request.valor(), request.forma(), usuario);
        return PedidoMapper.paraResposta(pedido);
    }

    @Transactional
    public PedidoResponse registrarEntrega(Long pedidoId, EntregaRequest request, Long usuarioId) {
        Usuario usuario = pedidoService.buscarUsuario(usuarioId);
        Pedido pedido = pedidoService.buscarPedido(pedidoId);
        pedido.registrarEntrega(request.valorRecebido(), request.forma(), request.recebidoPor(), usuario);
        return PedidoMapper.paraResposta(pedido);
    }
}
