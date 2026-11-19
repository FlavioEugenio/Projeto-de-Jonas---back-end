package br.com.fichario.internal.produto.entity;

import br.com.fichario.internal.material.entity.Material;
import br.com.fichario.shared.util.Validacao;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Item do catalogo. Sua lista padrao de materiais e copiada para cada novo pedido. */
@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = false)
    private boolean ativo = true;

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

    public void renomear(String novoNome) {
        this.nome = Validacao.texto(novoNome, "Nome do produto", 100);
    }

    public void adicionarMaterialPadrao(Material material) {
        materiaisPadrao.add(Validacao.obrigatorio(material, "Material"));
    }

    public void removerMaterialPadrao(Material material) {
        materiaisPadrao.remove(material);
    }

    public void desativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public boolean isAtivo() { return ativo; }
    public Set<Material> getMateriaisPadrao() { return Collections.unmodifiableSet(materiaisPadrao); }
}
