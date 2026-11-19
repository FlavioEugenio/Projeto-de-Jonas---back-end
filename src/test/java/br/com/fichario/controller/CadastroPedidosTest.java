package br.com.fichario.controller;

import br.com.fichario.repository.ProdutoRepository;
import br.com.fichario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Chama os endpoints de verdade, com o banco H2 e os dados iniciais. Cada teste usa um telefone proprio. */
@SpringBootTest
@AutoConfigureMockMvc
class CadastroPedidosTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private ProdutoRepository produtos;

    private Long ana;
    private Long beto;
    private Long kitFesta;

    @BeforeEach
    void carregarIds() {
        ana = usuarios.findByLogin("ana").orElseThrow().getId();
        beto = usuarios.findByLogin("beto").orElseThrow().getId();
        kitFesta = produtos.findAll().stream()
                .filter(p -> p.getNome().equals("Kit festa")).findFirst().orElseThrow().getId();
    }

    @Test
    void cadastraClienteEPedidoEConsultaPeloId() throws Exception {
        long cliente = cadastrarCliente("Joao Lima", "(85) 98888-0001");

        String resposta = cadastrarPedido(ana, cliente)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.cliente.telefone").value("85988880001"))
                .andExpect(jsonPath("$.materiais", hasSize(2)))
                .andReturn().getResponse().getContentAsString();
        long pedido = ((Number) JsonPath.read(resposta, "$.id")).longValue();

        mvc.perform(get("/pedidos/{id}", pedido))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produto").value("Kit festa"))
                .andExpect(jsonPath("$.criadoPor").value("Ana"));

        mvc.perform(get("/pedidos").param("clienteId", String.valueOf(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void producaoNaoPodeCadastrarPedido() throws Exception {
        long cliente = cadastrarCliente("Carla Dias", "85988880002");

        cadastrarPedido(beto, cliente).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoAceitaTelefoneDuplicado() throws Exception {
        cadastrarCliente("Pedro Alves", "85988880003");

        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Outro Pedro", "telefone": "(85) 98888-0003"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void recusaPedidoComCamposFaltando() throws Exception {
        mvc.perform(post("/pedidos").header("X-Usuario-Id", ana).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasSize(7)));
    }

    private long cadastrarCliente(String nome, String telefone) throws Exception {
        String resposta = mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "%s", "telefone": "%s"}
                                """.formatted(nome, telefone)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private ResultActions cadastrarPedido(Long usuario, long cliente) throws Exception {
        return mvc.perform(post("/pedidos").header("X-Usuario-Id", usuario)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "clienteId": %d,
                          "produtoId": %d,
                          "quantidade": 30,
                          "personalizacao": "Tema jardim, fita azul",
                          "dataEntrega": "%s",
                          "valorTotal": 350.00,
                          "canal": "WHATSAPP"
                        }
                        """.formatted(cliente, kitFesta, LocalDate.now().plusDays(10))));
    }
}
