package br.com.fichario.internal.cliente.mapper;

import br.com.fichario.internal.cliente.dto.ClienteResponse;
import br.com.fichario.internal.cliente.entity.Cliente;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static ClienteResponse paraResposta(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getTelefone());
    }
}
