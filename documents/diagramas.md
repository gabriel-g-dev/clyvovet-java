# 📊 Diagramas e Arquitetura — ClyvoVet Java API

Modelagem das entidades, arquitetura macro e desenho do banco da aplicação Spring Boot da ClyvoVet.

---

## 🏗️ 1. Arquitetura macro

A aplicação tem **duas portas de entrada** sobre a mesma camada de domínio: o painel web em Thymeleaf, usado pela clínica, e a API REST com JWT, usada hoje pelo Swagger e pelo Insomnia e preparada para um cliente mobile. O aplicativo Mobile atual do ClyvoVet usa o Supabase e não consome esta API.

```mermaid
graph TD
    Navegador[Navegador - painel da clínica]
    App[Cliente da API<br/>Swagger / Insomnia / app futuro]

    Security[Spring Security<br/>chain web: sessão + formulário<br/>chain /api: JWT stateless]

    Web[WebController<br/>Thymeleaf]
    Rest[Controllers REST<br/>/api]

    Service[Camada de Service<br/>PetService · ConsultaService · VacinaService<br/>IntelligenceService · BaixaVacinaService<br/>RegistroService · PainelService]
    Repo[Repositories<br/>Spring Data JPA]
    Banco[(H2 / Oracle<br/>schema versionado pelo Flyway)]
    Cache[(Spring Cache)]

    Navegador -->|HTML| Security
    App -->|JSON + Bearer token| Security
    Security --> Web
    Security --> Rest
    Web --> Service
    Rest --> Service
    Rest -.-> Cache
    Service --> Repo
    Repo --> Banco
```

### Divisão de responsabilidades

| Camada | Responsabilidade |
| :--- | :--- |
| **security** | Autenticação (BCrypt na tabela `USUARIOS`), emissão e validação do JWT, e as regras de quem acessa o quê |
| **controller** | Recebe a requisição, valida o DTO e delega. Não contém regra de negócio |
| **service** | Onde vivem as regras: score preventivo, baixa de vacina com reforço, cadastro de pet, montagem do painel |
| **repository** | `JpaRepository` com query methods |
| **model** | Entidades JPA. Regras próprias do calendário vacinal ficam na entidade `Vacina` |
| **dto** | Contratos de entrada e saída + Bean Validation |
| **exception** | Exceções de domínio e o `@RestControllerAdvice` que traduz cada uma para o status HTTP correto |

---

## 🔐 2. Fluxo de autenticação e autorização

```mermaid
sequenceDiagram
    participant App as Cliente da API
    participant Auth as AuthRestController
    participant SS as AuthenticationManager
    participant DB as Tabela USUARIOS
    participant API as Endpoint protegido

    App->>Auth: POST /api/auth/register (nome, email, telefone, senha)
    Auth->>DB: grava USUARIOS (ROLE_TUTOR) + DONOS com o mesmo e-mail
    Auth-->>App: 201 com tutorId, sem senha

    App->>Auth: POST /api/auth/login (email, senha)
    Auth->>SS: authenticate(email, senha)
    SS->>DB: busca usuário pelo e-mail
    DB-->>SS: hash BCrypt + role
    SS-->>Auth: autenticado (ROLE_ADMIN ou ROLE_TUTOR)
    Auth-->>App: token JWT + role + validade

    App->>API: GET /api/pets (Authorization: Bearer token)
    API-->>App: 200 quando a role permite, 403 quando não
```

| Recurso | ROLE_ADMIN | ROLE_TUTOR |
| :--- | :---: | :---: |
| Pets e consultas | todos | **apenas os seus** |
| Calendário vacinal | CRUD + baixa | leitura dos seus pets |
| Funcionários | CRUD | somente leitura |
| Medicamentos e cadastro de tutores | CRUD | ❌ 403 |

### Propriedade do recurso

O e-mail é o elo entre a credencial e o cadastro do tutor: `USUARIOS.usuario_email` e
`DONOS.dono_email` guardam o mesmo valor e ambas as colunas são únicas. Com isso dá para
saber de quem é cada pet sem criar chave estrangeira entre as duas tabelas.

