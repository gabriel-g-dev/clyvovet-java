# 📅 Cronograma de Desenvolvimento — ClyvoVet (Sprints 1 a 3)

Este documento descreve as fases de planejamento, modelagem, desenvolvimento e testes das APIs e artefatos do projeto ClyvoVet, especificando os responsáveis, entregáveis e datas de conclusão.

---

## 👥 Atribuição de Papéis
- **Gabriel Garcia (RM563298)**: Scrum Master, Desenvolvedor Backend (.NET & Java), Responsável por Git/GitHub.
- **Andre Bellandi (RM564662)**: Arquiteto de Software, Especialista em Bancos de Dados (Oracle & H2), Desenvolvedor Backend.
- **Vitor Augusto (RM564227)**: Engenheiro de QA, Documentador Técnico, Responsável por Testes e Collections (Insomnia/Postman).

---

## 🗓️ Cronograma de Atividades

| Fase | Atividade / Entregável | Responsável | Início | Fim | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Planejamento** | Análise do Briefing da Clyvo VET e definição de escopo | Todos | 16/04/2026 | 20/04/2026 | ✅ Concluído |
| **Modelagem** | Criação do DER, Diagrama de Classes e Arquitetura Macro | Andre Bellandi | 21/04/2026 | 26/04/2026 | ✅ Concluído |
| **Modelagem** | Criação dos scripts DDL no Oracle Data Modeler | Andre Bellandi | 27/04/2026 | 02/05/2026 | ✅ Concluído |
| **Setup .NET** | Estruturação inicial do projeto ClyvoVetApi (.NET Core 9) | Gabriel Garcia | 03/05/2026 | 06/05/2026 | ✅ Concluído |
| **Dev .NET** | Implementação dos CRUDs completas no Entity Framework Core | Gabriel Garcia | 07/05/2026 | 12/05/2026 | ✅ Concluído |
| **Dev .NET** | Configuração do Swagger e testes iniciais locais | Gabriel Garcia | 13/05/2026 | 14/05/2026 | ✅ Concluído |
| **Setup Java** | Criação do projeto Java com Spring Boot 3.3.5 e Maven | Gabriel Garcia | 05/05/2026 | 08/05/2026 | ✅ Concluído |
| **Dev Java** | Implementação dos Models, JpaRepositories e controladores REST | Andre Bellandi | 09/05/2026 | 14/05/2026 | ✅ Concluído |
| **Dev Java** | Paginação, ordenação e custom Query Methods (JPQL) | Gabriel / Andre | 15/05/2026 | 17/05/2026 | ✅ Concluído |
| **Dev Java** | Criação do GlobalExceptionHandler e tratamento de erros | Gabriel Garcia | 18/05/2026 | 19/05/2026 | ✅ Concluído |
| **Refatoração** | Criação e mapeamento de DTOs e validação com Bean Validation | Gabriel / Andre | 20/05/2026 | 20/05/2026 | ✅ Concluído |
| **Otimização** | Implementação do mecanismo de Cache nas consultas frequentes | Gabriel Garcia | 20/05/2026 | 20/05/2026 | ✅ Concluído |
| **QA / Testes** | Elaboração e teste da Collection completa no Insomnia | Vitor Augusto | 20/05/2026 | 21/05/2026 | ✅ Concluído |
| **Documentação** | Elaboração do README.md completo e diagramas integrados | Vitor Augusto | 21/05/2026 | 22/05/2026 | ✅ Concluído |
| **Entrega** | Consolidação dos repositórios, merge final e envio do desafio | Todos | 23/05/2026 | 24/05/2026 | ⏳ Pronto |

---

## 🗓️ Sprint 3 — Java Advanced (aplicação web completa)

| Fase | Atividade / Entregável | Responsável | Início | Fim | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Banco** | Migração do schema para Flyway (V1 tabelas, V2 carga inicial) | Andre Bellandi | 10/08/2026 | 14/08/2026 | ✅ Concluído |
| **Banco** | Troca do `ddl-auto` para `validate`, com o Flyway como fonte do schema | Andre Bellandi | 15/08/2026 | 16/08/2026 | ✅ Concluído |
| **Segurança** | Tabela `USUARIOS`, senhas em BCrypt e `UserDetailsService` | Gabriel Garcia | 17/08/2026 | 20/08/2026 | ✅ Concluído |
| **Segurança** | Duas filter chains: sessão no painel web e JWT stateless na API | Gabriel Garcia | 21/08/2026 | 25/08/2026 | ✅ Concluído |
| **Segurança** | Perfis ADMIN e TUTOR com permissões distintas por rota e método | Gabriel Garcia | 26/08/2026 | 28/08/2026 | ✅ Concluído |
| **Frontend** | Páginas Thymeleaf: login, dashboard, lista e ficha do pet | Gabriel / Vitor | 29/08/2026 | 02/09/2026 | ✅ Concluído |
| **Frontend** | Formulário de cadastro com validação campo a campo e feedback na tela | Vitor Augusto | 03/09/2026 | 04/09/2026 | ✅ Concluído |
| **Fluxo 1** | Motor de inteligência preventiva (score 0-100 e recomendações) | Gabriel Garcia | 05/09/2026 | 05/09/2026 | ✅ Concluído |
| **Fluxo 2** | Baixa de vacina com agendamento automático do reforço | Gabriel Garcia | 05/09/2026 | 06/09/2026 | ✅ Concluído |
| **QA / Testes** | Testes de permissão por perfil e das regras da baixa de vacina | Vitor Augusto | 06/09/2026 | 07/09/2026 | ✅ Concluído |
| **Documentação** | README, diagramas e collection do Insomnia atualizados | Vitor Augusto | 07/09/2026 | 07/09/2026 | ✅ Concluído |
| **Entrega** | Revisão final, gravação do vídeo e envio | Todos | 07/09/2026 | 07/09/2026 | ⏳ Pronto |

---

## 📈 Resumo do Progresso
- **Sprint 1 (Backend, Database, DevOps, Mobilidade)**: 100% concluída.
- **Sprint 2 (Consolidação de Requisitos, Cache, DTOs e Documentação)**: 100% concluída.
- **Sprint 3 (Flyway, Spring Security, frontend Thymeleaf e fluxos de negócio)**: 100% concluída.
- **Documentação de Processo**: Registrada na pasta `documents` conforme exigido pelas diretrizes acadêmicas da FIAP.
