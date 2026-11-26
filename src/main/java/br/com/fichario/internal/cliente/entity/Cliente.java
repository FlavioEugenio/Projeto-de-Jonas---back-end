package br.com.fichario.internal.cliente.entity;

import br.com.fichario.shared.exception.RegraDeNegocioException;
import br.com.fichario.shared.util.Validacao;
import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String telefone;

    protected Cliente() {
    }

    public Cliente(String nome, String telefone) {
        this.nome = Validacao.texto(nome, "Nome", 100);
        String digitos = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (digitos.length() < 10 || digitos.length() > 11) {
            throw new RegraDeNegocioException("O telefone deve ter DDD e numero, com 10 ou 11 digitos.");
        }
        this.telefone = digitos;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
}
