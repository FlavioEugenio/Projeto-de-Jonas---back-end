package br.com.fichario;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Percorre o caminho do pedido pelos 5 modulos, chamando os endpoints de verdade.
 * Usa os dados iniciais: 1 = Ana (atendimento), 2 = Beto (producao), produto 1 com materiais 1 e 2.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FluxoPedidoTest {

    private static final long ANA = 1;
    private static final long BETO = 2;

    @Autowired
    private MockMvc mvc;

    @Test
    void pedidoVaiDoCadastroAteAEntrega() throws Exception {
        long cliente = criarCliente("(85) 98888-0001");
        long pedido = criarPedido(cliente);

        // alteracao: fica pendente e trava a producao ate a ciencia
        enviar(put("/pedidos/{id}/personalizacao", pedido), ANA, "{\"personalizacao\": \"Tema jardim, fita rosa\"}")
                .andExpect(jsonPath("$.alteracoesPendentes").value(true));
        enviar(post("/pedidos/{id}/ciencia", pedido), BETO, null)
                .andExpect(jsonPath("$.alteracoesPendentes").value(false));

        // materiais: sem material, a producao nao comeca
        enviar(post("/pedidos/{id}/iniciar-producao", pedido), BETO, null)
                .andExpect(status().isUnprocessableEntity());
        enviar(put("/pedidos/{id}/materiais/1/disponivel", pedido), BETO, null).andExpect(status().isOk());
        enviar(put("/pedidos/{id}/materiais/2/disponivel", pedido), BETO, null).andExpect(status().isOk());

        // producao
        enviar(post("/pedidos/{id}/iniciar-producao", pedido), BETO, null)
                .andExpect(jsonPath("$.status").value("EM_PRODUCAO"));
        enviar(post("/pedidos/{id}/pronto", pedido), BETO, null)
                .andExpect(jsonPath("$.status").value("PRONTO"));

        // pagamento: sinal de 100, e a entrega so fecha com o saldo de 250
        enviar(post("/pedidos/{id}/pagamentos", pedido), ANA, "{\"valor\": 100.00, \"forma\": \"PIX\"}")
                .andExpect(jsonPath("$.saldo").value(250.0));
        enviar(post("/pedidos/{id}/entrega", pedido), ANA,
                "{\"valorRecebido\": 100.00, \"forma\": \"DINHEIRO\", \"recebidoPor\": \"Ana\"}")
                .andExpect(status().isUnprocessableEntity());
        enviar(post("/pedidos/{id}/entrega", pedido), ANA,
                "{\"valorRecebido\": 250.00, \"forma\": \"DINHEIRO\", \"recebidoPor\": \"Ana\"}")
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.saldo").value(0.0));
    }

    @Test
    void producaoNaoPodeCadastrarPedido() throws Exception {
        long cliente = criarCliente("(85) 98888-0002");
        enviar(post("/pedidos"), BETO, corpoDoPedido(cliente)).andExpect(status().isUnprocessableEntity());
    }

    private long criarCliente(String telefone) throws Exception {
        String json = enviar(post("/clientes"), ANA, "{\"nome\": \"Joao\", \"telefone\": \"%s\"}".formatted(telefone))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private long criarPedido(long cliente) throws Exception {
        String json = enviar(post("/pedidos"), ANA, corpoDoPedido(cliente))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private String corpoDoPedido(long cliente) {
        return """
                {"clienteId": %d, "produtoId": 1, "quantidade": 30, "personalizacao": "Tema jardim",
                 "dataEntrega": "%s", "valorTotal": 350.00, "canal": "WHATSAPP"}
                """.formatted(cliente, LocalDate.now().plusDays(10));
    }

    private ResultActions enviar(MockHttpServletRequestBuilder requisicao, long usuario, String corpo)
            throws Exception {
        requisicao.header("X-Usuario-Id", usuario);
        if (corpo != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        return mvc.perform(requisicao);
    }
}
