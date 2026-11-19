package br.com.fichario.dto;

import br.com.fichario.domain.model.Material;

public record MaterialResponse(Long id, String nome) {

    public static MaterialResponse de(Material material) {
        return new MaterialResponse(material.getId(), material.getNome());
    }
}
