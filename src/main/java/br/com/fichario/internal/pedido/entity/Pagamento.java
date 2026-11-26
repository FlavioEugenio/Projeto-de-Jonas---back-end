package br.com.fichario.internal.pedido.entity;

import br.com.fichario.internal.usuario.entity.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormaPagamento forma;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    @Column(name = "recebido_por", length = 100)
    private String recebidoPor;

    protected Pagamento() {
    }

    Pagamento(Pedido pedido, BigDecimal valor, FormaPagamento forma, Usuario registradoPor, String recebidoPor) {
        this.pedido = pedido;
        this.valor = valor;
        this.forma = forma;
        this.registradoPor = registradoPor;
        this.recebidoPor = recebidoPor;
        this.registradoEm = LocalDateTime.now();
    }

    BigDecimal getValor() { return valor; }
}
