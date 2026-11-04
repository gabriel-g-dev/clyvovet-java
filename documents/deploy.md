# Deploy e configuração

URL do sistema: [clyvovet-java.onrender.com](https://clyvovet-java.onrender.com). O serviço usa o perfil `demo`; o Blueprint mantém a publicação automática desligada. A configuração local não altera o serviço remoto.

## Perfis

| Perfil | Banco e finalidade |
| :--- | :--- |
| Padrão | H2 em memória, com console para desenvolvimento local |
| `demo` | H2 em memória, dados fictícios restaurados a cada reinício, console desabilitado |
| `persistente` | H2 em arquivo, uma única instância por arquivo, console desabilitado |

As contas de demonstração são as mesmas nos três perfis e estão somente na seção de acesso do [README](../README.md). Não exigem cadastro, confirmação nem variáveis de provisionamento. Use exclusivamente dados fictícios na demonstração pública.

## Variáveis de ambiente

| Variável | Finalidade |
| :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `demo` no Render; `persistente` para H2 em arquivo |
| `CLYVOVET_JWT_SECRET` | Obrigatória em `demo` e `persistente`; chave Base64 de pelo menos 32 bytes |
| `CLYVOVET_COOKIE_SECURE` | `true` por padrão nesses perfis; `false` somente para HTTP local |
| `CLYVOVET_CORS_ORIGINS` | Origens externas autorizadas, separadas por vírgula; vazio por padrão nesses perfis |
| `CLYVOVET_DB_PASSWORD` | Obrigatória em `persistente` |
| `CLYVOVET_DATA_PATH` | Arquivo H2 em `persistente`; padrão `./data/clyvovet` |
| `PORT` | Porta HTTP; padrão 8080 |

Mantenha a chave JWT específica já configurada no Render e os cookies seguros em HTTPS. Não copie seu valor para o código, README, collection, logs ou capturas. A aplicação não carrega `.env` automaticamente. As instruções de execução local e geração de chave temporária estão no [README](../README.md#executar).

Para usar H2 em arquivo localmente, além da chave JWT, configure senha e caminho no ambiente e inicie o JAR com `--spring.profiles.active=persistente`. O diretório deve ser privado e ter backup. Em hospedagem, ele precisa de armazenamento que sobreviva ao reinício; o Blueprint atual não provisiona esse volume. O perfil mantém dados fictícios e não transforma a demonstração em operação clínica de produção.

## Publicação e conferência

As alterações precisam estar na origem usada pelo serviço antes da publicação manual pela equipe. O perfil `demo` exige apenas a chave JWT já prevista na configuração; não há novas variáveis de contas a cadastrar no Render.

Depois da publicação, confira:

- `/health`: HTTP 200 com `{"status":"UP"}`.
- Login com as duas contas do README e logout da sessão.
- ADMIN: dashboard, busca, cadastro/edição de pet, vacina, aplicação e reforço.
- TUTOR: somente os próprios pets e consultas; acesso negado a operações clínicas.
- Formulários com CSRF, mensagens de erro, navegação desktop e mobile.
- `/swagger-ui.html` e `/v3/api-docs`; dados da API exigindo Bearer token.
- `/h2-console` indisponível, recursos e links carregando corretamente.

O script de navegador cria dados fictícios e aceita somente endereços locais. Os resultados estão em [Evidências da aplicação](evidencias/README.md).

## Docker

```powershell
docker build -t clyvovet-java .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=demo -e CLYVOVET_JWT_SECRET -e CLYVOVET_COOKIE_SECURE=false clyvovet-java
```

Antes de executar o container, defina a chave JWT no ambiente do processo, conforme o README. O Dockerfile executa `verify`, empacota o JAR e usa JRE 17 com usuário sem privilégios de root. `.env` e bancos locais são excluídos pelo `.dockerignore`.

A imagem e seu runtime precisam ser verificados em um ambiente com Docker. No modo em memória, cadastros e aplicações se perdem a cada reinício. Antes da apresentação, confirme disponibilidade e tempo de inicialização do serviço.
