package br.com.fichario.internal.material.mapper;

import br.com.fichario.internal.material.dto.MaterialResponse;
import br.com.fichario.internal.material.entity.Material;

/** Converte a entidade Material para os DTOs de saida. */
public final class MaterialMapper {

    private MaterialMapper() {
    }

    public static MaterialResponse paraResposta(Material material) {
        return new MaterialResponse(material.getId(), material.getNome());
    }
}
