# 🐾 ClyvoVet Java API

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-blue?logo=springsecurity)](https://spring.io/projects/spring-security)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-red?logo=flyway)](https://flywaydb.org/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Frontend-brightgreen?logo=thymeleaf)](https://www.thymeleaf.org/)

Aplicação web Spring Boot para o acompanhamento contínuo da saúde do pet — FIAP Challenge 2026 em parceria com a Clyvo VET.

O projeto entrega um **painel web em Thymeleaf** para a clínica e uma **API REST com JWT**, sobre a mesma camada de domínio. A API está pronta para ser consumida por um cliente externo, como um aplicativo mobile — veja a seção *Relação com o aplicativo Mobile*.

---

## 👥 Integrantes
* **Gabriel Garcia** — RM563298
* **Andre Bellandi** — RM564662
* **Vitor Augusto** — RM564227

---

## 📋 O problema

A maioria dos tutores só procura a clínica em situação de urgência. O ClyvoVet organiza a jornada preventiva do pet: cadastro do tutor e do animal, calendário vacinal, histórico de consultas e um motor que cruza esses dados para apontar o que está atrasado antes que vire emergência.

---

## 🛠️ Tecnologias

| Recurso | Uso no projeto |
| :--- | :--- |
| **Java 17 / Spring Boot 3.3.5** | Base da aplicação |
| **Spring Security + JWT (jjwt)** | Login do painel web (sessão) e da API REST (Bearer token) |
| **Flyway** | Versionamento do schema e carga inicial de dados |
| **Thymeleaf** | Páginas do painel web |
| **Spring Data JPA / H2** | Persistência (banco em memória para desenvolvimento) |
| **Bean Validation** | Validação dos dados na API e nos formulários |
| **springdoc-openapi** | Documentação interativa em Swagger UI |
| **Spring Cache / @Scheduled** | Cache de leitura e varredura preventiva periódica |

---

## 📦 Como executar

### Pré-requisitos
- **Java JDK 17** com `JAVA_HOME` configurado
- Porta **8080** livre

Não é preciso instalar Maven nem banco de dados: o projeto usa o Maven Wrapper e um H2 em memória.

```powershell
# 1. Clonar
git clone https://github.com/gabriel-g-dev/clyvovet-java.git
cd clyvovet-java

# 2. Compilar e rodar os testes
.\mvnw clean verify

# 3. Subir a aplicação
.\mvnw spring-boot:run
```

No Linux/macOS use `./mvnw` no lugar de `.\mvnw`.

A aplicação sobe em `http://localhost:8080`. O Flyway cria o schema e insere a massa de demonstração automaticamente a cada inicialização — o banco é em memória, então tudo é recriado do zero quando a aplicação reinicia.

---

## 🔐 Acesso e perfis

O login acontece contra a tabela `USUARIOS`, com senha guardada em **hash BCrypt**. Não há usuário fixo no código.

| Perfil | Usuário de demonstração | Senha | Papel |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@clyvovet.com.br` | `admin123` | Equipe da clínica |
| **TUTOR** | `gabriel@clyvovet.com.br` | `tutor123` | Dono do pet |

Tutores novos se cadastram sozinhos pela API, em `POST /api/auth/register`. O perfil nunca vem do cliente: quem se registra por ali é **sempre** `ROLE_TUTOR`.

### O que cada perfil pode fazer

| Recurso | ADMIN | TUTOR |
| :--- | :--- | :--- |
| Pets | CRUD de todos | **CRUD apenas dos seus** |
| Consultas | CRUD de todas, definindo o valor | **CRUD apenas das dos seus pets**, sem definir o valor |
| Calendário vacinal | CRUD + baixa de aplicação | Somente leitura, apenas dos seus pets |
| Funcionários | CRUD | Somente leitura (para escolher o veterinário) |
| Medicamentos | CRUD | Sem acesso (403) |
| Cadastro de tutores (`/api/tutores`) | CRUD | Sem acesso (403) — vê o próprio perfil em `/api/auth/me` |
| Inteligência preventiva | Qualquer pet | Apenas dos seus pets |

### Propriedade do recurso

O vínculo entre a credencial e o cadastro do tutor é o **e-mail**: a linha de `USUARIOS` e a de `DONOS` do mesmo tutor compartilham o mesmo e-mail, e as duas colunas são únicas. É isso que permite restringir os dados por usuário sem chave estrangeira nova.

Duas camadas protegem o dado, em [`SecurityConfig`](src/main/java/br/com/fiap/clyvovet/security/SecurityConfig.java) e [`EscopoUsuarioService`](src/main/java/br/com/fiap/clyvovet/security/EscopoUsuarioService.java):

1. **Regra de rota** — o que cada perfil pode chamar;
2. **Regra de propriedade** — de quem é o registro. O TUTOR recebe **403** ao tocar no pet de outra pessoa, e as listagens já saem filtradas. No cadastro e na edição, o tutor gravado no pet é sempre o próprio usuário logado: o `tutorId` enviado no corpo é ignorado para o perfil TUTOR, o que impede transferir um pet para outro tutor.
3. **Preço da consulta** — é decisão da clínica. O `valor` enviado pelo TUTOR é ignorado: a consulta que ele agenda nasce com valor `0`, e quando ele edita uma consulta o valor que já estava gravado é mantido. O ADMIN informa e altera o valor normalmente. No corpo da requisição, `valor` é opcional.

> As senhas acima são de demonstração. A chave de assinatura do JWT tem um valor padrão apenas para desenvolvimento e pode ser trocada pela variável de ambiente `CLYVOVET_JWT_SECRET`.

---

## 🖥️ Painel web (Thymeleaf)

| Rota | Descrição | Acesso |
| :--- | :--- | :--- |
| `/login` | Formulário de entrada | Público |
| `/dashboard` | Totais da clínica e calendário vacinal de cada pet | ADMIN e TUTOR |
| `/pets` | Lista de pets com as pendências de cada um | ADMIN e TUTOR |
| `/pets/{id}` | Ficha do pet: score preventivo, recomendações e baixa de vacina | ADMIN e TUTOR |
| `/pets/novo` | Formulário de cadastro com validação campo a campo | **Somente ADMIN** |
| `/acesso-negado` | Página exibida quando o perfil não tem permissão | Autenticado |

---

## 🔄 Os dois fluxos de negócio (não-CRUD)

### 1. Inteligência preventiva do pet

Não é uma leitura de tabela: o serviço cruza o calendário vacinal com o histórico de consultas e calcula um score.

```
GET /api/pets/{id}/inteligencia-preventiva
  → PetController
  → IntelligenceService.calcularPorPetId
       ├── avaliarCalendarioVacinal    (vacina atrasada: -15 | sem registro: -30)
       ├── avaliarHistoricoDeConsultas (check-up vencido há +1 ano: -20 | sem registro: -30)
       └── classificarEngajamento      → EXCELENTE / ATENÇÃO / CRÍTICO
  → HealthDashboardResponseDTO (score 0-100 + recomendações)
```

Regra que vale destacar: **uma vacina atrasada impede o status EXCELENTE**, mesmo com score alto — um pet com pendência vencida não pode ser apresentado ao tutor como "em dia".

No painel web o mesmo cálculo aparece em `/pets/{id}`.

### 2. Baixa de vacina com agendamento automático do reforço

Mais do que um `UPDATE` de status: a aplicação de uma dose **gera a próxima**.

```
POST /api/vacinas/{id}/aplicar     (ou o botão "Registrar aplicação" em /pets/{id})
  → VacinaController / WebController
  → BaixaVacinaService.aplicar
       ├── vacina já aplicada?  → 409 Conflict
       ├── data no futuro?      → 409 Conflict
       ├── marca como aplicada ('A')
       └── agendarReforco: cria nova vacina pendente em
           dataAplicacao + intervaloReforcoDias (sem duplicar dose já agendada)
  → BaixaVacinaResponseDTO (dose aplicada + próxima dose)
```

---

## 🔗 Endpoints da API

`POST /api/auth/login` é público; todas as demais rotas exigem `Authorization: Bearer <token>`.

### Autenticação (`/api/auth`) — rotas públicas
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| **POST** | `/api/auth/register` | Cadastra um tutor (cria o login e o cadastro na mesma transação). Perfil sempre `ROLE_TUTOR` |
| **POST** | `/api/auth/login` | Autentica e devolve o token JWT, o perfil e a validade |
| **GET** | `/api/auth/me` | Perfil do dono do token: e-mail, role e, para o tutor, `tutorId`, nome e telefone |

### Pets (`/api/pets`)
| Método | Endpoint | Acesso |
| :--- | :--- | :--- |
| **GET** | `/api/pets` (paginado) | ADMIN, TUTOR |
| **GET** | `/api/pets/{id}` | ADMIN, TUTOR |
| **GET** | `/api/pets/{id}/inteligencia-preventiva` — **fluxo 1** | ADMIN, TUTOR |
| **GET** | `/api/pets/especie/{especie}` · `/api/pets/raca/{raca}` · `/api/pets/tutor/{tutorId}` | ADMIN, TUTOR |
| **POST / PUT / DELETE** | `/api/pets` · `/api/pets/{id}` | ADMIN (qualquer) · TUTOR (só os seus) |

### Vacinas (`/api/vacinas`)
| Método | Endpoint | Acesso |
| :--- | :--- | :--- |
| **GET** | `/api/vacinas` (paginado) · `/api/vacinas/{id}` | ADMIN, TUTOR |
| **GET** | `/api/vacinas/pendentes` · `/api/vacinas/nome/{nome}` · `/api/vacinas/pet/{petId}` | ADMIN, TUTOR |
| **POST** | `/api/vacinas/{id}/aplicar` — **fluxo 2** | Somente ADMIN |
| **POST / PUT / DELETE** | `/api/vacinas` · `/api/vacinas/{id}` | Somente ADMIN |

### Tutores (`/api/tutores`)
| Método | Endpoint | Acesso |
| :--- | :--- | :--- |
| **GET** | `/api/tutores` (paginado) · `/api/tutores/{id}` · `/api/tutores/email/{email}` · `/api/tutores/{id}/pets` | Somente ADMIN |
| **POST / PUT / DELETE** | `/api/tutores` · `/api/tutores/{id}` | Somente ADMIN |

> O tutor não lista os outros tutores: o próprio cadastro dele vem em `GET /api/auth/me`.

### Consultas (`/api/consultas`)
| Método | Endpoint | Acesso |
| :--- | :--- | :--- |
| **GET** | `/api/consultas` (paginado) · `/api/consultas/{id}` · `/api/consultas/status/{status}` · `/api/consultas/periodo?inicio=&fim=` · `/api/consultas/pet/{petId}` | ADMIN, TUTOR |
| **POST / PUT / DELETE** | `/api/consultas` · `/api/consultas/{id}` | ADMIN (qualquer) · TUTOR (só as dos seus pets) |

### Funcionários e Medicamentos
| Método | Endpoint | Acesso |
| :--- | :--- | :--- |
| **GET** | `/api/funcionarios` · `/api/funcionarios/{id}` · `/api/funcionarios/setor/{setor}` | ADMIN, TUTOR |
| **POST / PUT / DELETE** | `/api/funcionarios` · `/api/funcionarios/{id}` | Somente ADMIN |
| **GET / POST / PUT / DELETE** | `/api/medicamentos` · `/api/medicamentos/{id}` | Somente ADMIN |

### Formato dos dados
Datas seguem o padrão **ISO-8601**, que é o formato padrão do Spring: `LocalDate` como `2023-05-15` e `LocalDateTime` como `2026-05-22T10:30:00`.

### Consumindo por um cliente mobile (preparado para integração futura)

Hoje nenhum aplicativo consome esta API (veja *Relação com o aplicativo Mobile*), mas ela já está preparada para isso. O **CORS** está liberado para as origens de desenvolvimento do Expo (`localhost`, `127.0.0.1`, a faixa `192.168.*.*` e `exp://`) em `/api/**`, com os métodos `GET`, `POST`, `PUT`, `DELETE` e o header `Authorization`. Um app empacotado é nativo e não passa por CORS; a configuração atende o Expo Web e o Expo Go.

Fluxo de uma sessão de um cliente mobile:

```
POST /api/auth/register   → cria a conta do tutor
POST /api/auth/login      → guarda o token devolvido
GET  /api/auth/me         → perfil do tutor (tutorId, nome, telefone)
GET  /api/pets            → já vem só com os pets desse tutor
```

Nas demais chamadas basta enviar `Authorization: Bearer <token>`. O token vale 8 horas; quando expira, a API responde **401** e o cliente deve refazer o login. Não há refresh token.

### Exemplo de uso

```bash
# 1. Cadastro de um tutor novo
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Victor","email":"victor@exemplo.com","telefone":"(11) 91234-5678","senha":"senha123"}'

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@clyvovet.com.br","senha":"admin123"}'

# 3. Fluxo 1 — análise preventiva
curl http://localhost:8080/api/pets/1/inteligencia-preventiva \
  -H "Authorization: Bearer SEU_TOKEN"

# 4. Fluxo 2 — baixa de vacina com reforço automático
curl -X POST http://localhost:8080/api/vacinas/2/aplicar \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" -d "{}"
```

---

## 📱 Relação com o aplicativo Mobile

O aplicativo Mobile do ClyvoVet ([clyvo-ve](https://github.com/Vitor-Augusto-olv/clyvo-ve)) é entregue na disciplina de Mobile e tem **arquitetura própria**: autenticação e dados de pets e vacinas ficam no **Supabase** (Auth + Postgres com Row Level Security), acessado pelo SDK `@supabase/supabase-js`.

**Hoje o aplicativo não consome esta API Java.** São dois projetos independentes do mesmo Challenge, cada um avaliado pela sua disciplina.

Esta API foi preparada para uma integração futura, caso a equipe decida por ela: cadastro público de tutor, login JWT, isolamento dos dados por tutor, CORS para Expo e datas em ISO-8601. Para conectar, o app precisaria trocar os services do Supabase por chamadas HTTP e adaptar alguns campos — por exemplo, o app guarda `idade` em anos e usa UUID como identificador, enquanto a API trabalha com `dataNascimento` e IDs numéricos.

---

## 🗄️ Banco de dados e Flyway

O schema **não** é gerado pelo Hibernate: `spring.jpa.hibernate.ddl-auto=validate`. Quem cria e versiona as tabelas é o Flyway, e o Hibernate apenas confere se as entidades batem com o schema — se divergirem, a aplicação falha ao subir.

| Migration | O que faz |
| :--- | :--- |
| `V1__create_tables.sql` | Cria `USUARIOS`, `DONOS`, `FUNCIONARIOS`, `PETS`, `VACINAS`, `MEDICAMENTOS`, `CONSULTAS`, `CONSULTAS_MEDICAMENTOS` e os índices |
| `V2__insert_mocks.sql` | Massa de demonstração: usuários com senha BCrypt, tutores, pets, vacinas e consultas |
| `V3__add_intervalo_reforco_vacina.sql` | Adiciona `vacina_intervalo_reforco_dias`, usada pelo fluxo de reforço |

As migrations ficam em [`src/main/resources/db/migration`](src/main/resources/db/migration). As datas da carga inicial são relativas a `CURRENT_DATE`, então a demonstração continua fazendo sentido com o tempo: sempre há uma vacina atrasada e uma próxima do vencimento.

**Console H2:** `http://localhost:8080/h2-console` · JDBC URL `jdbc:h2:mem:clyvovetdb` · usuário `sa` · senha em branco.

---

## 📖 Documentação e apoio

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Collection Insomnia:** [documents/ClyvoVet_Java_API_Insomnia.json](documents/ClyvoVet_Java_API_Insomnia.json)
- **Cronograma:** [documents/cronograma.md](documents/cronograma.md)
- **Diagramas (UML, DER, arquitetura):** [documents/diagramas.md](documents/diagramas.md)

---

## 🧪 Testes

```powershell
.\mvnw test
```

Cobrem o que sustenta a entrega: a separação de permissões e o isolamento entre tutores (`PerfilDeAcessoTest`), o cadastro público de tutor (`RegistroTutorTest`), a regra de preço da consulta por perfil (`ValorConsultaTest`) e as regras do fluxo de baixa de vacina (`BaixaVacinaServiceTest`).

---

## 📁 Estrutura

```text
src/main/java/br/com/fiap/clyvovet/
├── controller/   # REST (/api) e o painel web em Thymeleaf (WebController)
├── service/      # Regras de negócio: prevenção, baixa de vacina, cadastro, painel
├── security/     # SecurityConfig, JWT e integração com a tabela USUARIOS
├── model/        # Entidades JPA
├── repository/   # Spring Data JPA
├── dto/          # Contratos de entrada e saída + Bean Validation
└── exception/    # Exceções de domínio e handler global

src/main/resources/
├── db/migration/ # Migrations do Flyway
├── templates/    # Páginas Thymeleaf
├── static/css/   # Folha de estilo do painel
└── application.yml
```
