package br.com.fichario.internal.pedido.repository;

import br.com.fichario.internal.pedido.entity.Pedido;
import br.com.fichario.internal.pedido.entity.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByDataEntregaAsc();

    List<Pedido> findByStatusInOrderByDataEntregaAsc(List<StatusPedido> status);
}
