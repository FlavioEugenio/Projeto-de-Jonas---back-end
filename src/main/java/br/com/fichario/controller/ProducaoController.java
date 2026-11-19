package br.com.fichario.controller;

import br.com.fichario.dto.CancelamentoRequest;
import br.com.fichario.dto.FilaProducaoItem;
import br.com.fichario.dto.PedidoDetalheResponse;
import br.com.fichario.service.ProducaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Status do pedido na producao. Quem faz a operacao vai no cabecalho X-Usuario-Id. */
@RestController
public class ProducaoController {

    private final ProducaoService service;

    public ProducaoController(ProducaoService service) {
        this.service = service;
    }

    @GetMapping("/producao/fila")
    public List<FilaProducaoItem> listarFila() {
        return service.listarFila();
    }

    @PostMapping("/pedidos/{id}/iniciar-producao")
    public PedidoDetalheResponse iniciarProducao(@PathVariable Long id,
                                                 @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return service.iniciarProducao(id, usuarioId);
    }

    @PostMapping("/pedidos/{id}/aguardar-material")
    public PedidoDetalheResponse aguardarMaterial(@PathVariable Long id,
                                                  @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return service.aguardarMaterial(id, usuarioId);
    }

    @PostMapping("/pedidos/{id}/pronto")
    public PedidoDetalheResponse marcarPronto(@PathVariable Long id,
                                              @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return service.marcarPronto(id, usuarioId);
    }

    @PostMapping("/pedidos/{id}/voltar-producao")
    public PedidoDetalheResponse voltarParaProducao(@PathVariable Long id,
                                                    @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return service.voltarParaProducao(id, usuarioId);
    }

    @PostMapping("/pedidos/{id}/cancelamento")
    public PedidoDetalheResponse cancelar(@PathVariable Long id,
                                          @RequestHeader("X-Usuario-Id") Long usuarioId,
                                          @RequestBody @Valid CancelamentoRequest dados) {
        return service.cancelar(id, dados.motivo(), usuarioId);
    }
}
