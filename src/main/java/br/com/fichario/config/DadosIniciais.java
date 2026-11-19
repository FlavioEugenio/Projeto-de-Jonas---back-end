package br.com.fichario.config;

import br.com.fichario.internal.cliente.entity.Cliente;
import br.com.fichario.internal.material.entity.Material;
import br.com.fichario.internal.produto.entity.Produto;
import br.com.fichario.internal.usuario.entity.Perfil;
import br.com.fichario.internal.usuario.entity.Usuario;
import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Popula o banco H2 ao subir a aplicacao, para testar os endpoints sem cadastrar tudo na mao.
 * Usuarios criados (use o id no cabecalho X-Usuario-Id):
 * 1 = Ana (atendimento), 2 = Beto (producao), 3 = Marcos (dono).
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    /** Placeholder ate o login com BCrypt existir. */
    private static final String SEM_SENHA = "login-nao-implementado";

    private final EntityManager em;

    public DadosIniciais(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Long usuarios = em.createQuery("select count(u) from Usuario u", Long.class).getSingleResult();
        if (usuarios > 0) {
            return;
        }

        em.persist(new Usuario("Ana", "ana", SEM_SENHA, Perfil.ATENDIMENTO));
        em.persist(new Usuario("Beto", "beto", SEM_SENHA, Perfil.PRODUCAO));
        em.persist(new Usuario("Marcos", "marcos", SEM_SENHA, Perfil.DONO));

        Material fita = salvar(new Material("Fita de cetim"));
        Material caixa = salvar(new Material("Caixa kraft"));
        Material caneca = salvar(new Material("Caneca branca"));
        Material papel = salvar(new Material("Papel fotografico"));

        Produto kit = new Produto("Kit festa");
        kit.adicionarMaterialPadrao(fita);
        kit.adicionarMaterialPadrao(caixa);
        Produto canecaPersonalizada = new Produto("Caneca personalizada");
        canecaPersonalizada.adicionarMaterialPadrao(caneca);
        Produto topo = new Produto("Topo de bolo");
        topo.adicionarMaterialPadrao(papel);
        em.persist(kit);
        em.persist(canecaPersonalizada);
        em.persist(topo);

        em.persist(new Cliente("Maria Souza", "(85) 99999-1142"));
    }

    private Material salvar(Material material) {
        em.persist(material);
        return material;
    }
}
