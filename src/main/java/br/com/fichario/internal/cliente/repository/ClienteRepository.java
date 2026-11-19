package br.com.fichario.internal.cliente.repository;

import br.com.fichario.internal.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByTelefone(String telefone);

    List<Cliente> findAllByOrderByNomeAsc();
}
