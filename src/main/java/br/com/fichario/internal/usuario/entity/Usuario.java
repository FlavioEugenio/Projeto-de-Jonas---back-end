package br.com.fichario.internal.usuario.entity;

import br.com.fichario.shared.exception.RegraDeNegocioException;
import br.com.fichario.shared.util.Validacao;
import jakarta.persistence.*;

import java.util.Arrays;

/** Funcionario que acessa o sistema com login individual. */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    /** Hash da senha. A senha em texto nunca chega ao dominio. */
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(nullable = false)
    private boolean ativo = true;

    protected Usuario() {
    }

    public Usuario(String nome, String login, String senhaHash, Perfil perfil) {
        this.nome = Validacao.texto(nome, "Nome", 100);
        this.login = Validacao.texto(login, "Login", 50).toLowerCase();
        this.senhaHash = Validacao.texto(senhaHash, "Senha", 255);
        this.perfil = Validacao.obrigatorio(perfil, "Perfil");
    }

    public void alterarPerfil(Perfil novoPerfil) {
        this.perfil = Validacao.obrigatorio(novoPerfil, "Perfil");
    }

    public void trocarSenha(String novoSenhaHash) {
        this.senhaHash = Validacao.texto(novoSenhaHash, "Senha", 255);
    }

    public void desativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    /** Garante que o usuario esta ativo e tem um dos perfis permitidos para a operacao. */
    public void exigirPerfil(Perfil... permitidos) {
        if (!ativo) {
            throw new RegraDeNegocioException("O usuario " + login + " esta desativado.");
        }
        if (Arrays.stream(permitidos).noneMatch(p -> p == perfil)) {
            throw new RegraDeNegocioException("O perfil " + perfil + " nao pode realizar esta operacao.");
        }
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getLogin() { return login; }
    public String getSenhaHash() { return senhaHash; }
    public Perfil getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
}
