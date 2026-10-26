# ClyvoVet — Sprint 4 · Java Advanced

Aplicação web para organizar o acompanhamento preventivo de pets no Challenge FIAP 2026, em parceria com a Clyvo VET. O projeto reúne um painel em Thymeleaf e uma API REST com JWT sobre as mesmas regras de negócio.

## Acesso para avaliação

**Não há URL pública confirmada.** O projeto contém Dockerfile e configuração para publicação no Render Free. O procedimento, as variáveis e os limites de persistência estão em [Deploy](documents/deploy.md).

Para avaliar localmente, execute os comandos abaixo e abra [o login](http://localhost:8080/login).

| Perfil | E-mail de demonstração | Senha |
| :--- | :--- | :--- |
| Clínica — ADMIN | `admin@clyvovet.com.br` | `admin123` |
| Tutor — TUTOR | `gabriel@clyvovet.com.br` | `tutor123` |

Essas contas e os dados iniciais são fictícios, destinados à demonstração. Use essa instância apenas com dados de demonstração.

1. Entre como ADMIN, abra o dashboard e a ficha de **Rex**.
2. Observe a vacina Antirrábica atrasada e o score de acompanhamento.
3. Registre a aplicação. Confira o feedback, a dose aplicada e o reforço criado automaticamente.
4. Cadastre um pet e agende uma vacina pela ficha. Confira os estados sem histórico.
5. Saia e entre como TUTOR. Apenas Rex, do tutor Gabriel, deve aparecer; o cadastro de Mel pertence a outra pessoa.
6. Para a API, abra [Swagger UI](http://localhost:8080/swagger-ui.html), faça login em `POST /api/auth/login` e use o token no botão **Authorize**.

## Problema e proposta

Vacinas pendentes, consultas e dados do tutor dispersos dificultam o acompanhamento contínuo do pet. O ClyvoVet centraliza esse histórico e transforma os registros em uma lista de pendências que a clínica pode consultar antes do atendimento.

A principal decisão de produto foi aproximar o alerta da ação: a ficha mostra a pendência, permite registrar a aplicação e agenda a próxima dose quando há um intervalo de reforço cadastrado. O mesmo fluxo está disponível na API.

O score é um indicador de acompanhamento calculado por regras explícitas. Ele depende dos registros cadastrados e não realiza diagnóstico. Um calendário vazio não comprova que o pet está em dia.

## Funcionalidades

- Login web com sessão, logout e permissões por perfil.
- Dashboard com resumo dos pets e das pendências vacinais visíveis ao usuário.
- Busca de pets por nome, espécie, raça ou tutor.
- Ficha com dados do pet, recomendações, calendário vacinal e histórico de consultas.
- Cadastro e edição de pets e agendamento de vacinas pela clínica no painel.
- Baixa de vacina com agendamento automático do reforço e bloqueio de reaplicação.
- API REST para pets, tutores, vacinas, consultas, funcionários e medicamentos, com paginação nas listagens principais.
- Cadastro público de tutor pela API, login JWT e consulta do próprio perfil.
- Isolamento dos dados por tutor, validação de entrada e tratamento de erros.
- Varredura periódica de pendências registrada nos logs. **Não há envio real de e-mail ou SMS.**

## Executar localmente

Pré-requisitos: JDK 17, `JAVA_HOME` configurado, acesso à internet na primeira execução para baixar o Maven e suas dependências, e porta 8080 disponível. Não é necessário instalar Maven ou um servidor de banco.

```powershell
git clone https://github.com/gabriel-g-dev/clyvovet-java.git
cd clyvovet-java

# Compilar, testar e gerar o JAR
.\mvnw.cmd clean verify

# Executar o JAR
java -jar target/clyvovet-0.0.1-SNAPSHOT.jar
```

Outra opção para desenvolvimento é `.\mvnw.cmd spring-boot:run`. No Linux/macOS, use `sh mvnw clean verify` e `sh mvnw spring-boot:run`; assim não é necessário alterar a permissão de execução do arquivo.

| Recurso local | Endereço |
| :--- | :--- |
| Painel | [Login](http://localhost:8080/login) |
| Swagger UI | [Documentação interativa](http://localhost:8080/swagger-ui.html) |
| OpenAPI JSON | [Contrato JSON](http://localhost:8080/v3/api-docs) |
| Saúde do processo | [Health](http://localhost:8080/health) |

No perfil local, o H2 é em memória: os dados voltam à carga inicial após reiniciar o processo. As datas da carga são relativas a `CURRENT_DATE`, mantendo exemplos de atraso e de vacina próxima do vencimento. O console H2 é um recurso de desenvolvimento; consulte as diferenças entre perfis em [Deploy](documents/deploy.md).

Console local: [H2](http://localhost:8080/h2-console), JDBC URL `jdbc:h2:mem:clyvovetdb`, usuário `sa`, senha em branco. Os perfis `demo` e `persistente` desabilitam esse console. O perfil `persistente` mantém o H2 em arquivo e exige configuração de diretório e senha; no Render Free, o deploy preparado usa `demo` e perde alterações ao reiniciar.

## Tecnologias e arquitetura

| Tecnologia | Uso concreto |
| :--- | :--- |
| Java 17 e Spring Boot 3.3.5 | Aplicação e configuração |
| Spring MVC e Thymeleaf | Painel renderizado no servidor |
| Spring Security e BCrypt | Sessão web, proteção CSRF e hash de senha |
| jjwt 0.12.6 | Emissão e validação de tokens da API |
| Spring Data JPA e H2 | Entidades, relacionamentos e persistência |
| Flyway | Criação e evolução versionada do schema |
| Bean Validation | Restrições de campos nos contratos de entrada |
| springdoc-openapi 2.6.0 | Contrato OpenAPI e Swagger UI |
| Spring Cache e Scheduling | Cache de leituras e varredura de pendências |
| JUnit, MockMvc e Spring Security Test | Testes de regras, fluxos e autorização |

```text
Navegador → Security (sessão + CSRF) → WebController → services
Cliente REST → Security (JWT) → controllers /api → services
services → repositories JPA → H2 (schema Flyway)
```

Os controllers tratam a entrada HTTP; os DTOs definem os contratos; os services concentram os fluxos de pets, consultas, vacinas e acompanhamento; os repositories acessam o banco. Os cadastros auxiliares de tutores, funcionários e medicamentos mantêm CRUDs diretos nos controllers. O painel e a API reutilizam os services, evitando duas implementações para a mesma baixa de vacina. O Flyway é a fonte do schema e o Hibernate apenas o valida (`ddl-auto=validate`).

Thymeleaf mantém a entrega simples: formulários, feedback e navegação na própria aplicação Java. A separação entre sessão web e JWT permite que clientes da API recebam status HTTP, enquanto o navegador recebe páginas e redirecionamentos. Os [diagramas](documents/diagramas.md) detalham classes, relações e fluxos.

## Regras dos fluxos de negócio

### Acompanhamento preventivo

`GET /api/pets/{id}/inteligencia-preventiva` cruza vacinas e consultas, devolvendo score de 0 a 100, classificação e recomendações. A ficha web usa o mesmo cálculo.

| Situação | Regra |
| :--- | :--- |
| Sem vacinas registradas | −30 pontos |
| Cada vacina pendente no passado | −15 pontos |
| Sem consulta realizada | −30 pontos |
| Última consulta realizada há mais de um ano | −20 pontos |
| Vacina próxima, dentro de 8 dias | Aviso, sem desconto |
| Score abaixo de 50 | CRÍTICO |
| Vacina atrasada ou score abaixo de 85 | ATENÇÃO |
| Score a partir de 85, sem atraso | EXCELENTE |

O score nunca fica negativo. Consultas canceladas e agendamentos não comprovam atendimento realizado. Uma vacina atrasada impede a classificação EXCELENTE, mesmo com score alto.

### Aplicação e reforço

`POST /api/vacinas/{id}/aplicar` recebe `{}` para aplicar na data atual, ou `{"dataAplicacao":"AAAA-MM-DD"}`. O fluxo, exclusivo do ADMIN:

1. Localiza a vacina e rejeita a reaplicação de uma dose já aplicada (`409`).
2. Rejeita uma data futura na entrada da API (`400`).
3. Marca a dose como aplicada e registra a data informada.
4. Se há intervalo positivo, agenda uma dose pendente em `dataAplicacao + intervaloReforcoDias`.
5. Evita criar de novo uma dose pendente do mesmo pet, nome e data. Sem intervalo, apenas registra a aplicação.

A operação ocorre em uma transação. O retorno e a mensagem do painel distinguem reforço criado, já agendado e sem intervalo. Os intervalos da massa de demonstração são exemplos de cadastro; o sistema não escolhe o protocolo vacinal pelo veterinário.

## Perfis e segurança

| Recurso | ADMIN | TUTOR |
| :--- | :--- | :--- |
| Pets e consultas na API | CRUD de todos | CRUD dos próprios pets e consultas |
| Calendário vacinal | CRUD e aplicação | Leitura dos próprios pets |
| Funcionários na API | CRUD | Leitura |
| Medicamentos e tutores na API | CRUD | Sem acesso (`403`) |
| Cadastro/edição de pets e vacinas no painel | Permitido | Sem acesso |
| Ficha e acompanhamento | Qualquer pet | Próprios pets |

O e-mail vincula `USUARIOS` ao cadastro em `DONOS`, com unicidade nas duas tabelas. `SecurityConfig` decide as permissões de rota e `EscopoUsuarioService` verifica a propriedade do recurso. Listagens de tutor já retornam apenas seus registros. Ao cadastrar ou editar um pet pela API, o tutor não pode transferi-lo alterando `tutorId`.

Como o e-mail sustenta esse vínculo, o cadastro administrativo recusa a troca de e-mail de um tutor que já tenha login (`409`). A edição de nome e telefone continua disponível; não há fluxo de alteração de credencial nessa operação.

O valor da consulta é definido pela clínica: o valor enviado pelo TUTOR é ignorado; consultas novas desse perfil começam em zero e a edição preserva o preço existente. O registro público sempre cria `ROLE_TUTOR`, sem aceitar elevação de perfil pelo cliente.

A API usa `Authorization: Bearer <token>`. O token vale 8 horas, sem refresh token; após expirar, é necessário fazer login novamente. A chave local serve somente à demonstração; a publicação deve usar `CLYVOVET_JWT_SECRET`, conforme [Deploy](documents/deploy.md).

O CORS de desenvolvimento aceita `localhost`, `127.0.0.1`, a faixa `192.168.*.*` e `exp://` em `/api/**`. Um cliente web publicado em outra origem exige configuração explícita dessa origem. Essa preparação não comprova uma integração mobile executada.

## API e navegação

| Método e rota | Finalidade |
| :--- | :--- |
| `POST /api/auth/register` | Cadastrar tutor e credencial na mesma transação |
| `POST /api/auth/login` | Obter token, perfil e validade |
| `GET /api/auth/me` | Consultar o próprio perfil autenticado |
| `GET /api/pets` e `/api/pets/{id}` | Listagem paginada e consulta individual |
| `GET /api/pets/{id}/inteligencia-preventiva` | Análise por regras |
| `POST /api/vacinas/{id}/aplicar` | Aplicação e reforço |
| `/api/pets`, `/api/vacinas`, `/api/consultas` | GET, POST, PUT e DELETE conforme perfil |
| `/api/tutores`, `/api/funcionarios`, `/api/medicamentos` | Cadastros de apoio conforme perfil |

Filtros adicionais por espécie, raça, tutor, pet, status e período estão descritos no Swagger e na [collection Insomnia](documents/ClyvoVet_Java_API_Insomnia.json). Nas listagens paginadas, use `?page=0&size=10&sort=nome,asc` (o campo de ordenação deve existir no recurso). Datas usam ISO-8601: `2026-10-26` e `2026-10-26T10:30:00`.

Exemplo de login e consulta no Bash:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@clyvovet.com.br","senha":"admin123"}'

# Copie o token retornado e substitua SEU_TOKEN.
curl http://localhost:8080/api/pets/1/inteligencia-preventiva \
  -H 'Authorization: Bearer SEU_TOKEN'
```

No Windows PowerShell, use `curl.exe` ou o Swagger para evitar diferenças do alias `curl`. Na collection Insomnia, ajuste `base_url` para o endereço em avaliação e os tokens do ambiente após o login. Operações de criação, edição e exclusão alteram a massa; execute a demonstração sobre uma base preparada.

| Rota web | Acesso e finalidade |
| :--- | :--- |
| `/login` | Público — entrada |
| `/dashboard` | ADMIN e TUTOR — resumo do escopo permitido |
| `/pets?q=Rex` | ADMIN e TUTOR — busca e listagem |
| `/pets/{id}` | ADMIN e TUTOR — ficha permitida |
| `/pets/novo` e `/pets/{id}/editar` | ADMIN — cadastro e edição |
| `/pets/{id}/vacinas/novo` | ADMIN — agendamento vacinal |
| `/acesso-negado` | Feedback de permissão no painel |

## Banco de dados e integração entre disciplinas

| Migration | Evidência |
| :--- | :--- |
| [V1](src/main/resources/db/migration/V1__create_tables.sql) | Oito tabelas, PKs, FKs, unicidade e índices |
| [V2](src/main/resources/db/migration/V2__insert_mocks.sql) | Dados fictícios com senhas BCrypt e datas relativas |
| [V3](src/main/resources/db/migration/V3__add_intervalo_reforco_vacina.sql) | Evolução do calendário com intervalo de reforço |

Banco de dados aparece na modelagem e nas migrations; QA aparece nos testes e na collection; engenharia de software aparece nos diagramas e na separação de responsabilidades; UI/UX aparece nos templates e no CSS responsivo. As evidências e seus limites estão em [Integração entre disciplinas](documents/integracao-disciplinas.md).

A execução documentada utiliza H2. O POM mantém o driver Oracle, sem validação de execução nesse banco. Não há integração executada com os projetos Mobile ou .NET; os artefatos dessas disciplinas são identificados em seus respectivos contextos.

## Testes

```powershell
.\mvnw.cmd clean verify
```

A suíte cobre inicialização, perfis e isolamento entre tutores, registro público, preço de consulta, score preventivo, baixa de vacina, formulários e páginas de erro. O workflow CI executa o build e disponibiliza os relatórios dos testes como artefato da execução.

Para verificar os fluxos no navegador, instale Node.js e Chrome. Inicie o JAR local na porta 18080:

```powershell
java -jar target/clyvovet-0.0.1-SNAPSHOT.jar --server.port=18080
```

Em outro terminal, na raiz do projeto:

```powershell
npm.cmd install --prefix target/browser-tools --no-package-lock --no-audit --no-fund playwright@1.56.1
node scripts/validar-ui.cjs
```

A regressão verifica login, navegação, formulários, permissões e layout entre 320 e 1366 px. Aceita somente `localhost`/`127.0.0.1` e cria um pet e doses fictícias. As capturas e o relatório são gravados em `target/evidencias/ui`; reinicie a instância em memória para restaurar a carga inicial. O build `clean` remove as ferramentas e os resultados locais de `target`.

Para verificar H2 em arquivo, execute em uma sessão PowerShell com permissão para scripts:

```powershell
.\scripts\validar-persistencia.ps1
```

O script inicia duas JVMs sequenciais na porta 18081, cria um cadastro em uma pasta H2 nova em `target` e verifica sua recuperação após reiniciar. O relatório fica em `target/evidencias/persistencia.json`. Esses resultados de execução permanecem fora do versionamento.

## Documentação

- [Arquitetura, classes e banco](documents/diagramas.md)
- [Integração multidisciplinar](documents/integracao-disciplinas.md)
- [Contribuições verificáveis da equipe](documents/colaboracao-equipe.md)
- [Deploy e perfis de execução](documents/deploy.md)
- [Collection Insomnia](documents/ClyvoVet_Java_API_Insomnia.json)
- [Imagens da aplicação](documents/evidencias/README.md)

## Integrantes

| Integrante | RM |
| :--- | :--- |
| Gabriel Garcia | 563298 |
| Andre Bellandi | 564662 |
| Vitor Augusto | 564227 |

As [evidências de colaboração](documents/colaboracao-equipe.md) identificam contribuições verificáveis de Gabriel no Java, Andre no DevOps e Vitor no Mobile. Os registros das outras disciplinas são apresentados em seus respectivos contextos, sem atribuí-los ao histórico Java.

## Estrutura

```text
src/main/java/br/com/fiap/clyvovet/
  controller/  Entradas web e REST
  service/     Regras e montagem das respostas
  security/    Sessão, JWT, perfis e propriedade
  model/       Entidades JPA
  repository/  Acesso ao banco
  dto/         Contratos e validação
  exception/   Exceções e respostas de erro
src/main/resources/
  db/migration/  Schema e carga Flyway
  templates/     Páginas Thymeleaf
  static/        Estilos do painel
src/test/         Testes automatizados
documents/        Arquitetura, integração, colaboração, deploy e imagens
```
