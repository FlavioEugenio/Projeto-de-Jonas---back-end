package br.com.fichario.dto;

import br.com.fichario.domain.model.Cliente;

public record ClienteResponse(Long id, String nome, String telefone, String instagram, String observacoes) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getTelefone(),
                cliente.getInstagram(), cliente.getObservacoes());
    }
}
