package br.com.fichario.internal.pedido.entity;

import br.com.fichario.internal.cliente.entity.Cliente;
import br.com.fichario.internal.material.entity.Material;
import br.com.fichario.internal.produto.entity.Produto;
import br.com.fichario.internal.usuario.entity.Perfil;
import br.com.fichario.internal.usuario.entity.Usuario;
import br.com.fichario.shared.exception.RegraDeNegocioException;
import br.com.fichario.shared.util.Validacao;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false, length = 1000)
    private String personalizacao;

    @Column(name = "data_entrega", nullable = false)
    private LocalDate dataEntrega;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalOrigem canal;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    private Usuario criadoPor;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<MaterialPedido> materiais = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<Pagamento> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<Alteracao> alteracoes = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<EventoPedido> historico = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(Cliente cliente, Produto produto, int quantidade, String personalizacao,
                  LocalDate dataEntrega, BigDecimal valorTotal, CanalOrigem canal, Usuario criadoPor) {
        Validacao.obrigatorio(criadoPor, "Usuario").exigirPerfil(Perfil.ATENDIMENTO, Perfil.DONO);
        if (quantidade < 1) {
            throw new RegraDeNegocioException("A quantidade deve ser de 1 para cima.");
        }
        Validacao.obrigatorio(dataEntrega, "Data de entrega");
        if (dataEntrega.isBefore(LocalDate.now())) {
            throw new RegraDeNegocioException("A data de entrega nao pode estar no passado.");
        }
        this.cliente = Validacao.obrigatorio(cliente, "Cliente");
        this.produto = Validacao.obrigatorio(produto, "Produto");
        this.quantidade = quantidade;
        this.personalizacao = Validacao.texto(personalizacao, "Personalizacao", 1000);
        this.dataEntrega = dataEntrega;
        this.valorTotal = Validacao.dinheiroPositivo(valorTotal, "Valor total");
        this.canal = Validacao.obrigatorio(canal, "Canal");
        this.criadoPor = criadoPor;
        this.criadoEm = LocalDateTime.now();
        this.status = StatusPedido.RECEBIDO;
        produto.getMateriaisPadrao().forEach(m -> materiais.add(new MaterialPedido(this, m)));
        registrarEvento("Pedido criado.", criadoPor);
    }

    public void alterarPersonalizacao(String nova, Usuario autor) {
        exigirPerfil(autor, Perfil.ATENDIMENTO, Perfil.DONO);
        exigirStatus(StatusPedido.RECEBIDO, StatusPedido.EM_PRODUCAO);
        String novaPersonalizacao = Validacao.texto(nova, "Personalizacao", 1000);
        alteracoes.add(new Alteracao(this, "Personalizacao", personalizacao, novaPersonalizacao, autor));
        this.personalizacao = novaPersonalizacao;
        registrarEvento("Personalizacao alterada.", autor);
    }

    public void confirmarCiencia(Usuario responsavel) {
        exigirPerfil(responsavel, Perfil.PRODUCAO);
        List<Alteracao> pendentes = getAlteracoesPendentes();
        if (pendentes.isEmpty()) {
            throw new RegraDeNegocioException("Nao ha alteracoes pendentes neste pedido.");
        }
        pendentes.forEach(a -> a.registrarCiencia(responsavel));
        registrarEvento("Producao confirmou ciencia das alteracoes.", responsavel);
    }

    public void marcarMaterialDisponivel(Material material, Usuario autor) {
        exigirPerfil(autor, Perfil.PRODUCAO, Perfil.DONO);
        MaterialPedido item = materiais.stream()
                .filter(m -> m.getMaterial().equals(material))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Este material nao faz parte do pedido."));
        item.marcarDisponivel();
        registrarEvento("Material " + material.getNome() + " disponivel.", autor);
    }

    public void iniciarProducao(Usuario autor) {
        exigirPerfil(autor, Perfil.PRODUCAO, Perfil.DONO);
        exigirStatus(StatusPedido.RECEBIDO);
        exigirSemAlteracoesPendentes();
        if (!getMateriaisEmFalta().isEmpty()) {
            throw new RegraDeNegocioException("Ainda falta material para este pedido.");
        }
        mudarStatus(StatusPedido.EM_PRODUCAO, autor);
    }

    public void marcarPronto(Usuario autor) {
        exigirPerfil(autor, Perfil.PRODUCAO, Perfil.DONO);
        exigirStatus(StatusPedido.EM_PRODUCAO);
        exigirSemAlteracoesPendentes();
        mudarStatus(StatusPedido.PRONTO, autor);
    }

    public void registrarPagamento(BigDecimal valor, FormaPagamento forma, Usuario autor) {
        exigirPerfil(autor, Perfil.ATENDIMENTO, Perfil.DONO);
        exigirStatus(StatusPedido.RECEBIDO, StatusPedido.EM_PRODUCAO, StatusPedido.PRONTO);
        BigDecimal v = Validacao.dinheiroPositivo(valor, "Valor do pagamento");
        if (v.compareTo(getSaldo()) > 0) {
            throw new RegraDeNegocioException("O valor passa do saldo de R$ " + getSaldo() + ".");
        }
        adicionarPagamento(v, forma, autor, null);
    }

    public void registrarEntrega(BigDecimal valorRecebido, FormaPagamento forma, String recebidoPor, Usuario autor) {
        exigirPerfil(autor, Perfil.ATENDIMENTO, Perfil.DONO);
        exigirStatus(StatusPedido.PRONTO);
        exigirSemAlteracoesPendentes();
        BigDecimal saldo = getSaldo();
        if (saldo.signum() > 0) {
            BigDecimal recebido = Validacao.dinheiro(valorRecebido, "Valor recebido na entrega");
            if (recebido.compareTo(saldo) != 0) {
                throw new RegraDeNegocioException("A entrega so fecha com o saldo de R$ " + saldo + " recebido.");
            }
            adicionarPagamento(recebido, forma, autor, Validacao.texto(recebidoPor, "Quem recebeu", 100));
        }
        mudarStatus(StatusPedido.ENTREGUE, autor);
    }

    private BigDecimal getValorPago() {
        return pagamentos.stream().map(Pagamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSaldo() {
        return valorTotal.subtract(getValorPago());
    }

    public boolean possuiAlteracoesPendentes() {
        return !getAlteracoesPendentes().isEmpty();
    }

    private List<Alteracao> getAlteracoesPendentes() {
        return alteracoes.stream().filter(Alteracao::isPendente).toList();
    }

    public List<MaterialPedido> getMateriaisEmFalta() {
        return materiais.stream().filter(m -> !m.isDisponivel()).toList();
    }

    private void adicionarPagamento(BigDecimal valor, FormaPagamento forma, Usuario autor, String recebidoPor) {
        pagamentos.add(new Pagamento(this, valor, Validacao.obrigatorio(forma, "Forma de pagamento"),
                autor, recebidoPor));
        registrarEvento("Pagamento de R$ " + valor + " registrado.", autor);
    }

    private void mudarStatus(StatusPedido novo, Usuario autor) {
        this.status = novo;
        registrarEvento("Status: " + novo + ".", autor);
    }

    private void registrarEvento(String descricao, Usuario autor) {
        historico.add(new EventoPedido(this, descricao, autor));
    }

    private void exigirStatus(StatusPedido... permitidos) {
        if (!List.of(permitidos).contains(status)) {
            throw new RegraDeNegocioException("Operacao invalida para pedido com status " + status + ".");
        }
    }

    private void exigirSemAlteracoesPendentes() {
        if (possuiAlteracoesPendentes()) {
            throw new RegraDeNegocioException("A producao precisa confirmar a alteracao do pedido antes.");
        }
    }

    private static void exigirPerfil(Usuario usuario, Perfil... permitidos) {
        Validacao.obrigatorio(usuario, "Usuario").exigirPerfil(permitidos);
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public String getPersonalizacao() { return personalizacao; }
    public LocalDate getDataEntrega() { return dataEntrega; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public StatusPedido getStatus() { return status; }
    public List<MaterialPedido> getMateriais() { return Collections.unmodifiableList(materiais); }
    List<Alteracao> getAlteracoes() { return Collections.unmodifiableList(alteracoes); }
    List<EventoPedido> getHistorico() { return Collections.unmodifiableList(historico); }
}
