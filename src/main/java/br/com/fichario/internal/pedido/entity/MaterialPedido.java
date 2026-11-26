package br.com.fichario.internal.pedido.entity;

import br.com.fichario.internal.material.entity.Material;
import jakarta.persistence.*;

@Entity
@Table(name = "material_pedido")
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

    public Material getMaterial() { return material; }
    public boolean isDisponivel() { return disponivel; }
}
