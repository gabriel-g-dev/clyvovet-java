# 🐾 ClyvoVet Java API

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3.0-blue?logo=swagger)](https://swagger.io/)
[![H2 Database](https://img.shields.io/badge/H2-In--Memory_Database-blue)](https://www.h2database.com/)

API RESTful desenvolvida em Java com Spring Boot para gestão da jornada contínua de saúde do pet — FIAP Challenge 2026 em parceria com a Clyvo VET.

---

## 👥 Integrantes
* **Gabriel Garcia** — RM563298
* **Andre Bellandi** — RM564662
* **Vitor Augusto** — RM564227

---

## 📋 Descrição do Projeto

A **ClyvoVet API** resolve o problema crônico de fragmentação na jornada de saúde animal. A maioria dos tutores só interage com clínicas veterinárias em situações emergenciais ou de urgência reativa. Nossa solução fornece uma infraestrutura digital robusta para organizar, lembrar e personalizar o cuidado preventivo e preventivo do pet de forma contínua e inteligente.

---

## 🛠️ Tecnologias e Recursos Utilizados

- **Java 17** & **Spring Boot 3.3.5**
- **Spring Data JPA** & **H2 in-memory Database** para persistência ágil
- **Spring Cache** para otimização de velocidade de leitura nos endpoints principais
- **Bean Validation (Jakarta Validation)** para consistência cadastral (`@NotBlank`, `@Email`, `@NotNull`)
- **OpenAPI 3.0 (springdoc-openapi)** para documentação de rotas interativas
- **Global Exception Handler** (`@RestControllerAdvice`) para respostas HTTP coerentes com a maturidade REST
- **DTOs (Data Transfer Objects)** para encapsular requisições e respostas de forma limpa, evitando loops de serialização recursiva
- **Spring Scheduler (@Scheduled)** & **Spring Transactions (@Transactional)** para simulação assíncrona diária de rotinas preventivas de saúde do Pet
- **ClyvoVet Care Intelligence Engine**: Algoritmo analítico de prevenção (Score 0-100 e recomendações inteligentes baseadas em regras de negócio)

---

## 📁 Estrutura de Pacotes do Projeto

```text
src/main/java/br/com/fiap/clyvovet/
├── controller/            # Controllers RESTful com suporte a DTO e Cache
├── dto/                   # Classes DTO (Request/Response) e Bean Validations
├── model/                 # Entidades JPA com mapeamentos relacionais e constraints
├── repository/            # JpaRepositories contendo custom Query Methods
├── exception/             # Manipulador global de exceções da aplicação
└── ClyvovetApplication.java # Classe principal contendo @EnableCaching
```

---

## 🔗 Rotas da API e Endpoints (32 rotas)

### 1. Tutores (`/api/tutores`)
| Método | Endpoint | Parâmetros | Descrição |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/tutores` | `Pageable` (page, size, sort) | Lista todos os tutores com paginação, ordenação e cache. |
| **GET** | `/api/tutores/{id}` | `@PathVariable Long id` | Busca um tutor específico por seu ID único (com cache). |
| **GET** | `/api/tutores/email/{email}` | `@PathVariable String email` | Filtra tutores pelo e-mail informado (com cache). |
| **GET** | `/api/tutores/{id}/pets` | `@PathVariable Long id` | Retorna o tutor juntamente com a lista de seus pets (com cache). |
| **POST** | `/api/tutores` | `@Valid @RequestBody TutorRequestDTO` | Cria um novo tutor no sistema (limpa caches). |
| **PUT** | `/api/tutores/{id}` | `@PathVariable id`, `@Valid @RequestBody TutorRequestDTO` | Atualiza dados cadastrais do tutor (limpa caches). |
| **DELETE** | `/api/tutores/{id}` | `@PathVariable Long id` | Exclui tutor em cascata (limpa caches). |

### 2. Pets (`/api/pets`)
| Método | Endpoint | Parâmetros | Descrição |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/pets` | `Pageable` (page, size, sort) | Lista pets com paginação, ordenação e cache. |
| **GET** | `/api/pets/{id}` | `@PathVariable Long id` | Busca um pet específico por ID (com cache). |
| **GET** | `/api/pets/{id}/inteligencia-preventiva` | `@PathVariable Long id` | **[INOVAÇÃO]** Retorna análise preventiva de saúde, calculando Score (0-100) e gerando recomendações inteligentes. |
| **GET** | `/api/pets/especie/{especie}` | `@PathVariable String especie` | Filtra todos os pets da espécie informada (com cache). |
| **GET** | `/api/pets/raca/{raca}` | `@PathVariable String raca` | Filtra todos os pets da raça informada (com cache). |
| **GET** | `/api/pets/tutor/{tutorId}` | `@PathVariable Long tutorId` | Lista todos os pets associados a um determinado tutor (com cache). |
| **POST** | `/api/pets` | `@Valid @RequestBody PetRequestDTO` | Cadastra um novo pet associado a um tutor (limpa caches). |
| **PUT** | `/api/pets/{id}` | `@PathVariable id`, `@Valid @RequestBody PetRequestDTO` | Atualiza informações sobre o pet (limpa caches). |
| **DELETE** | `/api/pets/{id}` | `@PathVariable Long id` | Exclui pet do sistema em cascata (limpa caches). |

### 3. Consultas (`/api/consultas`)
| Método | Endpoint | Parâmetros | Descrição |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/consultas` | `Pageable` (page, size, sort) | Lista consultas com paginação, ordenação e cache. |
| **GET** | `/api/consultas/{id}` | `@PathVariable Long id` | Busca consulta por ID (com cache). |
| **GET** | `/api/consultas/veterinario/{nome}` | `@PathVariable String nome` | Busca consultas cujo nome do veterinário contém o termo (case-insensitive). |
| **GET** | `/api/consultas/periodo` | `LocalDateTime inicio`, `LocalDateTime fim` | Filtra consultas ocorridas ou agendadas em uma faixa de datas. |
| **GET** | `/api/consultas/pet/{petId}` | `@PathVariable Long petId` | Retorna o histórico de consultas de um determinado pet. |
| **POST** | `/api/consultas` | `@Valid @RequestBody ConsultaRequestDTO` | Cadastra ou agenda uma consulta clínica para um pet (limpa caches). |
| **PUT** | `/api/consultas/{id}` | `@PathVariable id`, `@Valid @RequestBody ConsultaRequestDTO` | Atualiza uma consulta existente no histórico (limpa caches). |
| **DELETE** | `/api/consultas/{id}` | `@PathVariable Long id` | Exclui o registro da consulta (limpa caches). |

### 4. Vacinas (`/api/vacinas`)
| Método | Endpoint | Parâmetros | Descrição |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/vacinas` | `Pageable` (page, size, sort) | Lista vacinas com paginação, ordenação e cache. |
| **GET** | `/api/vacinas/{id}` | `@PathVariable Long id` | Busca vacina por ID (com cache). |
| **GET** | `/api/vacinas/pendentes` | — | Busca vacinas agendadas que ainda não foram aplicadas (aplicada = false). |
| **GET** | `/api/vacinas/nome/{nome}` | `@PathVariable String nome` | Busca vacinas pelo nome comercial ou científico (case-insensitive). |
| **GET** | `/api/vacinas/proximas` | `int dias` (default: 30) | Filtra próximas doses de vacinas programadas para os próximos N dias. |
| **GET** | `/api/vacinas/pet/{petId}` | `@PathVariable Long petId` | Lista o cartão vacinal (todas aplicadas e pendentes) de um pet. |
| **POST** | `/api/vacinas` | `@Valid @RequestBody VacinaRequestDTO` | Adiciona uma vacina aplicada ou agenda próxima dose para um pet (limpa caches). |
| **PUT** | `/api/vacinas/{id}` | `@PathVariable id`, `@Valid @RequestBody VacinaRequestDTO` | Atualiza o status cadastral ou de aplicação da vacina (limpa caches). |
| **DELETE** | `/api/vacinas/{id}` | `@PathVariable Long id` | Remove a vacina da ficha do animal (limpa caches). |

---

## 📦 Como Instalar, Executar e Testar Localmente

### Pré-requisitos
- **Java JDK 17** instalado e configurado na variável de ambiente `JAVA_HOME`.
- Gerenciador de portas liberado para a porta padrão `8080`.

### Executando a API
Como o Maven não está global na maioria dos laboratórios e ambientes de entrega, utilize o Maven Wrapper (`.\mvnw`) fornecido na raiz do projeto:

```powershell
# 1. Clone o repositório
git clone https://github.com/gabriel-g-dev/clyvovet-java.git
cd clyvovet-java

# 2. Compile e execute a aplicação Spring Boot
.\mvnw spring-boot:run
```

Uma vez inicializada com sucesso, o console indicará que o Tomcat está rodando na porta `8080`.

---

## 📖 Documentações de Apoio e Entrega (FIAP Challenge)

Disponibilizamos todos os artefatos exigidos pela rubrica na pasta raiz e subpastas:

1. **Testes e Validação de Endpoints**:
   - O arquivo JSON para importação direta no **Insomnia** contendo todas as 32 requisições prontas e simuladas com corpos HTTP adequados está localizado em: [documents/ClyvoVet_Java_API_Insomnia.json](file:///C:/Users/T-GAMER/clyvovet-java/documents/ClyvoVet_Java_API_Insomnia.json).
2. **Cronograma de Desenvolvimento**:
   - A distribuição detalhada de tarefas, responsabilidades e prazos do time está descrita em: [documents/cronograma.md](file:///C:/Users/T-GAMER/clyvovet-java/documents/cronograma.md).
3. **Diagramas UML, DER e Arquitetura**:
   - Os diagramas explicativos de classes, entidade-relacionamento (DER) e desenho macro da arquitetura estão em: [documents/diagramas.md](file:///C:/Users/T-GAMER/clyvovet-java/documents/diagramas.md).
4. **Documentação Swagger Interativa**:
   - Com o servidor rodando localmente, acesse a UI iterativa em: `http://localhost:8080/swagger-ui/index.html`
   - O endpoint da especificação JSON OpenAPI 3.0 está em: `http://localhost:8080/v3/api-docs`
5. **Console do Banco de Dados H2 (In-Memory)**:
   - Para inspecionar as tabelas geradas dinamicamente via JPA, acesse: `http://localhost:8080/h2-console`
   - URL JDBC: `jdbc:h2:mem:clyvovetdb`
   - User: `sa` / Password: *(deixar em branco)*
