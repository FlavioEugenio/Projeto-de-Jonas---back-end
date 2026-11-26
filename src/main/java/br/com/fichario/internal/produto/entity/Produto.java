package br.com.fichario.internal.produto.entity;

import br.com.fichario.internal.material.entity.Material;
import br.com.fichario.shared.util.Validacao;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @ManyToMany
    @JoinTable(name = "produto_material",
            joinColumns = @JoinColumn(name = "produto_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"))
    private Set<Material> materiaisPadrao = new LinkedHashSet<>();

    protected Produto() {
    }

    public Produto(String nome) {
        this.nome = Validacao.texto(nome, "Nome do produto", 100);
    }

    public void adicionarMaterialPadrao(Material material) {
        materiaisPadrao.add(Validacao.obrigatorio(material, "Material"));
    }

    public String getNome() { return nome; }
    public Set<Material> getMateriaisPadrao() { return Collections.unmodifiableSet(materiaisPadrao); }
}
