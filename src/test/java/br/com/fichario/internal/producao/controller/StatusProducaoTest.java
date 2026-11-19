package br.com.fichario.internal.producao.controller;

import br.com.fichario.internal.produto.entity.Produto;
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
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StatusProducaoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private ProdutoRepository produtos;

    private Long ana;
    private Long beto;
    private Long marcos;

    @BeforeEach
    void carregarIds() {
        ana = usuarios.findByLogin("ana").orElseThrow().getId();
        beto = usuarios.findByLogin("beto").orElseThrow().getId();
        marcos = usuarios.findByLogin("marcos").orElseThrow().getId();
    }

    @Test
    void pedidoSemPendenciasVaiDaFilaAtePronto() throws Exception {
        long pedido = criarPedido("85966660001", produtoSemMaterial("Chaveiro teste producao"));

        mvc.perform(get("/producao/fila")).andExpect(jsonPath("$[*].id", hasItem((int) pedido)));

        mvc.perform(post("/pedidos/{id}/iniciar-producao", pedido).header("X-Usuario-Id", beto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PRODUCAO"));
        mvc.perform(post("/pedidos/{id}/pronto", pedido).header("X-Usuario-Id", beto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRONTO"));

        mvc.perform(get("/producao/fila")).andExpect(jsonPath("$[*].id", not(hasItem((int) pedido))));
    }

    @Test
    void naoIniciaComMaterialFaltando() throws Exception {
        long kitFesta = produtos.findAll().stream()
                .filter(p -> p.getNome().equals("Kit festa")).findFirst().orElseThrow().getId();
        long pedido = criarPedido("85966660002", kitFesta);

        mvc.perform(post("/pedidos/{id}/iniciar-producao", pedido).header("X-Usuario-Id", beto))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void somenteODonoCancela() throws Exception {
        long pedido = criarPedido("85966660003", produtoSemMaterial("Ima teste producao"));
        String corpo = "{\"motivo\": \"Cliente desistiu\"}";

        mvc.perform(post("/pedidos/{id}/cancelamento", pedido).header("X-Usuario-Id", ana)
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/pedidos/{id}/cancelamento", pedido).header("X-Usuario-Id", marcos)
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    private long produtoSemMaterial(String nome) {
        return produtos.save(new Produto(nome)).getId();
    }

    private long criarPedido(String telefone, long produto) throws Exception {
        String cliente = mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Cliente %s\", \"telefone\": \"%s\"}".formatted(telefone, telefone)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String pedido = mvc.perform(post("/pedidos").header("X-Usuario-Id", ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId": %d, "produtoId": %d, "quantidade": 10,
                                 "personalizacao": "Nome Alice", "dataEntrega": "%s",
                                 "valorTotal": 120.00, "canal": "INSTAGRAM"}
                                """.formatted(((Number) JsonPath.read(cliente, "$.id")).longValue(), produto,
                                LocalDate.now().plusDays(20))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(pedido, "$.id")).longValue();
    }
}
