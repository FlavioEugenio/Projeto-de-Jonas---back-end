package br.com.fichario.internal.alteracao.controller;

import br.com.fichario.internal.alteracao.dto.AlterarPersonalizacaoRequest;
import br.com.fichario.internal.alteracao.service.AlteracaoService;
import br.com.fichario.internal.pedido.dto.PedidoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos/{id}")
@RequiredArgsConstructor
public class AlteracaoController {

    private final AlteracaoService alteracaoService;

    @PutMapping("/personalizacao")
    public PedidoResponse alterarPersonalizacao(@PathVariable Long id,
                                                @RequestHeader("X-Usuario-Id") Long usuarioId,
                                                @RequestBody @Valid AlterarPersonalizacaoRequest request) {
        return alteracaoService.alterarPersonalizacao(id, request.personalizacao(), usuarioId);
    }

    @PostMapping("/ciencia")
    public PedidoResponse confirmarCiencia(@PathVariable Long id,
                                           @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return alteracaoService.confirmarCiencia(id, usuarioId);
    }
}
