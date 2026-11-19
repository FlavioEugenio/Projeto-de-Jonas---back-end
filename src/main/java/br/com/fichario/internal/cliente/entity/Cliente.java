package br.com.fichario.internal.cliente.entity;

import br.com.fichario.shared.exception.RegraDeNegocioException;
import br.com.fichario.shared.util.Validacao;
import jakarta.persistence.*;

/** Pessoa ou empresa que faz encomendas. Identificada pelo telefone do WhatsApp. */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    /** Somente digitos, com DDD. Unico para evitar cadastro duplicado. */
    @Column(nullable = false, unique = true, length = 11)
    private String telefone;

    @Column(length = 60)
    private String instagram;

    @Column(length = 500)
    private String observacoes;

    protected Cliente() {
    }

    public Cliente(String nome, String telefone) {
        this.nome = Validacao.texto(nome, "Nome", 100);
        this.telefone = normalizarTelefone(telefone);
    }

    public void atualizarNome(String novoNome) {
        this.nome = Validacao.texto(novoNome, "Nome", 100);
    }

    public void atualizarTelefone(String novoTelefone) {
        this.telefone = normalizarTelefone(novoTelefone);
    }

    public void atualizarInstagram(String novoInstagram) {
        this.instagram = Validacao.textoOpcional(novoInstagram, "Instagram", 60);
    }

    public void atualizarObservacoes(String novasObservacoes) {
        this.observacoes = Validacao.textoOpcional(novasObservacoes, "Observacoes", 500);
    }

    /** Remove a mascara e exige DDD mais numero (10 ou 11 digitos). */
    public static String normalizarTelefone(String telefone) {
        String digitos = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (digitos.length() < 10 || digitos.length() > 11) {
            throw new RegraDeNegocioException("O telefone deve ter DDD e numero, com 10 ou 11 digitos.");
        }
        return digitos;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public String getInstagram() { return instagram; }
    public String getObservacoes() { return observacoes; }
}
