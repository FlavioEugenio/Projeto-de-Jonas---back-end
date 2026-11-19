# Fichário de Encomendas

Sistema de pedidos para uma pequena empresa de produtos personalizados sob encomenda.

Hoje os pedidos chegam por WhatsApp, Instagram e balcão e ficam espalhados em celulares, cadernos e planilhas. Isso causa três erros recorrentes:

- alterações que não chegam à produção;
- produção iniciada sem o material necessário;
- entregas feitas sem conferir o saldo a receber.

O sistema cria uma **fonte única sobre cada pedido**: dados, alterações, materiais, pagamentos, status e histórico.

A **Etapa 2** fez a modelagem do domínio e definiu a arquitetura. A **Etapa 3** implementou os endpoints, um conjunto por integrante, e organizou o código por módulo.

## Escopo da primeira versão

- O atendente registra o pedido feito pelo cliente no WhatsApp. Não há bot nem integração automática.
- Uma alteração feita pelo atendimento fica pendente até a produção confirmar ciência.
- A produção só começa com todos os materiais disponíveis.
- O saldo é calculado pelo sistema, a partir dos pagamentos, e nunca digitado.
- A entrega só fecha com o saldo completo recebido e com o nome de quem recebeu.
- Somente o dono cancela pedidos e corrige pagamentos, sempre com motivo registrado.
- Um limite de pedidos por dia avisa o atendente antes de prometer um prazo.

## Diagrama de classes

![Diagrama de classes do domínio](docs/diagrama-classes.svg)

Arquivos do diagrama:

- [`docs/diagrama-classes.mmd`](docs/diagrama-classes.mmd): fonte em Mermaid. Edite este arquivo quando o modelo mudar.
- [`docs/diagrama-classes.svg`](docs/diagrama-classes.svg) e [`docs/diagrama-classes.png`](docs/diagrama-classes.png): imagens geradas a partir da fonte.

### Entidades

| Entidade | Papel | Relacionamentos JPA |
|---|---|---|
| `Pedido` | Raiz do agregado. Concentra todas as regras do pedido. | `@ManyToOne` Cliente, Produto, Usuario (criadoPor). `@OneToMany` MaterialPedido, Pagamento, Alteracao, EventoPedido |
| `Cliente` | Quem encomenda. Identificado pelo telefone, que é único. | Referenciado por Pedido |
| `Produto` | Item do catálogo, com a lista padrão de materiais. | `@ManyToMany` Material (tabela `produto_material`) |
| `Material` | Insumo da produção. | Referenciado por Produto e MaterialPedido |
| `MaterialPedido` | Material de um pedido e sua situação: disponível ou falta comprar. | `@ManyToOne` Pedido, Material |
| `Pagamento` | Sinal, parcial ou saldo. Correções guardam o valor original. | `@ManyToOne` Pedido, Usuario (registradoPor, corrigidoPor) |
| `Alteracao` | Mudança de um campo, com ciência da produção. | `@ManyToOne` Pedido, Usuario (alteradoPor, cienciaPor) |
| `EventoPedido` | Linha imutável do histórico. | `@ManyToOne` Pedido, Usuario (autor) |
| `Usuario` | Funcionário com login e perfil. | Referenciado pelas entidades acima |
| `ConfiguracaoAgenda` | Limite de pedidos por dia de entrega. | Nenhum |

Enums: `StatusPedido`, `CanalOrigem`, `FormaPagamento` e `Perfil`.

### Encapsulamento

As entidades não têm setters públicos. Cada mudança de estado é um método com nome do negócio, que valida a regra antes de alterar o dado. Por exemplo, `alterarPersonalizacao`, `confirmarCiencia`, `registrarPagamento` e `registrarEntrega`.

- `Pagamento`, `Alteracao`, `MaterialPedido` e `EventoPedido` são criados e alterados somente por `Pedido`. Por isso seus construtores e modificadores são visíveis apenas dentro do pacote.
- As listas são devolvidas como somente leitura.
- `Pedido` tem `@Version`, para impedir que duas pessoas salvem versões diferentes do mesmo pedido ao mesmo tempo.
- Regras violadas lançam `RegraDeNegocioException`.

## Arquitetura

**Arquitetura em camadas** (Controller, Service, Repository), com **modelo de domínio rico**: as regras de negócio ficam nas entidades, e não espalhadas pelos services. O código é **organizado por módulo**: cada funcionalidade tem sua pasta, e as camadas ficam dentro dela.

### Justificativa

- **Necessidade do negócio.** O problema da empresa é de regras, e não de integração. Exemplos: só entregar com o saldo quitado, só produzir depois da ciência da alteração. Colocar essas regras nas entidades garante que valham em qualquer tela ou endpoint futuro, e permite testá-las sem banco nem servidor.
- **Complexidade do sistema.** O sistema é um CRUD com regras, usado por uma equipe de oito pessoas e com um único banco de dados. Arquiteturas como a hexagonal ou a Clean Architecture pedem portas, adaptadores e mapeamento entre modelos. Isso dobraria o número de classes sem resolver um problema real desta empresa.
- **Características da equipe.** A equipe está aprendendo Spring Boot e JPA. O padrão em camadas é o que o próprio Spring adota e o que mais aparece em documentação e exemplos, o que reduz a curva de aprendizado. Cada integrante entende onde cada código fica.
- **Evolução.** Se no futuro surgir uma integração, como um formulário de pedidos ou a API do WhatsApp, ela entra como um novo controller ou service. O domínio não muda.

