# ClyvoVet

O ClyvoVet centraliza o acompanhamento preventivo de pets para clínicas veterinárias e tutores. O painel web e a API REST reúnem cadastro, histórico de consultas e calendário vacinal, com acesso por perfil. Projeto Java Advanced do Challenge FIAP 2026.

## Problema e proposta

Registros dispersos dificultam a consulta do histórico e o acompanhamento de vacinas pendentes. O ClyvoVet reúne essas informações na ficha do pet e aproxima a pendência da ação: a clínica localiza uma dose vencida ou próxima, registra a aplicação e confere o reforço agendado automaticamente. O tutor acompanha somente os próprios animais.

O indicador preventivo usa regras sobre o histórico disponível para destacar situações que merecem acompanhamento. Ele não realiza diagnóstico nem define protocolos vacinais; ausência de registros não comprova que o pet esteja em dia.

## Acesso para avaliação

**Sistema online:** [clyvovet-java.onrender.com](https://clyvovet-java.onrender.com).

| Perfil | E-mail | Senha |
| :--- | :--- | :--- |
| ADMIN | `admin@clyvovet.com.br` | `admin123` |
| TUTOR | `gabriel@clyvovet.com.br` | `tutor123` |

São contas públicas de demonstração com dados exclusivamente fictícios. Não é necessário cadastrar usuário, confirmar conta ou informar um e-mail real. Use o ADMIN para os fluxos da clínica e o TUTOR para conferir o isolamento de dados.

O perfil `demo` usa H2 em memória: reiniciar o processo restaura a carga fictícia e apaga as alterações da sessão de demonstração. As alterações locais precisam ser publicadas para atualizar a instância do Render; depois, confira a disponibilidade e os fluxos conforme [Deploy](documents/deploy.md).

## Funcionalidades

- Login, logout, dashboard e navegação conforme o perfil.
- Busca de pets por nome, espécie, raça ou tutor; ficha com consultas, vacinas e recomendações.
- Cadastro e edição de pets e agendamento de vacinas no painel pelo ADMIN.
- Aplicação de vacina com reforço automático, operação transacional e prevenção de reaplicação.
- API para pets, tutores, consultas, vacinas, funcionários e medicamentos, com filtros e paginação nas listagens principais.
- Cadastro opcional de tutor pela API, validação de entradas e respostas de erro.

As consultas são gerenciadas pela API e exibidas na ficha web. A varredura periódica registra pendências no log; não há envio de e-mail, SMS ou push. O profissional define o intervalo de reforço de cada vacina; os valores iniciais são exemplos fictícios.

## Tecnologias e arquitetura

Java 17, Spring Boot 3.5.16, Spring MVC, Thymeleaf, Spring Security, Spring Data JPA, Bean Validation, H2, Flyway, jjwt e springdoc-openapi. Os testes usam JUnit, MockMvc e Spring Security Test; a regressão de navegador usa Playwright e Chrome. Docker, GitHub Actions e Render compõem os artefatos de operação.

```text
Navegador → sessão + CSRF → WebController → services
Cliente REST → JWT → controllers /api → services
services → repositories JPA → H2, com migrations Flyway
```

Os DTOs delimitam os contratos HTTP. Os services concentram os fluxos de negócio, reutilizados pelo painel e pela API; os cadastros auxiliares simples acessam repositories nos controllers. Thymeleaf mantém a interface e os formulários no mesmo processo Java. O Flyway versiona o schema e o Hibernate confere sua compatibilidade com `ddl-auto=validate`.

A aplicação de vacina bloqueia a dose para escrita e grava a aplicação e o reforço na mesma transação. Se já existe reforço pendente do mesmo pet, nome e data, ele é reutilizado. Reaplicação retorna `409`; data futura de aplicação retorna `400`. As alterações relevantes invalidam o cache do indicador preventivo, cuja chave também considera o dia do cálculo. Os [diagramas](documents/diagramas.md) apresentam classes, banco e fluxos.

## Perfis e segurança

| Operação | ADMIN | TUTOR |
| :--- | :--- | :--- |
| Pets na API | CRUD de todos | CRUD dos próprios, preservando histórico clínico |
| Consultas na API | CRUD de todas | Próprias; sem registrar ou alterar atendimento realizado e seu preço |
| Vacinas | CRUD e aplicação | Leitura dos próprios pets |
| Funcionários na API | CRUD | Leitura |
| Tutores e medicamentos na API | CRUD | Acesso negado |
| Cadastro de pets e vacinas no painel | Permitido | Acesso negado |
| Ficha e acompanhamento | Todos os pets | Próprios pets |

As rotas e a propriedade dos registros são verificadas no servidor. O e-mail vincula o login ao tutor; sua alteração administrativa é recusada quando há credencial vinculada. O cadastro público sempre cria TUTOR. Senhas são armazenadas como hash BCrypt, com validação do limite de 72 bytes; novas contas exigem de 12 a 60 caracteres.

O painel usa sessão e CSRF; a API exige Bearer token e não aceita HTTP Basic. O JWT vale oito horas, sem refresh token. Logout encerra a sessão web e não revoga tokens já emitidos. Os perfis `demo` e `persistente` exigem chave JWT própria, desabilitam o console H2 e usam cookies seguros por padrão. No Render, mantenha a chave específica já configurada, sem publicá-la.

## Executar

Pré-requisitos: JDK 17, `JAVA_HOME` configurado e acesso à internet para baixar Maven e dependências na primeira execução. O Maven Wrapper está incluído.

```powershell
git clone https://github.com/gabriel-g-dev/clyvovet-java.git
cd clyvovet-java
.\mvnw.cmd clean verify
java -jar target/clyvovet-0.0.1-SNAPSHOT.jar
```

Abra [localhost:8080/login](http://localhost:8080/login). Esse perfil local usa H2 em memória e console de desenvolvimento; não o publique. No Linux/macOS, use `sh mvnw clean verify`.

Para executar o mesmo perfil `demo` do Render em HTTP local, gere uma chave temporária no ambiente do processo:

```powershell
$chaveLocal = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($chaveLocal)
$env:CLYVOVET_JWT_SECRET = [Convert]::ToBase64String($chaveLocal)
$env:CLYVOVET_COOKIE_SECURE = 'false'
java -jar target/clyvovet-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

Não há variáveis obrigatórias para contas de acesso. A aplicação não carrega `.env` automaticamente. Para H2 em arquivo, Docker e opções de ambiente, consulte [Deploy](documents/deploy.md).

## API

- [Swagger UI local](http://localhost:8080/swagger-ui.html) e [OpenAPI JSON](http://localhost:8080/v3/api-docs).
- [Swagger UI online](https://clyvovet-java.onrender.com/swagger-ui.html).
- [Collection Insomnia](documents/ClyvoVet_Java_API_Insomnia.json).
- [Health check online](https://clyvovet-java.onrender.com/health), que consulta o banco.

| Método e rota | Finalidade |
| :--- | :--- |
| `POST /api/auth/login` | Obter token, perfil e validade |
| `POST /api/auth/register` | Cadastrar tutor e credencial na mesma transação |
| `GET /api/auth/me` | Consultar o próprio perfil |
| `GET /api/pets/{id}/inteligencia-preventiva` | Obter indicador e recomendações |
| `POST /api/vacinas/{id}/aplicar` | Registrar aplicação e reforço |
| `/api/pets`, `/api/consultas`, `/api/vacinas` | GET, POST, PUT e DELETE conforme perfil |
| `/api/tutores`, `/api/funcionarios`, `/api/medicamentos` | Cadastros de apoio conforme perfil |

No login, envie `{"email":"seu-email","senha":"sua-senha"}` e use o token no botão **Authorize** do Swagger ou em `Authorization: Bearer <token>`. Na collection, preencha os campos de acesso com as contas da seção de avaliação e copie o token retornado; tokens ficam vazios no arquivo exportado. As operações de escrita alteram os dados da instância.

As listagens principais aceitam `?page=0&size=10&sort=nome,asc`, com campo existente no recurso. O Swagger descreve filtros e DTOs. Datas usam ISO-8601. A aplicação de vacina aceita corpo ausente ou `{}` para hoje, ou `{"dataAplicacao":"AAAA-MM-DD"}` para uma data passada.

## Testes

```powershell
.\mvnw.cmd clean verify
```

O comando executa os testes e produz o JAR completo em `target/clyvovet-0.0.1-SNAPSHOT.jar`. A suíte cobre autenticação, perfis, isolamento entre tutores, validações, registro, histórico e preço de consultas, indicador preventivo, aplicação/reforço e páginas web. O [workflow CI](.github/workflows/ci.yml) executa `verify`; os relatórios locais ficam em `target/surefire-reports`.

Para a regressão de navegador, inicie a aplicação local na porta 18080 (`$env:PORT = '18080'` antes do comando Java). Em outro terminal, com Node.js e Chrome instalados:

```powershell
npm.cmd install --prefix target/browser-tools --no-package-lock --no-audit --no-fund playwright@1.56.1
node scripts/validar-ui.cjs
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/validar-persistencia.ps1
```

Os scripts usam dados fictícios e verificam interface, API e persistência local após reinício. Os [resultados e capturas](documents/evidencias/README.md) delimitam o que foi efetivamente executado. Relatórios, ferramentas de navegador e bancos de teste ficam em `target`, fora do Git, e são removidos por `clean`.

## Integração multidisciplinar

As entidades, chaves, índices e migrations evidenciam modelagem de dados; camadas, DTOs e diagramas representam engenharia de software; testes e regressão aplicam QA; navegação responsiva, estados vazios e validação aplicam UI/UX; build, CI, Dockerfile e configuração Render aplicam DevOps. As [evidências por disciplina](documents/integracao-disciplinas.md) apontam para os artefatos.

Os projetos .NET e Mobile têm implementações próprias. Não há consumo desta API por eles nem banco compartilhado demonstrados nesta entrega. O banco executado é H2; o driver Oracle no POM não comprova execução em Oracle.

## Integrantes

| Integrante | RM |
| :--- | :--- |
| Gabriel Garcia | 563298 |
| Andre Bellandi | 564662 |
| Vitor Augusto | 564227 |

## Apresentação

**Vídeo:https://youtu.be/qYahQt3wIno