```mermaid
flowchart LR
    A[Requisição com token] --> B[SecurityConfig<br/>regra de rota]
    B -->|perfil não pode| X[403]
    B -->|perfil pode| C[EscopoUsuarioService]
    C -->|é ADMIN| D[acesso liberado]
    C -->|é TUTOR| E{e-mail do token<br/>== e-mail do dono do pet?}
    E -->|sim| D
    E -->|não| X
```

No cadastro e na edição de pet, o `tutorId` enviado pelo cliente só é respeitado para o ADMIN:
para o TUTOR o vínculo é sempre o próprio usuário, o que impede transferir um pet para outra pessoa.

---

## 🧬 3. Diagrama de classes

```mermaid
classDiagram
    direction LR

    class Usuario {
        +Long id
        +String email
        +String senha
        +String role
    }

    class Dono {
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
        +BigDecimal peso
        +Dono dono
        +List~Consulta~ consultas
        +List~Vacina~ vacinas
    }

    class Vacina {
        +Long id
        +String nome
        +LocalDate data
        +String status
        +Integer intervaloReforcoDias
        +Pet pet
        +estaAtrasada() boolean
        +venceEm(dias) boolean
    }

    class Consulta {
        +Long id
        +LocalDateTime data
        +String tipo
        +String descricao
        +BigDecimal valor
        +String status
        +Pet pet
        +Funcionario funcionario
    }

    class Funcionario {
        +Long id
        +String nome
        +String setor
        +String cargo
        +String email
        +String telefone
    }

    class Medicamento {
        +Long id
        +String nome
    }

    class ConsultaMedicamento {
        +Long id
        +String dosagem
        +Consulta consulta
        +Medicamento medicamento
    }

    Dono "1" --> "0..*" Pet : possui
    Pet "1" --> "0..*" Consulta : realiza
    Pet "1" --> "0..*" Vacina : recebe
    Funcionario "1" --> "0..*" Consulta : atende
    Consulta "1" --> "0..*" ConsultaMedicamento : prescreve
    Medicamento "1" --> "0..*" ConsultaMedicamento : consta em
```

> `Usuario` fica separada de `Dono` de propósito: nem todo usuário do sistema é tutor de um pet (o ADMIN da clínica, por exemplo), e nem todo tutor cadastrado precisa ter login.

---

## 🛢️ 4. Diagrama Entidade-Relacionamento (DER)

Modelo criado por `V1__create_tables.sql` e estendido por `V3__add_intervalo_reforco_vacina.sql`.

```mermaid
erDiagram
    USUARIOS {
        BIGINT usuario_id PK
        VARCHAR usuario_email UK
        VARCHAR usuario_senha
        VARCHAR usuario_role
    }

    DONOS {
        BIGINT dono_id PK
        VARCHAR dono_nome
        VARCHAR dono_email UK
        VARCHAR dono_telefone
    }

    FUNCIONARIOS {
        BIGINT funcionario_id PK
        VARCHAR funcionario_nome
        VARCHAR funcionario_setor
        VARCHAR funcionario_cargo
        VARCHAR funcionario_email UK
        VARCHAR funcionario_telefone
    }

    PETS {
        BIGINT pet_id PK
        VARCHAR pet_nome
        DATE pet_data_nascimento
        NUMERIC pet_peso
        VARCHAR pet_especie
        VARCHAR pet_raca
        BIGINT dono_id FK
    }

    VACINAS {
        BIGINT vacina_id PK
        VARCHAR vacina_nome
        DATE vacina_data
        VARCHAR vacina_status
        INTEGER vacina_intervalo_reforco_dias
        BIGINT pet_id FK
    }

    CONSULTAS {
        BIGINT consulta_id PK
        VARCHAR consulta_tipo
        NUMERIC consulta_valor
        VARCHAR consulta_descricao
        VARCHAR consulta_status
        TIMESTAMP consulta_data
        BIGINT pet_id FK
        BIGINT funcionario_id FK
    }

    MEDICAMENTOS {
        BIGINT medicamento_id PK
        VARCHAR medicamento_nome
    }

    CONSULTAS_MEDICAMENTOS {
        BIGINT consulta_medicamento_id PK
        BIGINT consulta_id FK
        BIGINT medicamento_id FK
        VARCHAR cm_dosagem
    }

    DONOS ||--o{ PETS : "possui"
    PETS ||--o{ VACINAS : "recebe"
    PETS ||--o{ CONSULTAS : "realiza"
    FUNCIONARIOS ||--o{ CONSULTAS : "atende"
    CONSULTAS ||--o{ CONSULTAS_MEDICAMENTOS : "prescreve"
    MEDICAMENTOS ||--o{ CONSULTAS_MEDICAMENTOS : "consta em"
```