### Organização do código

```
src/main/java/br/com/fichario
├── FicharioApplication.java        ponto de entrada do Spring Boot
├── config
│   ├── DadosIniciais.java          dados de exemplo ao subir a aplicação
│   └── exception                   TratadorDeErros: exceções viram JSON
├── internal                        um pacote por módulo do negócio
│   ├── cliente                     controller, dto, entity, mapper, repository, service
│   ├── pedido                      controller, dto, entity, mapper, repository, service
│   ├── alteracao                   controller, dto, mapper, repository, service
│   ├── producao                    controller, dto, mapper, service
│   ├── pagamento                   controller, dto, mapper, service
│   ├── material                    controller, dto, entity, mapper, repository, service
│   ├── produto                     entity, repository
│   ├── usuario                     entity, repository
│   └── agenda                      entity
└── shared
    ├── exception                   RegraDeNegocioException, RecursoNaoEncontradoException
    └── util                        Validacao, usada pelas entidades
```

Dentro de cada módulo:

- `controller`: endpoints REST;
- `dto`: dados de entrada (com Bean Validation) e de saída;
- `mapper`: converte a entidade para o DTO de saída;
- `service`: casos de uso e transações: busca no banco, chama o domínio e salva;
- `repository`: interfaces Spring Data JPA;
- `entity`: entidades JPA e regras de negócio.

`Pedido` é a raiz do agregado. `Pagamento`, `Alteracao`, `MaterialPedido` e `EventoPedido` ficam em `pedido/entity`, no mesmo pacote do `Pedido`, porque só ele pode criá-los e alterá-los. Por isso os módulos `alteracao` e `pagamento` não têm `entity`: eles trabalham sobre o `Pedido`.

Regras de dependência:

- `controller` usa `service`;
- `service` usa `repository`, `mapper` e as entidades, inclusive de outros módulos;
- as entidades não dependem de nenhuma outra camada.

As consultas que cruzam vários pedidos ficam no `service`, pois precisam do banco. É o caso da fila da produção e da lista de compras.

## Tecnologias

### Usadas no projeto

- **Java 21**: linguagem do projeto.
- **Spring Boot 3.3**: configura a aplicação e o banco automaticamente.
- **Spring Data JPA com Hibernate**: mapeia as classes Java para tabelas do banco por meio das anotações JPA (`@Entity`, `@ManyToOne` e outras).
- **H2 em memória**: banco de dados que roda dentro da própria aplicação e some quando ela é desligada. Serve para desenvolver e testar sem instalar nada. O esquema é criado a partir das entidades.
- **JUnit 5**: biblioteca para escrever e executar testes automáticos.
- **AssertJ**: biblioteca de verificações que deixa os testes legíveis. Por exemplo, `assertThat(pedido.getSaldo()).isEqualByComparingTo("225.00")`.
- **Spring Web (REST)**: controllers que expõem os pedidos em JSON.
- **Bean Validation**: validação dos dados que chegam nos endpoints, antes de chegar ao domínio.
- **Maven**: gerencia as dependências e executa o build e os testes. O Maven Wrapper (`mvnw`) dispensa a instalação.

### Planejadas para a aplicação web (próximas etapas)

- **Spring Security com BCrypt**: login individual, controle por perfil (atendimento, produção e dono) e senhas guardadas como hash.
- **PostgreSQL**: banco de dados de produção, no lugar do H2.
- **Front-end web (HTML, CSS e JavaScript)**: telas de pedidos, agenda e compras, seguindo o protótipo da V1. Funciona no celular e no computador, sem instalação.

As tecnologias planejadas ainda serão confirmadas pela equipe e podem mudar.

## Como executar

Pré-requisito: JDK 21 instalado, com `JAVA_HOME` apontando para ele. O Maven não precisa estar instalado, porque o projeto usa o Maven Wrapper.

Rodar os testes:

```bash
./mvnw test
```

No Windows (PowerShell ou CMD):

```bash
mvnw.cmd test
```

Os testes cobrem:

- `PedidoTest`: regras de negócio, sem banco.
- `MapeamentoJpaTest`: salva um pedido com todos os relacionamentos no H2 e lê de volta, conferindo o mapeamento JPA.
- Um teste por módulo (`CadastroPedidosTest`, `AlteracoesPedidoTest`, `StatusProducaoTest`, `ControlePagamentosTest` e `MateriaisComprasTest`): chamam os endpoints de verdade, com o H2 e os dados iniciais.

Subir a aplicação em `http://localhost:8080`. Os dados de exemplo são criados ao subir (usuários 1 = Ana, atendimento; 2 = Beto, produção; 3 = Marcos, dono). Nas operações, o id de quem faz vai no cabeçalho `X-Usuario-Id`:

```bash
./mvnw spring-boot:run
```

## Equipe

<!-- Preencha com os nomes dos integrantes -->
- Flavio Eugenio
- Eike Sales
- Miguel Angelus
- Gustavo Interamnense
- Igor Maia
