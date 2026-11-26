package br.com.fichario.internal.pedido.entity;

import br.com.fichario.internal.usuario.entity.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alteracao")
public class Alteracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false, length = 50)
    private String campo;

    @Column(name = "valor_anterior", length = 1000)
    private String valorAnterior;

    @Column(name = "valor_novo", length = 1000)
    private String valorNovo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "alterado_por_id")
    private Usuario alteradoPor;

    @Column(name = "alterado_em", nullable = false)
    private LocalDateTime alteradoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciencia_por_id")
    private Usuario cienciaPor;

    @Column(name = "ciencia_em")
    private LocalDateTime cienciaEm;

    protected Alteracao() {
    }

    Alteracao(Pedido pedido, String campo, String valorAnterior, String valorNovo, Usuario alteradoPor) {
        this.pedido = pedido;
        this.campo = campo;
        this.valorAnterior = valorAnterior;
        this.valorNovo = valorNovo;
        this.alteradoPor = alteradoPor;
        this.alteradoEm = LocalDateTime.now();
    }

    void registrarCiencia(Usuario responsavel) {
        this.cienciaPor = responsavel;
        this.cienciaEm = LocalDateTime.now();
    }

    boolean isPendente() {
        return cienciaEm == null;
    }
}
