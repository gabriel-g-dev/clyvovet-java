# Evidências da aplicação

Validação local executada em **02/11/2026**, com dados fictícios e as contas de demonstração do [README](../../README.md).

- **Maven `clean verify`:** 63 testes em 12 classes, sem falhas, erros ou testes ignorados; JAR empacotado. Uma execução adicional de `package -DskipTests` também concluiu com sucesso.
- **Google Chrome 156.0.8078.74:** 179 verificações aprovadas nas larguras de 1440, 1366, 1024, 768, 390, 360 e 320 px. Incluem login ADMIN/TUTOR, logout, permissões, formulários, cadastro e edição de pets, consultas na API refletidas na ficha, vacinas, aplicação/reforço, páginas de erro, links, overflow e Swagger com JWT. Não houve erros inesperados de console ou carregamento de recursos.
- **Perfil `demo`:** logins no painel e na API funcionaram com as contas originais, sem variáveis privadas de contas. Foram mantidos os controles de perfil, propriedade dos dados e respostas 401/403.
- **Perfil `persistente`:** um pet criado foi recuperado após encerrar e reiniciar a JVM. Flyway confirmou as três migrations atualizadas e a aplicação respeitou a porta 18081 definida no ambiente.
- **Serviço público:** após timeouts iniciais, `/login`, `/health`, `/v3/api-docs` e `/swagger-ui.html` responderam HTTP 200 em 02/11/2026, com health `UP` e contrato identificado como ClyvoVet API. A versão hospedada ainda exibe credenciais na tela de login; as alterações locais precisam ser publicadas. A validação pública foi somente de leitura, sem autenticação ou alteração de dados.

A execução dos comandos do [README](../../README.md#testes) gera relatórios em `target/surefire-reports`, `target/evidencias/ui/validacao-ui.json` e `target/evidencias/persistencia.json`. Esses relatórios, ferramentas e bancos de teste são temporários, ficam fora do Git e não acompanham a entrega. As capturas selecionadas abaixo são preservadas como evidências.

As capturas abaixo foram atualizadas a partir dessa execução e inspecionadas visualmente:

| Imagem | Conteúdo |
| :--- | :--- |
| [Painel desktop](dashboard-desktop.png) | Visão geral dos pets e do calendário vacinal em 1366 px. |
| [Ficha mobile](ficha-mobile.png) | Acompanhamento preventivo, vacinas e consultas em 390 px. |
| [Validação de formulário](validacao-formulario-mobile.png) | Mensagens de campos obrigatórios no cadastro de pet em 390 px. |

A validação de interface corresponde ao Chrome nas resoluções listadas, sem representar todos os navegadores ou dispositivos físicos. Build/runtime Docker e execução em Oracle não foram validados. A execução local e as leituras públicas não comprovam os fluxos autenticados da versão hospedada; a publicação e essa conferência no Render continuam sob responsabilidade da equipe, conforme [Deploy](../deploy.md).
