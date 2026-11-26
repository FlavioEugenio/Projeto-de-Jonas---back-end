package br.com.fichario.internal.usuario.entity;

import br.com.fichario.shared.exception.RegraDeNegocioException;
import br.com.fichario.shared.util.Validacao;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    protected Usuario() {
    }

    public Usuario(String nome, Perfil perfil) {
        this.nome = Validacao.texto(nome, "Nome", 100);
        this.perfil = Validacao.obrigatorio(perfil, "Perfil");
    }

    public void exigirPerfil(Perfil... permitidos) {
        if (!List.of(permitidos).contains(perfil)) {
            throw new RegraDeNegocioException("O perfil " + perfil + " nao pode realizar esta operacao.");
        }
    }
}
