package br.com.fichario.internal.pedido.entity;

import br.com.fichario.internal.agenda.entity.ConfiguracaoAgenda;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static br.com.fichario.internal.usuario.entity.Perfil.ATENDIMENTO;
import static br.com.fichario.internal.usuario.entity.Perfil.DONO;
import static br.com.fichario.internal.usuario.entity.Perfil.PRODUCAO;

/**
 * Raiz do agregado de encomenda. E a fonte unica sobre o pedido: dados, alteracoes,
 * materiais, pagamentos, status e historico. Toda mudanca passa por um metodo deste
 * objeto, que valida a regra de negocio e registra o historico.
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Controle de concorrencia: impede que duas pessoas salvem versoes diferentes ao mesmo tempo. */
    @Version
    private Long versao;

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

    @Column(length = 1000)
    private String observacoes;

    @Column(name = "data_entrega", nullable = false)
    private LocalDate dataEntrega;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalOrigem canal;

    @Column(name = "confirmado_pelo_cliente", nullable = false)
    private boolean confirmadoPeloCliente;

    @Column(name = "motivo_cancelamento", length = 300)
    private String motivoCancelamento;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    private Usuario criadoPor;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
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

    /** Registra um pedido novo. Os materiais padrao do produto entram como "falta conferir". */
    public Pedido(Cliente cliente, Produto produto, int quantidade, String personalizacao,
                  LocalDate dataEntrega, BigDecimal valorTotal, CanalOrigem canal, Usuario criadoPor) {
        exigirPerfil(criadoPor, ATENDIMENTO, DONO);
        this.cliente = Validacao.obrigatorio(cliente, "Cliente");
        this.produto = Validacao.obrigatorio(produto, "Produto");
        if (!produto.isAtivo()) {
            throw new RegraDeNegocioException("O produto " + produto.getNome() + " esta desativado.");
        }
        this.quantidade = validarQuantidade(quantidade);
        this.personalizacao = Validacao.texto(personalizacao, "Personalizacao", 1000);
        this.dataEntrega = validarDataEntrega(dataEntrega);
        this.valorTotal = Validacao.dinheiroPositivo(valorTotal, "Valor total");
        this.canal = Validacao.obrigatorio(canal, "Canal");
        this.criadoPor = criadoPor;
        this.criadoEm = LocalDateTime.now();
        this.status = StatusPedido.RECEBIDO;
        produto.getMateriaisPadrao().forEach(m -> materiais.add(new MaterialPedido(this, m)));
        registrarEvento("Pedido criado. Canal: " + canal + ".", criadoPor);
    }

    // ---------------------------------------------------------------- alteracoes do pedido

    public void alterarPersonalizacao(String novaPersonalizacao, Usuario autor) {
        prepararAlteracao(autor);
        String nova = Validacao.texto(novaPersonalizacao, "Personalizacao", 1000);
        if (registrarAlteracao("Personalizacao", personalizacao, nova, true, autor)) {
            this.personalizacao = nova;
        }
    }

    public void alterarQuantidade(int novaQuantidade, Usuario autor) {
        prepararAlteracao(autor);
        int nova = validarQuantidade(novaQuantidade);
        if (registrarAlteracao("Quantidade", quantidade, nova, true, autor)) {
            this.quantidade = nova;
        }
    }

    public void alterarDataEntrega(LocalDate novaData, Usuario autor) {
        prepararAlteracao(autor);
        LocalDate nova = validarDataEntrega(novaData);
        if (registrarAlteracao("Data de entrega", dataEntrega.format(DATA), nova.format(DATA), true, autor)) {
            this.dataEntrega = nova;
        }
    }

    public void alterarObservacoes(String novasObservacoes, Usuario autor) {
        prepararAlteracao(autor);
        String novas = Validacao.textoOpcional(novasObservacoes, "Observacoes", 1000);
        if (registrarAlteracao("Observacoes", observacoes, novas, true, autor)) {
            this.observacoes = novas;
        }
    }

    /** Muda o preco. Nao afeta a producao, mas o saldo e recalculado. */
    public void alterarValorTotal(BigDecimal novoValor, Usuario autor) {
        prepararAlteracao(autor);
        BigDecimal novo = Validacao.dinheiroPositivo(novoValor, "Valor total");
        if (novo.compareTo(getValorPago()) < 0) {
            throw new RegraDeNegocioException("O valor total nao pode ficar abaixo do que ja foi pago ("
                    + reais(getValorPago()) + ").");
        }
        if (registrarAlteracao("Valor total", reais(valorTotal), reais(novo), false, autor)) {
            this.valorTotal = novo;
        }
    }

    /** Cliente confirmou no WhatsApp o resumo da versao atual do pedido. */
    public void confirmarPeloCliente(Usuario autor) {
        exigirPerfil(autor, ATENDIMENTO, DONO);
        exigirAberto();
        if (!confirmadoPeloCliente) {
            this.confirmadoPeloCliente = true;
            registrarEvento("Cliente confirmou o resumo do pedido.", autor);
        }
    }

    /** Guarda no historico que o prazo foi prometido mesmo com o dia acima do limite. */
    public void registrarDataAcimaDoLimite(ConfiguracaoAgenda agenda, long pedidosNoDia, Usuario autor) {
        exigirPerfil(autor, ATENDIMENTO, DONO);
        Validacao.obrigatorio(agenda, "Configuracao da agenda");
        registrarEvento("Entrega mantida em " + dataEntrega.format(DATA) + " acima do limite: "
                + pedidosNoDia + " pedidos para " + agenda.getLimitePedidosPorDia() + " vagas.", autor);
    }

    // ---------------------------------------------------------------- materiais

    /** Mudanca feita fora da producao precisa da ciencia dela. */
    public void adicionarMaterial(Material material, Usuario autor) {
        exigirPerfil(autor, ATENDIMENTO, PRODUCAO, DONO);
        exigirAberto();
        Validacao.obrigatorio(material, "Material");
        if (buscarMaterial(material) != null) {
            throw new RegraDeNegocioException("O material " + material.getNome() + " ja esta no pedido.");
        }
        materiais.add(new MaterialPedido(this, material));
        registrarAlteracao("Material", "(nao previsto)", material.getNome(), autor.getPerfil() != PRODUCAO, autor);
    }

    public void removerMaterial(Material material, Usuario autor) {
        exigirPerfil(autor, ATENDIMENTO, PRODUCAO, DONO);
        exigirAberto();
        MaterialPedido item = materialDoPedido(material);
        materiais.remove(item);
        registrarAlteracao("Material", material.getNome(), "(removido)", autor.getPerfil() != PRODUCAO, autor);
    }

    public void marcarMaterialDisponivel(Material material, Usuario autor) {
        exigirPerfil(autor, PRODUCAO, DONO);
        exigirAberto();
        MaterialPedido item = materialDoPedido(material);
        if (!item.isDisponivel()) {
            item.marcarDisponivel();
            registrarEvento("Material " + material.getNome() + " disponivel.", autor);
        }
    }

    public void marcarMaterialEmFalta(Material material, Usuario autor) {
        exigirPerfil(autor, PRODUCAO, DONO);
        exigirAberto();
        MaterialPedido item = materialDoPedido(material);
        if (item.isDisponivel()) {
            item.marcarEmFalta();
            registrarEvento("Material " + material.getNome() + " marcado como falta comprar.", autor);
        }
    }

    // ---------------------------------------------------------------- producao

    /** A producao confirma que viu todas as alteracoes pendentes. */
    public void confirmarCiencia(Usuario responsavel) {
        exigirPerfil(responsavel, PRODUCAO);
        exigirAberto();
        List<Alteracao> pendentes = getAlteracoesPendentes();
        if (pendentes.isEmpty()) {
            throw new RegraDeNegocioException("Nao ha alteracoes pendentes neste pedido.");
        }
        pendentes.forEach(a -> a.registrarCiencia(responsavel));
        String campos = pendentes.stream().map(Alteracao::getCampo).distinct().collect(Collectors.joining(", "));
        registrarEvento("Producao confirmou ciencia da alteracao: " + campos + ".", responsavel);
    }

    public void iniciarProducao(Usuario autor) {
        exigirPerfil(autor, PRODUCAO, DONO);
        exigirStatus(StatusPedido.RECEBIDO, StatusPedido.AGUARDANDO_MATERIAL);
        exigirSemAlteracoesPendentes();
        List<MaterialPedido> emFalta = getMateriaisEmFalta();
        if (!emFalta.isEmpty()) {
            throw new RegraDeNegocioException("Falta comprar: " + emFalta.stream()
                    .map(m -> m.getMaterial().getNome()).collect(Collectors.joining(", ")) + ".");
        }
        mudarStatus(StatusPedido.EM_PRODUCAO, autor);
    }

    public void aguardarMaterial(Usuario autor) {
        exigirPerfil(autor, PRODUCAO, DONO);
        exigirStatus(StatusPedido.RECEBIDO, StatusPedido.EM_PRODUCAO);
        mudarStatus(StatusPedido.AGUARDANDO_MATERIAL, autor);
    }

    public void marcarPronto(Usuario autor) {
        exigirPerfil(autor, PRODUCAO, DONO);
        exigirStatus(StatusPedido.EM_PRODUCAO);
        exigirSemAlteracoesPendentes();
        mudarStatus(StatusPedido.PRONTO, autor);
    }

    public void voltarParaProducao(Usuario autor) {
        exigirPerfil(autor, PRODUCAO, DONO);
        exigirStatus(StatusPedido.PRONTO);
        mudarStatus(StatusPedido.EM_PRODUCAO, autor);
    }

    // ---------------------------------------------------------------- pagamento e entrega

    public Pagamento registrarPagamento(BigDecimal valor, FormaPagamento forma, Usuario autor) {
        exigirPerfil(autor, ATENDIMENTO, DONO);
        exigirAberto();
        BigDecimal v = Validacao.dinheiroPositivo(valor, "Valor do pagamento");
        if (v.compareTo(getSaldo()) > 0) {
            throw new RegraDeNegocioException("O valor passa do saldo de " + reais(getSaldo()) + ".");
        }
        return adicionarPagamento(v, Validacao.obrigatorio(forma, "Forma de pagamento"), autor, null);
    }

    /** Correcao feita somente pelo dono. O valor original e o motivo ficam guardados. */
    public void corrigirPagamento(Pagamento pagamento, BigDecimal novoValor, String motivo, Usuario autor) {
        exigirPerfil(autor, DONO);
        exigirAberto();
        if (pagamento == null || !pagamentos.contains(pagamento)) {
            throw new RegraDeNegocioException("Este pagamento nao pertence ao pedido.");
        }
        BigDecimal novo = Validacao.dinheiroPositivo(novoValor, "Valor corrigido");
        String justificativa = Validacao.texto(motivo, "Motivo da correcao", 300);
        BigDecimal antes = pagamento.getValor();
        if (getValorPago().subtract(antes).add(novo).compareTo(valorTotal) > 0) {
            throw new RegraDeNegocioException("Com a correcao, o total pago passaria do valor do pedido.");
        }
        if (antes.compareTo(novo) != 0) {
            pagamento.corrigir(novo, justificativa, autor);
            registrarEvento("Pagamento corrigido de " + reais(antes) + " para " + reais(novo)
                    + ". Motivo: " + justificativa + ".", autor);
        }
    }

    /**
     * Fecha o pedido. Com saldo em aberto, so fecha se o saldo completo for recebido
     * e se for informado quem recebeu o valor.
     */
    public void registrarEntrega(BigDecimal valorRecebido, FormaPagamento forma, String recebidoPor, Usuario autor) {
        exigirPerfil(autor, ATENDIMENTO, DONO);
        exigirStatus(StatusPedido.PRONTO);
        exigirSemAlteracoesPendentes();
        BigDecimal saldo = getSaldo();
        if (saldo.signum() > 0) {
            BigDecimal recebido = Validacao.dinheiro(valorRecebido, "Valor recebido na entrega");
            if (recebido.compareTo(saldo) != 0) {
                throw new RegraDeNegocioException("A entrega so fecha com o saldo de " + reais(saldo) + " recebido.");
            }
            String quem = Validacao.texto(recebidoPor, "Quem recebeu o valor", 100);
            adicionarPagamento(recebido, Validacao.obrigatorio(forma, "Forma de pagamento"), autor, quem);
        } else if (valorRecebido != null && valorRecebido.signum() > 0) {
            throw new RegraDeNegocioException("O pedido ja esta quitado. Nao ha valor a receber.");
        }
        mudarStatus(StatusPedido.ENTREGUE, autor);
    }

    /** Somente o dono cancela, sempre com motivo. O historico continua guardado. */
    public void cancelar(String motivo, Usuario autor) {
        exigirPerfil(autor, DONO);
        exigirAberto();
        this.motivoCancelamento = Validacao.texto(motivo, "Motivo do cancelamento", 300);
        this.status = StatusPedido.CANCELADO;
        registrarEvento("Pedido cancelado. Motivo: " + motivoCancelamento + ".", autor);
    }

    // ---------------------------------------------------------------- consultas

    public BigDecimal getValorPago() {
        return pagamentos.stream().map(Pagamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Calculado, nunca digitado: valor total menos a soma dos pagamentos. */
    public BigDecimal getSaldo() {
        return valorTotal.subtract(getValorPago());
    }

    public boolean possuiAlteracoesPendentes() {
        return alteracoes.stream().anyMatch(Alteracao::isPendente);
    }

    public List<Alteracao> getAlteracoesPendentes() {
        return alteracoes.stream().filter(Alteracao::isPendente).toList();
    }

    public List<MaterialPedido> getMateriaisEmFalta() {
        return materiais.stream().filter(m -> !m.isDisponivel()).toList();
    }

    public boolean estaAberto() {
        return status.aberto();
    }

    /** Pedido cancelado nao ocupa vaga no dia de entrega. */
    public boolean ocupaAgenda() {
        return status != StatusPedido.CANCELADO;
    }

    public boolean estaAtrasado(LocalDate hoje) {
        return estaAberto() && dataEntrega.isBefore(hoje);
    }

    // ---------------------------------------------------------------- apoio interno

    private void prepararAlteracao(Usuario autor) {
        exigirPerfil(autor, ATENDIMENTO, DONO);
        exigirAberto();
    }

    /** Registra a alteracao e o historico. Retorna falso quando o valor nao mudou. */
    private boolean registrarAlteracao(String campo, Object antes, Object depois, boolean exigeCiencia, Usuario autor) {
        String de = antes == null ? null : antes.toString();
        String para = depois == null ? null : depois.toString();
        if (Objects.equals(de, para)) {
            return false;
        }
        alteracoes.add(new Alteracao(this, campo, de, para, autor, exigeCiencia));
        this.confirmadoPeloCliente = false;
        registrarEvento(campo + " alterado: " + exibir(de) + " para " + exibir(para) + ".", autor);
        return true;
    }

    private Pagamento adicionarPagamento(BigDecimal valor, FormaPagamento forma, Usuario autor, String recebidoPor) {
        Pagamento pagamento = new Pagamento(this, valor, forma, autor, recebidoPor);
        pagamentos.add(pagamento);
        registrarEvento("Pagamento de " + reais(valor) + " registrado (" + forma + ")"
                + (recebidoPor == null ? "" : ", recebido por " + recebidoPor)
                + ". Saldo: " + reais(getSaldo()) + ".", autor);
        return pagamento;
    }

    private void mudarStatus(StatusPedido novo, Usuario autor) {
        this.status = novo;
        registrarEvento("Status: " + novo + ".", autor);
    }

    private void registrarEvento(String descricao, Usuario autor) {
        historico.add(new EventoPedido(this, descricao, autor));
    }

    private MaterialPedido buscarMaterial(Material material) {
        return materiais.stream().filter(m -> m.refereA(material)).findFirst().orElse(null);
    }

    private MaterialPedido materialDoPedido(Material material) {
        Validacao.obrigatorio(material, "Material");
        MaterialPedido item = buscarMaterial(material);
        if (item == null) {
            throw new RegraDeNegocioException("O material " + material.getNome() + " nao esta no pedido.");
        }
        return item;
    }

    private void exigirAberto() {
        if (!status.aberto()) {
            throw new RegraDeNegocioException("O pedido esta " + status + " e nao pode mais ser alterado.");
        }
    }

    private void exigirStatus(StatusPedido... permitidos) {
        if (Arrays.stream(permitidos).noneMatch(s -> s == status)) {
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

    private static int validarQuantidade(int quantidade) {
        if (quantidade < 1) {
            throw new RegraDeNegocioException("A quantidade deve ser de 1 para cima.");
        }
        return quantidade;
    }

    private static LocalDate validarDataEntrega(LocalDate data) {
        Validacao.obrigatorio(data, "Data de entrega");
        if (data.isBefore(LocalDate.now())) {
            throw new RegraDeNegocioException("A data de entrega nao pode estar no passado.");
        }
        return data;
    }

    private static String reais(BigDecimal valor) {
        return "R$ " + valor.setScale(2).toPlainString().replace('.', ',');
    }

    private static String exibir(String valor) {
        return valor == null ? "(vazio)" : valor;
    }

    // ---------------------------------------------------------------- leitura

    public Long getId() { return id; }
    public Long getVersao() { return versao; }
    public Cliente getCliente() { return cliente; }
    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public String getPersonalizacao() { return personalizacao; }
    public String getObservacoes() { return observacoes; }
    public LocalDate getDataEntrega() { return dataEntrega; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public StatusPedido getStatus() { return status; }
    public CanalOrigem getCanal() { return canal; }
    public boolean isConfirmadoPeloCliente() { return confirmadoPeloCliente; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public Usuario getCriadoPor() { return criadoPor; }
    public List<MaterialPedido> getMateriais() { return Collections.unmodifiableList(materiais); }
    public List<Pagamento> getPagamentos() { return Collections.unmodifiableList(pagamentos); }
    public List<Alteracao> getAlteracoes() { return Collections.unmodifiableList(alteracoes); }
    public List<EventoPedido> getHistorico() { return Collections.unmodifiableList(historico); }
}
