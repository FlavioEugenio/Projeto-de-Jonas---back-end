package br.com.fichario.internal.alteracao.service;

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
public class AlteracaoService {

    private final PedidoService pedidoService;

    @Transactional
    public PedidoResponse alterarPersonalizacao(Long pedidoId, String personalizacao, Long usuarioId) {
        Usuario usuario = pedidoService.buscarUsuario(usuarioId);
        Pedido pedido = pedidoService.buscarPedido(pedidoId);
        pedido.alterarPersonalizacao(personalizacao, usuario);
        return PedidoMapper.paraResposta(pedido);
    }

    @Transactional
    public PedidoResponse confirmarCiencia(Long pedidoId, Long usuarioId) {
        Usuario usuario = pedidoService.buscarUsuario(usuarioId);
        Pedido pedido = pedidoService.buscarPedido(pedidoId);
        pedido.confirmarCiencia(usuario);
        return PedidoMapper.paraResposta(pedido);
    }
}
