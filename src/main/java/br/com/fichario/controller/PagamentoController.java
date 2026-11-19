package br.com.fichario.controller;

import br.com.fichario.dto.CorrecaoPagamentoRequest;
import br.com.fichario.dto.EntregaRequest;
import br.com.fichario.dto.ExtratoPagamentosResponse;
import br.com.fichario.dto.PagamentoRequest;
import br.com.fichario.dto.ValorTotalRequest;
import br.com.fichario.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Pagamentos e entrega. Quem faz a operacao vai no cabecalho X-Usuario-Id. */
@RestController
public class PagamentoController {

    private final PagamentoService service;

    public PagamentoController(PagamentoService service) {
        this.service = service;
    }

    @PostMapping("/pedidos/{id}/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public ExtratoPagamentosResponse registrar(@PathVariable Long id,
                                               @RequestHeader("X-Usuario-Id") Long usuarioId,
                                               @RequestBody @Valid PagamentoRequest dados) {
        return service.registrar(id, dados, usuarioId);
    }

    @GetMapping("/pedidos/{id}/pagamentos")
    public ExtratoPagamentosResponse consultar(@PathVariable Long id) {
        return service.consultar(id);
    }

    @PutMapping("/pedidos/{id}/pagamentos/{pagamentoId}")
    public ExtratoPagamentosResponse corrigir(@PathVariable Long id, @PathVariable Long pagamentoId,
                                              @RequestHeader("X-Usuario-Id") Long usuarioId,
                                              @RequestBody @Valid CorrecaoPagamentoRequest dados) {
        return service.corrigir(id, pagamentoId, dados, usuarioId);
    }

    @PutMapping("/pedidos/{id}/valor-total")
    public ExtratoPagamentosResponse alterarValorTotal(@PathVariable Long id,
                                                       @RequestHeader("X-Usuario-Id") Long usuarioId,
                                                       @RequestBody @Valid ValorTotalRequest dados) {
        return service.alterarValorTotal(id, dados.valorTotal(), usuarioId);
    }

    @PostMapping("/pedidos/{id}/entrega")
    public ExtratoPagamentosResponse registrarEntrega(@PathVariable Long id,
                                                      @RequestHeader("X-Usuario-Id") Long usuarioId,
                                                      @RequestBody(required = false) @Valid EntregaRequest dados) {
        return service.registrarEntrega(id, dados, usuarioId);
    }

    @GetMapping("/pagamentos/a-receber")
    public List<ExtratoPagamentosResponse> listarAReceber() {
        return service.listarAReceber();
    }
}
