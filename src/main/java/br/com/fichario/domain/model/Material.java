package br.com.fichario.domain.model;

import jakarta.persistence.*;

/** Insumo usado na producao, como papel, fita ou caneca. */
@Entity
@Table(name = "material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    protected Material() {
    }

    public Material(String nome) {
        this.nome = Validacao.texto(nome, "Nome do material", 100);
    }

    public void renomear(String novoNome) {
        this.nome = Validacao.texto(novoNome, "Nome do material", 100);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }

    /** Igualdade pelo id depois de salvo; antes disso, pela propria instancia. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Material outro)) return false;
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Material.class.hashCode();
    }
}