### Convenções do modelo

- **Domínios de status** guardados em `VARCHAR(1)`:
  - `vacina_status`: `P` = pendente, `A` = aplicada
  - `consulta_status`: `A` = agendada, `R` = realizada, `C` = cancelada
- **Idade não é coluna.** O banco guarda `pet_data_nascimento`, porque idade envelhece; o texto ("3 anos") é derivado na resposta.
- **`vacina_intervalo_reforco_dias`** é o que permite o agendamento automático da próxima dose no fluxo de baixa de vacina.
- **Chaves e índices**: todas as FKs são `NOT NULL` e nomeadas (`PETS_DONOS_FK`, `VACINAS_PETS_FK`, `CONSULTAS_PETS_FK`, `CONSULTAS_FUNCIONARIOS_FK`), com índices em `PETS(dono_id)`, `VACINAS(pet_id)`, `VACINAS(vacina_status)` e `CONSULTAS(pet_id)`.
- **O schema não é gerado pelo Hibernate** (`ddl-auto=validate`): o Flyway cria e versiona, e o Hibernate apenas confere se as entidades batem.

---

## 🔄 5. Os dois fluxos de negócio

### Fluxo 1 — Inteligência preventiva

```mermaid
flowchart TD
    A[GET /api/pets/id/inteligencia-preventiva] --> B[IntelligenceService]
    B --> C{Tem vacinas?}
    C -->|Não| C1[-30 pontos]
    C -->|Sim| C2{Alguma atrasada?}
    C2 -->|Sim| C3[-15 por vacina + marca atraso]
    C2 -->|Vence em 8 dias| C4[Aviso preventivo]
    B --> D{Tem consultas?}
    D -->|Não| D1[-30 pontos]
    D -->|Última há mais de 1 ano| D2[-20 pontos]
    C1 & C3 & C4 & D1 & D2 --> E[classificarEngajamento]
    E --> F{score e atraso}
    F -->|score menor que 50| G[CRÍTICO]
    F -->|atraso ou score menor que 85| H[ATENÇÃO]
    F -->|sem atraso e score 85+| I[EXCELENTE]
```

Vacina atrasada **impede** o status EXCELENTE mesmo com score alto: um pet com pendência vencida não pode ser apresentado ao tutor como "em dia".

### Fluxo 2 — Baixa de vacina com reforço automático

```mermaid
sequenceDiagram
    participant Cli as Painel web / Cliente da API
    participant Ctrl as VacinaController
    participant Svc as BaixaVacinaService
    participant Repo as VacinaRepository

    Cli->>Ctrl: POST /api/vacinas/{id}/aplicar
    Ctrl->>Svc: aplicar(id, dataAplicacao)
    Svc->>Repo: findById(id)
    alt vacina já aplicada
        Svc-->>Cli: 409 Conflict
    else data no futuro
        Svc-->>Cli: 409 Conflict
    else
        Svc->>Repo: salva com status A
        Svc->>Repo: existe reforço já agendado?
        Svc->>Repo: cria dose pendente em data + intervaloReforcoDias
        Svc-->>Cli: 200 com dose aplicada + próxima dose
    end
```
