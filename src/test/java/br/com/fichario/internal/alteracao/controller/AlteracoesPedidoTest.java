package br.com.fichario.internal.alteracao.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AlteracoesPedidoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private ProdutoRepository produtos;

    private Long ana;
    private Long beto;

    @BeforeEach
    void carregarIds() {
        ana = usuarios.findByLogin("ana").orElseThrow().getId();
        beto = usuarios.findByLogin("beto").orElseThrow().getId();
    }

    @Test
    void alteracaoFicaPendenteAteAProducaoConfirmarCiencia() throws Exception {
        long pedido = criarPedido("85977770001");

        mvc.perform(put("/pedidos/{id}/personalizacao", pedido).header("X-Usuario-Id", ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\": \"Tema jardim, fita rosa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalizacao").value("Tema jardim, fita rosa"))
                .andExpect(jsonPath("$.alteracoesPendentes").value(true));

        mvc.perform(get("/alteracoes/pendentes"))
                .andExpect(jsonPath("$[*].id", hasItem((int) pedido)));

        mvc.perform(get("/pedidos/{id}/alteracoes", pedido))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].valorAnterior").value("Tema jardim, fita azul"))
                .andExpect(jsonPath("$[0].pendente").value(true));

        mvc.perform(post("/pedidos/{id}/ciencia", pedido).header("X-Usuario-Id", beto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alteracoesPendentes").value(false));
    }

    @Test
    void atendimentoNaoConfirmaCienciaNoLugarDaProducao() throws Exception {
        long pedido = criarPedido("85977770002");
        mvc.perform(put("/pedidos/{id}/quantidade", pedido).header("X-Usuario-Id", ana)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\": 40}"))
                .andExpect(status().isOk());

        mvc.perform(post("/pedidos/{id}/ciencia", pedido).header("X-Usuario-Id", ana))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoAceitaDataNoPassado() throws Exception {
        long pedido = criarPedido("85977770003");

        mvc.perform(put("/pedidos/{id}/data-entrega", pedido).header("X-Usuario-Id", ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataEntrega\": \"%s\"}".formatted(LocalDate.now().minusDays(1))))
                .andExpect(status().isUnprocessableEntity());
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
                                {"clienteId": %d, "produtoId": %d, "quantidade": 30,
                                 "personalizacao": "Tema jardim, fita azul", "dataEntrega": "%s",
                                 "valorTotal": 350.00, "canal": "WHATSAPP"}
                                """.formatted(((Number) JsonPath.read(cliente, "$.id")).longValue(), kitFesta,
                                LocalDate.now().plusDays(15))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(pedido, "$.id")).longValue();
    }
}
