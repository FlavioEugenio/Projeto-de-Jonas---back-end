package br.com.fichario.internal.pedido.entity;

import br.com.fichario.internal.usuario.entity.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Linha do historico do pedido. Imutavel: o historico nunca e apagado. */
@Entity
@Table(name = "evento_pedido")
public class EventoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false, length = 500)
    private String descricao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @Column(name = "ocorrido_em", nullable = false)
    private LocalDateTime ocorridoEm;

    protected EventoPedido() {
    }

    EventoPedido(Pedido pedido, String descricao, Usuario autor) {
        this.pedido = pedido;
        this.descricao = descricao.length() > 500 ? descricao.substring(0, 497) + "..." : descricao;
        this.autor = autor;
        this.ocorridoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public String getDescricao() { return descricao; }
    public Usuario getAutor() { return autor; }
    public LocalDateTime getOcorridoEm() { return ocorridoEm; }
}
