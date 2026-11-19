package br.com.fichario.internal.producao.controller;

import br.com.fichario.internal.pedido.dto.PedidoResponse;
import br.com.fichario.internal.producao.service.ProducaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProducaoController {

    private final ProducaoService producaoService;

    @GetMapping("/producao/fila")
    public List<PedidoResponse> listarFila() {
        return producaoService.listarFila();
    }

    @PostMapping("/pedidos/{id}/iniciar-producao")
    public PedidoResponse iniciarProducao(@PathVariable Long id,
                                          @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return producaoService.iniciarProducao(id, usuarioId);
    }

    @PostMapping("/pedidos/{id}/pronto")
    public PedidoResponse marcarPronto(@PathVariable Long id,
                                       @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return producaoService.marcarPronto(id, usuarioId);
    }
}
