package br.com.fichario.controller;

import br.com.fichario.dto.AdicionarMaterialRequest;
import br.com.fichario.dto.ItemCompraResponse;
import br.com.fichario.dto.MaterialRequest;
import br.com.fichario.dto.MaterialResponse;
import br.com.fichario.dto.PedidoDetalheResponse;
import br.com.fichario.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Materiais e lista de compras. Quem faz a operacao vai no cabecalho X-Usuario-Id. */
@RestController
public class MaterialController {

    private final MaterialService service;

    public MaterialController(MaterialService service) {
        this.service = service;
    }

    @PostMapping("/materiais")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialResponse cadastrar(@RequestBody @Valid MaterialRequest dados) {
        return service.cadastrar(dados.nome());
    }

    @GetMapping("/materiais")
    public List<MaterialResponse> listar() {
        return service.listar();
    }

    @GetMapping("/compras")
    public List<ItemCompraResponse> listarCompras() {
        return service.listarCompras();
    }

    @PostMapping("/pedidos/{id}/materiais")
    public PedidoDetalheResponse adicionarAoPedido(@PathVariable Long id,
                                                   @RequestHeader("X-Usuario-Id") Long usuarioId,
                                                   @RequestBody @Valid AdicionarMaterialRequest dados) {
        return service.adicionarAoPedido(id, dados.materialId(), usuarioId);
    }

    @DeleteMapping("/pedidos/{id}/materiais/{materialId}")
    public PedidoDetalheResponse removerDoPedido(@PathVariable Long id, @PathVariable Long materialId,
                                                 @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return service.removerDoPedido(id, materialId, usuarioId);
    }

    @PutMapping("/pedidos/{id}/materiais/{materialId}/disponivel")
    public PedidoDetalheResponse marcarDisponivel(@PathVariable Long id, @PathVariable Long materialId,
                                                  @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return service.marcarDisponivel(id, materialId, usuarioId);
    }

    @PutMapping("/pedidos/{id}/materiais/{materialId}/em-falta")
    public PedidoDetalheResponse marcarEmFalta(@PathVariable Long id, @PathVariable Long materialId,
                                               @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return service.marcarEmFalta(id, materialId, usuarioId);
    }
}
