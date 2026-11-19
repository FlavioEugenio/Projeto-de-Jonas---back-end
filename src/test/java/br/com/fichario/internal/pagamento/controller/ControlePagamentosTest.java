package br.com.fichario.internal.pagamento.controller;

import br.com.fichario.internal.pedido.entity.Pedido;
import br.com.fichario.internal.pedido.repository.PedidoRepository;
import br.com.fichario.internal.produto.entity.Produto;
import br.com.fichario.internal.produto.repository.ProdutoRepository;
import br.com.fichario.internal.usuario.entity.Usuario;
import br.com.fichario.internal.usuario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControlePagamentosTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private ProdutoRepository produtos;

    @Autowired
    private PedidoRepository pedidos;

    @Autowired
    private TransactionTemplate transacao;

    private Long ana;
    private Long marcos;

    @BeforeEach
    void carregarIds() {
        ana = usuarios.findByLogin("ana").orElseThrow().getId();
        marcos = usuarios.findByLogin("marcos").orElseThrow().getId();
    }

    @Test
    void sinalReduzOSaldoEPedidoApareceNaListaAReceber() throws Exception {
        long pedido = criarPedido("85955550001");

        pagar(pedido, ana, "100.00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorPago").value(100.0))
                .andExpect(jsonPath("$.saldo").value(250.0));

        mvc.perform(get("/pagamentos/a-receber"))
                .andExpect(jsonPath("$[*].pedidoId", hasItem((int) pedido)));
    }

    @Test
    void naoAceitaPagamentoAcimaDoSaldo() throws Exception {
        long pedido = criarPedido("85955550002");

        pagar(pedido, ana, "400.00").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void entregaSoFechaComOSaldoCompletoRecebido() throws Exception {
        long pedido = criarPedido("85955550003");
        pagar(pedido, ana, "100.00");
        deixarPronto(pedido);

        entregar(pedido, "{\"valorRecebido\": 100.00, \"forma\": \"DINHEIRO\", \"recebidoPor\": \"Ana\"}")
                .andExpect(status().isUnprocessableEntity());
        entregar(pedido, "{\"valorRecebido\": 250.00, \"forma\": \"DINHEIRO\", \"recebidoPor\": \"Ana\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.saldo").value(0.0))
                .andExpect(jsonPath("$.pagamentos[1].recebidoPor").value("Ana"));
    }

    @Test
    void somenteODonoCorrigePagamento() throws Exception {
        long pedido = criarPedido("85955550004");
        String extrato = pagar(pedido, ana, "100.00").andReturn().getResponse().getContentAsString();
        long pagamento = ((Number) JsonPath.read(extrato, "$.pagamentos[0].id")).longValue();
        String correcao = "{\"valor\": 80.00, \"motivo\": \"Pix caiu com valor errado\"}";

        mvc.perform(put("/pedidos/{id}/pagamentos/{p}", pedido, pagamento).header("X-Usuario-Id", ana)
                        .contentType(MediaType.APPLICATION_JSON).content(correcao))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(put("/pedidos/{id}/pagamentos/{p}", pedido, pagamento).header("X-Usuario-Id", marcos)
                        .contentType(MediaType.APPLICATION_JSON).content(correcao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagamentos[0].valorOriginal").value(100.0))
                .andExpect(jsonPath("$.saldo").value(270.0));
    }

    private ResultActions pagar(long pedido, Long usuario, String valor) throws Exception {
        return mvc.perform(post("/pedidos/{id}/pagamentos", pedido).header("X-Usuario-Id", usuario)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\": %s, \"forma\": \"PIX\"}".formatted(valor)));
    }

    private ResultActions entregar(long pedido, String corpo) throws Exception {
        return mvc.perform(post("/pedidos/{id}/entrega", pedido).header("X-Usuario-Id", ana)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    /** A producao e outra funcionalidade; aqui o pedido vai direto pelo dominio ate PRONTO. */
    private void deixarPronto(long pedidoId) {
        transacao.executeWithoutResult(t -> {
            Usuario beto = usuarios.findByLogin("beto").orElseThrow();
            Pedido pedido = pedidos.findById(pedidoId).orElseThrow();
            pedido.iniciarProducao(beto);
            pedido.marcarPronto(beto);
        });
    }

    private long criarPedido(String telefone) throws Exception {
        String cliente = mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Cliente %s\", \"telefone\": \"%s\"}".formatted(telefone, telefone)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long produto = produtos.save(new Produto("Caneca teste pagamento " + telefone)).getId();
        String pedido = mvc.perform(post("/pedidos").header("X-Usuario-Id", ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId": %d, "produtoId": %d, "quantidade": 5,
                                 "personalizacao": "Foto da familia", "dataEntrega": "%s",
                                 "valorTotal": 350.00, "canal": "PRESENCIAL"}
                                """.formatted(((Number) JsonPath.read(cliente, "$.id")).longValue(), produto,
                                LocalDate.now().plusDays(25))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(pedido, "$.id")).longValue();
    }
}
