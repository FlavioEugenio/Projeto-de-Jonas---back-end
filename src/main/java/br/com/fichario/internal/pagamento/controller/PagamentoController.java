package br.com.fichario.internal.pagamento.controller;

import br.com.fichario.internal.pagamento.dto.EntregaRequest;
import br.com.fichario.internal.pagamento.dto.PagamentoRequest;
import br.com.fichario.internal.pagamento.service.PagamentoService;
import br.com.fichario.internal.pedido.dto.PedidoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos/{id}")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @PostMapping("/pagamentos")
    public PedidoResponse registrarPagamento(@PathVariable Long id,
                                             @RequestHeader("X-Usuario-Id") Long usuarioId,
                                             @RequestBody @Valid PagamentoRequest request) {
        return pagamentoService.registrarPagamento(id, request, usuarioId);
    }

    @PostMapping("/entrega")
    public PedidoResponse registrarEntrega(@PathVariable Long id,
                                           @RequestHeader("X-Usuario-Id") Long usuarioId,
                                           @RequestBody EntregaRequest request) {
        return pagamentoService.registrarEntrega(id, request, usuarioId);
    }
}
