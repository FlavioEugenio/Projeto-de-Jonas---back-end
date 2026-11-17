package br.com.fichario.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Valor recebido de um pedido (sinal, parcial ou saldo na entrega).
 * Nao e editado diretamente: correcoes passam pelo {@link Pedido} e guardam o valor original.
 */
@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormaPagamento forma;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    /** Nome de quem recebeu o dinheiro na entrega, quando houver. */
    @Column(name = "recebido_por", length = 100)
    private String recebidoPor;

    @Column(name = "valor_original", precision = 10, scale = 2)
    private BigDecimal valorOriginal;

    @Column(name = "motivo_correcao", length = 300)
    private String motivoCorrecao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrigido_por_id")
    private Usuario corrigidoPor;

    protected Pagamento() {
    }

    Pagamento(Pedido pedido, BigDecimal valor, FormaPagamento forma, Usuario registradoPor, String recebidoPor) {
        this.pedido = pedido;
        this.valor = valor;
        this.forma = forma;
        this.registradoPor = registradoPor;
        this.recebidoPor = recebidoPor;
        this.registradoEm = LocalDateTime.now();
    }

    void corrigir(BigDecimal novoValor, String motivo, Usuario autor) {
        if (valorOriginal == null) {
            valorOriginal = valor;
        }
        this.valor = novoValor;
        this.motivoCorrecao = motivo;
        this.corrigidoPor = autor;
    }

    public boolean foiCorrigido() {
        return valorOriginal != null;
    }

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public BigDecimal getValor() { return valor; }
    public FormaPagamento getForma() { return forma; }
    public LocalDateTime getRegistradoEm() { return registradoEm; }
    public Usuario getRegistradoPor() { return registradoPor; }
    public String getRecebidoPor() { return recebidoPor; }
    public BigDecimal getValorOriginal() { return valorOriginal; }
    public String getMotivoCorrecao() { return motivoCorrecao; }
    public Usuario getCorrigidoPor() { return corrigidoPor; }
}
