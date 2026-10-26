# Deploy do ClyvoVet

## Configuração

**Não há URL pública confirmada.** O projeto contém Dockerfile e Blueprint para publicar a aplicação Java completa no Render Free.

A opção escolhida é o **Render, plano Free, com Docker**. O serviço executa a aplicação Java completa, incluindo páginas Thymeleaf, API e Flyway. Não é necessário separar frontend e backend.

## Publicação gratuita

1. Crie sua conta em [Render](https://dashboard.render.com/) e entre com sua conta GitHub. Se aparecer solicitação de pagamento, não conclua a contratação.
2. Autorize acesso somente ao repositório `gabriel-g-dev/clyvovet-java`.
3. Escolha a branch que contém os arquivos da aplicação. O Render utiliza os arquivos publicados no repositório remoto.
4. No Render, selecione **New > Blueprint** e escolha o repositório e a branch publicada.
5. Confira os recursos antes de aplicar: deve existir somente o web service `clyvovet-java`, com `plan: free`. Não crie PostgreSQL, disco ou outro recurso pago.
6. O Blueprint usa `Dockerfile`, ativa o perfil `demo` e gera `CLYVOVET_JWT_SECRET`. Se criar um Web Service manualmente, escolha Docker e Free, defina `SPRING_PROFILES_ACTIVE=demo` e uma chave JWT Base64 aleatória com pelo menos 32 bytes.
7. Aguarde o build e a verificação de saúde `/health`. Copie a URL efetivamente fornecida pelo Render.
8. Valide os passos abaixo na URL pública e só então registre o endereço no README.

O deploy automático está desligado no Blueprint para que a equipe escolha quando publicar uma nova versão. A primeira criação do serviço ainda executa um deploy.

## O que validar online

- `GET /health`: HTTP 200 e `{"status":"UP"}`. O endpoint consulta o banco e não expõe configuração.
- `/login`: formulário acessível por HTTPS.
- ADMIN: login, painel, busca de pets, cadastro/edição, calendário e baixa com reforço.
- TUTOR: somente seus pets, sem botões administrativos; acessar pet de outro tutor deve retornar 403.
- `/swagger-ui.html`: documentação disponível; endpoints protegidos pedem Bearer token.
- `/h2-console`: console indisponível no perfil `demo`.
- Logout e novo login; navegação em desktop e celular; console do navegador sem erros inesperados.

As credenciais do README são fictícias e públicas. Use apenas dados de demonstração nesse serviço.

## Limites do plano gratuito

O Render suspende serviços Free após 15 minutos sem tráfego. O primeiro acesso seguinte pode demorar para reiniciar a aplicação. A quantidade de horas gratuitas é limitada por workspace e deve ser conferida no painel.

O workspace dispõe de 750 horas gratuitas por mês, compartilhadas entre serviços Free. Banda e minutos de build também têm limites: excedentes podem gerar cobrança quando há forma de pagamento cadastrada. Não cadastre forma de pagamento nem habilite recursos pagos para esta demonstração; confira o uso no painel antes da publicação. Sem forma de pagamento, esgotar a banda suspende os serviços Free; esgotar os minutos de build bloqueia novos builds, mantendo os serviços já publicados.

O perfil `demo` usa H2 em memória. Qualquer reinicialização, suspensão ou novo deploy recria a massa inicial e perde cadastros e aplicações de vacina feitos durante a sessão anterior. Abra a aplicação com antecedência para permitir a inicialização.

Não use esse modo para dados reais. Persistência durável exige volume persistente ou banco externo. O projeto inclui perfil `persistente` para testar H2 em arquivo em uma única instância, mas esse perfil **não inclui armazenamento gratuito no Render**. O Blueprint Free permanece com `demo`.

## Execução do mesmo perfil localmente

```powershell
.\mvnw.cmd clean verify
$chaveDemo = New-Object byte[] 32
$geradorDemo = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$geradorDemo.GetBytes($chaveDemo)
$geradorDemo.Dispose()
$env:CLYVOVET_JWT_SECRET = [Convert]::ToBase64String($chaveDemo)
$env:CLYVOVET_COOKIE_SECURE = 'false' # somente HTTP local
java -jar target/clyvovet-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

Em hospedagem HTTPS, mantenha cookies seguros habilitados (padrão dos perfis publicados). `PORT` define a porta HTTP; o padrão local é 8080.

Para testar persistência local, além da chave, defina `CLYVOVET_DB_PASSWORD`, `CLYVOVET_DATA_PATH` (padrão `./data/clyvovet`) e execute com `--spring.profiles.active=persistente`. Encerre e reabra a aplicação para conferir que os registros permanecem. Faça backup do arquivo e mantenha uma única instância por arquivo H2. Os usuários fictícios são inseridos apenas na primeira criação do banco.

## Docker

```powershell
docker build -t clyvovet-java .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=demo -e CLYVOVET_JWT_SECRET -e CLYVOVET_COOKIE_SECURE=false clyvovet-java
```

O build executa os testes antes de criar a imagem final. O runtime usa JRE 17 e usuário sem privilégios de root. A imagem não contém o repositório Git, os arquivos `.env` ou banco local. A construção e a execução da imagem devem ser confirmadas no ambiente que realizará o deploy.

## Referências da plataforma

Documentação oficial: [Blueprint YAML](https://render.com/docs/blueprint-spec), [plano Free](https://render.com/docs/free), [health checks](https://render.com/docs/health-checks) e [discos persistentes](https://render.com/docs/disks).
