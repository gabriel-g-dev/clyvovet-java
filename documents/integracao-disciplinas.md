# Integração entre disciplinas

O ClyvoVet aplica conceitos de banco de dados, testes, arquitetura e interface nos mesmos fluxos da aplicação Java. Os artefatos abaixo permitem relacionar essas frentes à solução implementada.

| Frente | Aplicação na solução | Evidências |
| :--- | :--- | :--- |
| Java Advanced | MVC, injeção de dependência, DTOs, validação, JPA, transações, segurança, cache e agendamento | [Controllers](../src/main/java/br/com/fiap/clyvovet/controller), [services](../src/main/java/br/com/fiap/clyvovet/service) e [segurança](../src/main/java/br/com/fiap/clyvovet/security) |
| Banco de dados | Modelo relacional com oito tabelas, chaves estrangeiras, unicidade e índices; evolução do schema por migrations | [DER e classes](diagramas.md) e [migrations versionadas](../src/main/resources/db/migration) |
| QA | Testes de autorização, isolamento entre tutores, cadastro, preço de consulta, cálculo preventivo e aplicação com reforço; regressão de navegador e persistência após reinício | [Testes Java](../src/test/java/br/com/fiap/clyvovet), [collection Insomnia](ClyvoVet_Java_API_Insomnia.json), [script de interface](../scripts/validar-ui.cjs) e [script de persistência](../scripts/validar-persistencia.ps1) |
| Engenharia de software | Camadas, contratos de entrada e saída e reutilização das regras pelo painel e pela API | [Arquitetura e fluxos](diagramas.md), [DTOs](../src/main/java/br/com/fiap/clyvovet/dto) e [services](../src/main/java/br/com/fiap/clyvovet/service) |
| UI/UX | Navegação por perfil, busca, estados vazios, validação de formulários e adaptação a telas pequenas | [Templates](../src/main/resources/templates), [CSS](../src/main/resources/static/css/clyvovet.css) e [imagens da aplicação](evidencias/README.md) |
| DevOps e operação | Build com testes, CI, imagem Docker, configuração por ambiente e health check | [Workflow CI](../.github/workflows/ci.yml), [Dockerfile](../Dockerfile), [Blueprint Render](../render.yaml) e [instruções de deploy](deploy.md) |

## Relação entre os artefatos

O Flyway cria e versiona o banco, enquanto `ddl-auto=validate` confere a correspondência entre schema e entidades. A aplicação de uma vacina usa uma transação para registrar a dose e criar o reforço. O painel e a API chamam o mesmo service, e os testes verificam o resultado e os caminhos de erro.

## Limites da integração

- O Mobile e o projeto .NET têm repositórios próprios. Não há consumo desta API Java por esses sistemas demonstrado nesta entrega.
- O banco configurado e utilizado nos testes é H2. A presença do driver Oracle no POM não comprova execução nesse banco.
- A varredura preventiva registra avisos no log; não há serviço de envio de e-mail, SMS ou push integrado.
- A URL pública é [clyvovet-java.onrender.com](https://clyvovet-java.onrender.com). A configuração Docker/Render não comprova a disponibilidade do serviço: a versão publicada precisa ser conferida após a publicação das alterações locais.
