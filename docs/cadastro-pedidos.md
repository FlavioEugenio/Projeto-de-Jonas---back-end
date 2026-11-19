# Cadastro de pedidos

Branch: `feature/cadastro-pedidos` · Responsável: Miguel Angelus

Hoje cada pedido fica num lugar diferente: no celular de um funcionário, num caderno ou numa planilha. Esta funcionalidade cria o **registro único do pedido**. É a base que as outras partes usam: alterações, materiais, pagamentos e produção.

## Endpoints

| Método | Caminho | O que faz |
|---|---|---|
| `POST` | `/clientes` | Cadastra um cliente. O telefone é único e aceita máscara: `(85) 98888-7777`. |
| `GET` | `/clientes?busca=` | Lista os clientes. Com números, busca pelo telefone; com letras, pelo nome. |
| `GET` | `/clientes/{id}` | Busca um cliente. |
| `POST` | `/pedidos` | Registra um pedido. Precisa do cabeçalho `X-Usuario-Id`. |
| `GET` | `/pedidos?status=&dataEntrega=&clienteId=` | Lista os pedidos pela data de entrega. Os filtros são opcionais. |
| `GET` | `/pedidos/{id}` | Ficha completa: dados, materiais, saldo e histórico. |

Enquanto o login não existe, o id de quem faz a operação vai no cabeçalho `X-Usuario-Id`. O domínio confere o perfil: só **atendimento** (id 1, Ana) e **dono** (id 3, Marcos) cadastram pedidos. A **produção** (id 2, Beto) recebe erro 422.

A classe `config/DadosIniciais` cria esses usuários, 3 produtos (Kit festa, Caneca personalizada e Topo de bolo) e a cliente Maria Souza, para dar para testar.

### Exemplo

```http
POST /pedidos
X-Usuario-Id: 1
Content-Type: application/json

{
  "clienteId": 1,
  "produtoId": 1,
  "quantidade": 30,
  "personalizacao": "Tema jardim, fita azul, nome Alice",
  "dataEntrega": "2026-12-05",
  "valorTotal": 350.00,
  "canal": "WHATSAPP"
}
```

A resposta é `201 Created`, com o pedido em status `RECEBIDO` e os materiais do produto a conferir.

Os erros voltam como `{ "status": 422, "mensagem": "...", "detalhes": [] }`:

| Código | Quando |
|---|---|
| 400 | Campo faltando ou com formato errado. |
| 404 | Cliente, produto, usuário ou pedido não encontrado. |
| 422 | Regra de negócio: perfil sem permissão, telefone repetido, data no passado. |

## Organização

Segue a arquitetura em camadas do README: `controller` recebe a requisição, `service` busca no banco, chama o domínio e salva, e `repository` acessa o banco. As regras continuam nas entidades: quem valida perfil, quantidade e data é o construtor de `Pedido`, não o service. Os DTOs ficam no pacote `dto`, usado pelo controller e pelo service.

Esta branch cria os repositórios de `Cliente`, `Produto`, `Pedido` e `Usuario`, além do `TratadorDeErros`. As outras funcionalidades podem usar esses arquivos depois do merge.

## Testes

`mvnw.cmd test` (Windows) ou `./mvnw test` roda o `CadastroPedidosTest`, que chama os endpoints com o banco H2.
