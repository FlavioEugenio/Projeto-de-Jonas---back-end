package br.com.fichario.internal.material.controller;

import br.com.fichario.internal.material.dto.ItemCompraResponse;
import br.com.fichario.internal.material.dto.MaterialResponse;
import br.com.fichario.internal.material.service.MaterialService;
import br.com.fichario.internal.pedido.dto.PedidoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping("/materiais")
    public List<MaterialResponse> listar() {
        return materialService.listar();
    }

    @GetMapping("/compras")
    public List<ItemCompraResponse> listarCompras() {
        return materialService.listarCompras();
    }

    /** A producao confirma que o material ja esta separado para o pedido. */
    @PutMapping("/pedidos/{id}/materiais/{materialId}/disponivel")
    public PedidoResponse marcarDisponivel(@PathVariable Long id, @PathVariable Long materialId,
                                           @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return materialService.marcarDisponivel(id, materialId, usuarioId);
    }
}
