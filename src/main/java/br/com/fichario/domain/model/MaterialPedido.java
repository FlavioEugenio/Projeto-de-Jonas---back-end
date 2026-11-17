package br.com.fichario.domain.model;

import jakarta.persistence.*;

/**
 * Material necessario para um pedido especifico e sua situacao (disponivel ou falta comprar).
 * Criado e alterado somente pelo {@link Pedido}.
 */
@Entity
@Table(name = "material_pedido",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pedido_id", "material_id"}))
public class MaterialPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    /** Comeca falso: o material so conta como disponivel depois de conferido. */
    @Column(nullable = false)
    private boolean disponivel;

    protected MaterialPedido() {
    }

    MaterialPedido(Pedido pedido, Material material) {
        this.pedido = pedido;
        this.material = material;
    }

    void marcarDisponivel() {
        this.disponivel = true;
    }

    void marcarEmFalta() {
        this.disponivel = false;
    }

    boolean refereA(Material outro) {
        return material == outro || material.equals(outro);
    }

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public Material getMaterial() { return material; }
    public boolean isDisponivel() { return disponivel; }
}
