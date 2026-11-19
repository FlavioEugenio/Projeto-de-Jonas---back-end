package br.com.fichario.internal.produto.repository;

import br.com.fichario.internal.produto.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
