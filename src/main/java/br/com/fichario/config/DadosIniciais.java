package br.com.fichario.config;

import br.com.fichario.internal.cliente.entity.Cliente;
import br.com.fichario.internal.material.entity.Material;
import br.com.fichario.internal.produto.entity.Produto;
import br.com.fichario.internal.usuario.entity.Perfil;
import br.com.fichario.internal.usuario.entity.Usuario;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DadosIniciais implements CommandLineRunner {

    private final EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        em.persist(new Usuario("Ana", Perfil.ATENDIMENTO));
        em.persist(new Usuario("Beto", Perfil.PRODUCAO));
        em.persist(new Usuario("Marcos", Perfil.DONO));

        Material fita = new Material("Fita de cetim");
        Material caixa = new Material("Caixa kraft");
        em.persist(fita);
        em.persist(caixa);

        Produto kit = new Produto("Kit festa");
        kit.adicionarMaterialPadrao(fita);
        kit.adicionarMaterialPadrao(caixa);
        em.persist(kit);

        em.persist(new Cliente("Maria Souza", "(85) 99999-1142"));
    }
}
