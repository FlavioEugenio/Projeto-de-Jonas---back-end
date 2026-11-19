# Fichário de Encomendas

Sistema de pedidos para uma pequena empresa de produtos personalizados sob encomenda, desenvolvido com Java e Spring Boot.

## Sobre o projeto

Hoje os pedidos da empresa chegam por WhatsApp, Instagram e balcão e ficam espalhados em celulares, cadernos e planilhas. Com isso, alterações não chegam à produção, a produção começa sem o material necessário e produtos são entregues sem conferir o pagamento.

O Fichário de Encomendas centraliza cada pedido em um só lugar: dados do cliente, alterações, materiais, pagamentos e andamento da produção.

## Funcionalidades

- Cadastro e consulta de clientes.
- Registro e consulta de pedidos.
- Alterações do pedido, que só valem depois que a produção confirma que viu.
- Fila e andamento da produção.
- Registro de pagamentos e entrega, que só é feita com o saldo quitado.
- Controle dos materiais de cada pedido e lista do que falta comprar.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Web
- Bean Validation
- H2 (banco em memória)
- Lombok
- Maven

## Arquitetura

O projeto adota uma **arquitetura monolítica em camadas, organizada por módulo (feature)**. A aplicação roda como um único serviço Spring Boot, e o código de cada área do negócio fica agrupado no seu módulo: clientes, pedidos, alterações, produção, pagamentos e materiais.

### Justificativa

Tudo na empresa gira em torno do pedido: ele pertence a um cliente e recebe alterações, materiais e pagamentos. Manter isso em uma única aplicação garante dados consistentes e simplifica a comunicação entre as partes.

O sistema é um cadastro com regras, usado por uma equipe pequena e com um só banco de dados. O monólito em camadas separa bem as responsabilidades sem o custo de dividir o sistema em vários serviços. A organização por módulo ajuda a equipe, que está aprendendo Spring Boot, a encontrar o código de cada funcionalidade.

As regras de negócio ficam nas entidades, principalmente no `Pedido`. Por exemplo: só entregar com o saldo quitado e só produzir com os materiais disponíveis. Assim as regras valem em qualquer endpoint.

### Organização do código

```text
br.com.fichario
├── FicharioApplication.java
├── config                  # dados de exemplo e tratamento de erros
├── internal
│   ├── cliente
│   │   ├── controller      # endpoints HTTP
│   │   ├── service         # casos de uso
│   │   ├── repository      # acesso a dados com Spring Data JPA
│   │   ├── dto             # dados de entrada e saída da API
│   │   ├── mapper          # converte entidade em DTO
│   │   └── entity          # entidades JPA e regras de negócio
│   ├── pedido
│   ├── alteracao
│   ├── producao
│   ├── pagamento
│   ├── material
│   ├── produto
│   └── usuario
└── shared                  # exceções e validações usadas por todos
```

Os módulos menores têm só os pacotes de que precisam. Os controllers recebem as requisições e chamam os services. Os services buscam os dados nos repositories e chamam as regras das entidades. Os DTOs definem o que a API recebe e devolve, sem expor as entidades.

## Endpoints

Enquanto não há login, quem faz a operação é informado no cabeçalho `X-Usuario-Id`: 1 = Ana (atendimento), 2 = Beto (produção), 3 = Marcos (dono).

| Módulo | Endpoints |
|---|---|
| Cliente | `POST /clientes`, `GET /clientes`, `GET /clientes/{id}` |
| Pedido | `POST /pedidos`, `GET /pedidos`, `GET /pedidos/{id}` |
| Alteração | `PUT /pedidos/{id}/personalizacao`, `POST /pedidos/{id}/ciencia` |
| Produção | `GET /producao/fila`, `POST /pedidos/{id}/iniciar-producao`, `POST /pedidos/{id}/pronto` |
| Pagamento | `POST /pedidos/{id}/pagamentos`, `POST /pedidos/{id}/entrega` |
| Material | `GET /materiais`, `GET /compras`, `PUT /pedidos/{id}/materiais/{materialId}/disponivel` |

## Como executar

Pré-requisito: JDK 21 instalado. O Maven não precisa ser instalado.

```bash
./mvnw spring-boot:run      # sobe a aplicação em http://localhost:8080
./mvnw test                 # roda os testes
```

No Windows (CMD ou PowerShell), use `mvnw.cmd` no lugar de `./mvnw`.

Ao subir, a aplicação cria dados de exemplo: os 3 usuários acima, o produto "Kit festa" e a cliente Maria Souza.

## Modelo de dados

O diagrama abaixo representa as entidades do sistema e seus relacionamentos.

![Diagrama de classes](docs/diagrama-classes.svg)

## Equipe

- Flavio Eugenio
- Eike Sales
- Miguel Angelus
- Gustavo Interamnense
- Igor Maia

## Status do projeto

Em desenvolvimento.
