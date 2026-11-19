package br.com.fichario.repository;

import br.com.fichario.domain.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    List<Material> findAllByOrderByNomeAsc();
}
