package br.com.fichario.internal.pedido.controller;

import br.com.fichario.internal.pedido.dto.NovoPedidoRequest;
import br.com.fichario.internal.pedido.dto.PedidoDetalheResponse;
import br.com.fichario.internal.pedido.dto.PedidoResumoResponse;
import br.com.fichario.internal.pedido.entity.StatusPedido;
import br.com.fichario.internal.pedido.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Cadastro e consulta de pedidos. Enquanto o login nao existe, quem faz a operacao
 * e informado no cabecalho {@value #USUARIO}.
 */
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    static final String USUARIO = "X-Usuario-Id";

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PedidoDetalheResponse> cadastrar(@RequestHeader(USUARIO) Long usuarioId,
                                                           @RequestBody @Valid NovoPedidoRequest dados) {
        PedidoDetalheResponse criado = service.cadastrar(dados, usuarioId);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    /** GET /pedidos?status=RECEBIDO&dataEntrega=2026-12-05&clienteId=1 (todos opcionais) */
    @GetMapping
    public List<PedidoResumoResponse> listar(
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataEntrega,
            @RequestParam(required = false) Long clienteId) {
        return service.listar(status, dataEntrega, clienteId);
    }

    @GetMapping("/{id}")
    public PedidoDetalheResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
}
