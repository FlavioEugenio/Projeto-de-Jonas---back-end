package br.com.fichario.internal.material.controller;

import br.com.fichario.internal.material.repository.MaterialRepository;
import br.com.fichario.internal.produto.repository.ProdutoRepository;
import br.com.fichario.internal.usuario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MateriaisComprasTest {

    /** Pedidos deste material na lista de compras. */
    private static final String PEDIDOS_DA_FITA = "$[?(@.material == 'Fita de cetim')].pedidos[*]";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private ProdutoRepository produtos;

    @Autowired
    private MaterialRepository materiais;

    private Long ana;
    private Long beto;
    private Long fita;

    @BeforeEach
    void carregarIds() {
        ana = usuarios.findByLogin("ana").orElseThrow().getId();
        beto = usuarios.findByLogin("beto").orElseThrow().getId();
        fita = materiais.findAll().stream()
                .filter(m -> m.getNome().equals("Fita de cetim")).findFirst().orElseThrow().getId();
    }

    @Test
    void cadastraMaterialENaoAceitaNomeRepetido() throws Exception {
        String corpo = "{\"nome\": \"Papel vegetal\"}";
        mvc.perform(post("/materiais").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());
        mvc.perform(post("/materiais").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/materiais")).andExpect(jsonPath("$[*].nome", hasItem("Papel vegetal")));
    }

    @Test
    void materialSaiDaListaDeComprasQuandoAProducaoMarcaDisponivel() throws Exception {
        long pedido = criarPedido("85944440001");

        mvc.perform(get("/compras")).andExpect(jsonPath(PEDIDOS_DA_FITA, hasItem((int) pedido)));

        mvc.perform(put("/pedidos/{id}/materiais/{m}/disponivel", pedido, fita).header("X-Usuario-Id", beto))
                .andExpect(status().isOk());

        mvc.perform(get("/compras")).andExpect(jsonPath(PEDIDOS_DA_FITA, not(hasItem((int) pedido))));
    }

    @Test
    void somenteAProducaoMarcaMaterialDisponivel() throws Exception {
        long pedido = criarPedido("85944440002");

        mvc.perform(put("/pedidos/{id}/materiais/{m}/disponivel", pedido, fita).header("X-Usuario-Id", ana))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void materialIncluidoPeloAtendimentoPrecisaDaCienciaDaProducao() throws Exception {
        long pedido = criarPedido("85944440003");
        String novo = mvc.perform(post("/materiais").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Glitter dourado\"}"))
                .andReturn().getResponse().getContentAsString();
        long glitter = ((Number) JsonPath.read(novo, "$.id")).longValue();

        mvc.perform(post("/pedidos/{id}/materiais", pedido).header("X-Usuario-Id", ana)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"materialId\": %d}".formatted(glitter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materiais", hasSize(3)))
                .andExpect(jsonPath("$.alteracoesPendentes").value(true));
    }

    private long criarPedido(String telefone) throws Exception {
        String cliente = mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Cliente %s\", \"telefone\": \"%s\"}".formatted(telefone, telefone)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long kitFesta = produtos.findAll().stream()
                .filter(p -> p.getNome().equals("Kit festa")).findFirst().orElseThrow().getId();
        String pedido = mvc.perform(post("/pedidos").header("X-Usuario-Id", ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId": %d, "produtoId": %d, "quantidade": 20,
                                 "personalizacao": "Tema fundo do mar", "dataEntrega": "%s",
                                 "valorTotal": 280.00, "canal": "WHATSAPP"}
                                """.formatted(((Number) JsonPath.read(cliente, "$.id")).longValue(), kitFesta,
                                LocalDate.now().plusDays(30))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(pedido, "$.id")).longValue();
    }
}
