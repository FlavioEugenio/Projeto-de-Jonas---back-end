package br.com.fichario.domain.model;

import br.com.fichario.domain.exception.RegraDeNegocioException;
import jakarta.persistence.*;

/** Limite de pedidos por dia de entrega, usado para avisar antes de prometer um prazo. */
@Entity
@Table(name = "configuracao_agenda")
public class ConfiguracaoAgenda {

    public static final int LIMITE_MAXIMO = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "limite_pedidos_por_dia", nullable = false)
    private int limitePedidosPorDia;

    protected ConfiguracaoAgenda() {
    }

    public ConfiguracaoAgenda(int limitePedidosPorDia) {
        this.limitePedidosPorDia = validarLimite(limitePedidosPorDia);
    }

    /** Somente o dono define a capacidade da empresa. */
    public void alterarLimite(int novoLimite, Usuario autor) {
        Validacao.obrigatorio(autor, "Usuario").exigirPerfil(Perfil.DONO);
        this.limitePedidosPorDia = validarLimite(novoLimite);
    }

    /** Indica se um dia com essa quantidade de pedidos aceita mais um sem passar do limite. */
    public boolean comportaMaisUmPedido(long pedidosNoDia) {
        return pedidosNoDia < limitePedidosPorDia;
    }

    private static int validarLimite(int limite) {
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new RegraDeNegocioException("O limite por dia deve ficar entre 1 e " + LIMITE_MAXIMO + ".");
        }
        return limite;
    }

    public Long getId() { return id; }
    public int getLimitePedidosPorDia() { return limitePedidosPorDia; }
}
