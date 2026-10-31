# Clientes API

API REST Spring Boot para cadastro de clientes. O endereço é preenchido consultando o ViaCEP a partir do CEP informado. O projeto aplica padrões de projeto e uma separação clara entre API, serviço, persistência e integração externa, com respostas de erro sem dados internos.

## Padrões de projeto aplicados

- **Dependency Injection**: o Spring injeta as dependências por construtor, mantendo as classes desacopladas e testáveis.
- **Repository**: `CustomerRepository` abstrai o acesso ao banco por meio do Spring Data JPA.
- **Service Layer / Facade**: `CustomerService` concentra os casos de uso, valida a unicidade do e-mail, consulta o endereço e traduz falhas de infraestrutura.
- **Adapter/Proxy**: `ViaCepClient` é uma porta declarativa para o serviço externo, implementada pelo proxy gerado pelo OpenFeign.
- **DTO**: `CustomerRequest` define o contrato de entrada e impede que o domínio seja usado diretamente para receber dados HTTP.
- **Chain of Responsibility (Spring MVC)**: filtros, desserialização, Bean Validation e `ApiExceptionHandler` participam da cadeia de processamento das requisições.

## Arquitetura

```text
api          -> endpoints REST, DTOs e erros HTTP
service      -> casos de uso e regras de negócio
domain       -> entidades Customer e Address
repository   -> persistência JPA
viacep       -> integração com serviço externo
```

## Requisitos

- Java 21+
- Maven 3.9+

## Executar

```bash
mvn spring-boot:run
```

Executar testes:

```bash
mvn test
```

A suíte combina testes unitários da camada de serviço e testes de integração HTTP com `MockMvc`, banco H2 e cliente ViaCEP simulado, sem depender da rede durante os testes.

O banco padrão é H2 em memória. Para PostgreSQL, substitua `spring.datasource.*` por variáveis de ambiente em uma configuração externa.

## Exemplos

```bash
curl -X POST http://localhost:8080/api/customers \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ana Silva","email":"ana@example.com","cep":"01001-000"}'
curl http://localhost:8080/api/customers
```

Endpoints: `GET /api/customers`, `GET /api/customers/{id}`, `POST`, `PUT /api/customers/{id}` e `DELETE /api/customers/{id}`.

## Critérios atendidos

- API REST completa com operações de cadastro, consulta, atualização e exclusão.
- Validação de nome, e-mail e CEP com respostas HTTP semânticas.
- Integração externa isolada por cliente declarativo.
- Persistência relacional com H2 para execução local e PostgreSQL disponível para produção.
- Testes de sucesso, validação, recurso inexistente, CEP inválido e indisponibilidade do provedor.
