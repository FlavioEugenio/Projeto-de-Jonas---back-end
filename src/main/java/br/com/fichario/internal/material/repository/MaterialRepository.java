package br.com.fichario.internal.material.repository;

import br.com.fichario.internal.material.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findAllByOrderByNomeAsc();
}
