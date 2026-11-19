package br.com.fichario.controller;

import br.com.fichario.dto.AlteracaoResponse;
import br.com.fichario.dto.AlterarDataEntregaRequest;
import br.com.fichario.dto.AlterarQuantidadeRequest;
import br.com.fichario.dto.AlterarTextoRequest;
import br.com.fichario.dto.PedidoDetalheResponse;
import br.com.fichario.dto.PedidoResumoResponse;
import br.com.fichario.service.AlteracaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Alteracoes do pedido e ciencia da producao. Quem faz a operacao vai no cabecalho X-Usuario-Id. */
@RestController
public class AlteracaoController {

    private final AlteracaoService service;

    public AlteracaoController(AlteracaoService service) {
        this.service = service;
    }

    @PutMapping("/pedidos/{id}/personalizacao")
    public PedidoDetalheResponse alterarPersonalizacao(@PathVariable Long id,
                                                       @RequestHeader("X-Usuario-Id") Long usuarioId,
                                                       @RequestBody @Valid AlterarTextoRequest dados) {
        return service.alterarPersonalizacao(id, dados.valor(), usuarioId);
    }

    @PutMapping("/pedidos/{id}/quantidade")
    public PedidoDetalheResponse alterarQuantidade(@PathVariable Long id,
                                                   @RequestHeader("X-Usuario-Id") Long usuarioId,
                                                   @RequestBody @Valid AlterarQuantidadeRequest dados) {
        return service.alterarQuantidade(id, dados.quantidade(), usuarioId);
    }

    @PutMapping("/pedidos/{id}/data-entrega")
    public PedidoDetalheResponse alterarDataEntrega(@PathVariable Long id,
                                                    @RequestHeader("X-Usuario-Id") Long usuarioId,
                                                    @RequestBody @Valid AlterarDataEntregaRequest dados) {
        return service.alterarDataEntrega(id, dados.dataEntrega(), usuarioId);
    }

    @PutMapping("/pedidos/{id}/observacoes")
    public PedidoDetalheResponse alterarObservacoes(@PathVariable Long id,
                                                    @RequestHeader("X-Usuario-Id") Long usuarioId,
                                                    @RequestBody @Valid AlterarTextoRequest dados) {
        return service.alterarObservacoes(id, dados.valor(), usuarioId);
    }

    @PostMapping("/pedidos/{id}/confirmacao-cliente")
    public PedidoDetalheResponse confirmarPeloCliente(@PathVariable Long id,
                                                      @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return service.confirmarPeloCliente(id, usuarioId);
    }

    @PostMapping("/pedidos/{id}/ciencia")
    public PedidoDetalheResponse confirmarCiencia(@PathVariable Long id,
                                                  @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return service.confirmarCiencia(id, usuarioId);
    }

    @GetMapping("/pedidos/{id}/alteracoes")
    public List<AlteracaoResponse> listarDoPedido(@PathVariable Long id) {
        return service.listarDoPedido(id);
    }

    @GetMapping("/alteracoes/pendentes")
    public List<PedidoResumoResponse> listarPedidosComPendencia() {
        return service.listarPedidosComPendencia();
    }
}
