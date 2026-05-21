# 📊 Diagramas e Arquitetura do Sistema — ClyvoVet Java API

Este documento apresenta a modelagem de entidades, diagramas e a arquitetura macro adotada para o desenvolvimento da API RESTful Java da ClyvoVet.

---

## 🏗️ 1. Arquitetura Macro da Aplicação

A API ClyvoVet foi projetada seguindo os padrões arquiteturais clássicos do ecossistema **Spring Boot**, priorizando o baixo acoplamento e a alta coesão das classes.

```mermaid
graph TD
    Client[Cliente HTTP / Swagger / Insomnia]
    Controller[1. Camada de Controller / REST Endpoints]
    DTO[2. Camada de DTOs / Validação e Apresentação]
    Service[3. Camada JPA / H2 In-Memory SGBD]
    Cache[(4. Mecanismo de Cache / Spring Cache)]

    Client <-->|JSON Request/Response| Controller
    Controller -.->|Validação com Bean Validation| DTO
    Controller <-->|Spring Data JPA / Query Methods| Service
    Controller <-->|Consulta Otimizada| Cache
```

### Divisão de Responsabilidades:
1. **Model**: Representação fiel do banco de dados relacional com anotações JPA.
2. **DTO (Data Transfer Object)**: Define a interface de comunicação de entrada (`RequestDTO`) e de saída (`ResponseDTO`). Protege o domínio interno e evita referências circulares em relacionamentos bidirecionais.
3. **Repository**: Extensões do `JpaRepository` contendo Query Methods personalizados.
4. **Controller**: Manipulação das requisições REST, conversão de DTOs, paginação/ordenação e suporte ao cache.
5. **Exception Handler**: Centralizador de erros do Spring Validation e RuntimeExceptions, garantindo o retorno dos status HTTP adequados (ex: 400 Bad Request, 404 Not Found).

---

## 🧬 2. Diagrama de Classes das Entidades

O diagrama abaixo descreve a estrutura de herança, tipos de dados e os atributos das classes de domínio que compõem o escopo de saúde preventiva da ClyvoVet.

```mermaid
classDiagram
    direction LR
    class Tutor {
        +Long id
        +String nome
        +String email
        +String telefone
        +List~Pet~ pets
    }

    class Pet {
        +Long id
        +String nome
        +String especie
        +String raca
        +LocalDate dataNascimento
        +Tutor tutor
        +List~Consulta~ consultas
        +List~Vacina~ vacinas
    }

    class Consulta {
        +Long id
        +LocalDateTime data
        +String descricao
        +String veterinario
        +String observacoes
        +Pet pet
    }

    class Vacina {
        +Long id
        +String nome
        +LocalDate dataAplicacao
        +LocalDate proximaDose
        +boolean aplicada
        +Pet pet
    }

    Tutor "1" --> "0..*" Pet : possui >
    Pet "1" --> "0..*" Consulta : realiza >
    Pet "1" --> "0..*" Vacina : recebe >
```

---

## 🛢️ 3. Diagrama Entidade-Relacionamento (DER) / MER

Modelo relacional lógico indicando chaves primárias, chaves estrangeiras e a cardinalidade dos relacionamentos.

```mermaid
erDiagram
    TB_TUTOR {
        Long id PK
        String nome
        String email
        String telefone
    }

    TB_PET {
        Long id PK
        String nome
        String especie
        String raca
        LocalDate data_nascimento
        Long tutor_id FK
    }

    TB_CONSULTA {
        Long id PK
        LocalDateTime data
        String descricao
        String veterinario
        String observacoes
        Long pet_id FK
    }

    TB_VACINA {
        Long id PK
        String nome
        LocalDate data_aplicacao
        LocalDate proxima_dose
        Boolean aplicada
        Long pet_id FK
    }

    TB_TUTOR ||--o{ TB_PET : "1 para N"
    TB_PET ||--o{ TB_CONSULTA : "1 para N"
    TB_PET ||--o{ TB_VACINA : "1 para N"
```

### Explicação das Constraints e Relacionamentos:

1. **TB_TUTOR para TB_PET (1:N)**:
   - **Constraint**: `tutor_id` na tabela `TB_PET` é uma Chave Estrangeira (`FOREIGN KEY`) obrigatória (`NOT NULL`) referenciando `TB_TUTOR(id)`.
   - **Comportamento**: Cascade do tipo `ALL` habilitado na entidade JPA. Se um Tutor for removido do sistema, todos os seus Pets vinculados serão removidos em cascata para evitar órfãos.

2. **TB_PET para TB_CONSULTA (1:N)**:
   - **Constraint**: `pet_id` na tabela `TB_CONSULTA` é uma `FOREIGN KEY` obrigatória referenciando `TB_PET(id)`.
   - **Comportamento**: A deleção em cascata também é configurada. Caso o Pet seja excluído de forma longitudinal, seu histórico de consultas é apagado automaticamente.

3. **TB_PET para TB_VACINA (1:N)**:
   - **Constraint**: `pet_id` na tabela `TB_VACINA` é uma `FOREIGN KEY` obrigatória referenciando `TB_PET(id)`.
   - **Comportamento**: Permite rastrear todo o calendário de vacinas aplicadas e futuras de um animal específico.
