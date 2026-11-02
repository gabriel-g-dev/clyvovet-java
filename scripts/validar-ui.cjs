// Regressao local com Chrome headless. Veja a secao de testes do README.
const { chromium } = require('../target/browser-tools/node_modules/playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs/promises');
const path = require('node:path');

const baseURL = process.env.CLYVOVET_BASE_URL || 'http://localhost:18080';
if (!['localhost', '127.0.0.1'].includes(new URL(baseURL).hostname)) {
    throw new Error('Execute esta regressao apenas sobre a instancia local de demonstracao.');
}
const credentials = {
    adminEmail: 'admin@clyvovet.com.br',
    adminPassword: 'admin123',
    tutorEmail: 'gabriel@clyvovet.com.br',
    tutorPassword: 'tutor123'
};
const viewports = [1440, 1366, 1024, 768, 390, 360, 320];
const output = path.resolve(__dirname, '../target/evidencias/ui');
const checks = [];
const errors = [];
function check(name, condition) {
    assert.ok(condition, name);
    checks.push(name);
    console.log('OK ' + name);
}

(async () => {
    await fs.mkdir(output, { recursive: true });
    const browser = await chromium.launch({ channel: 'chrome', headless: true });
    try {
        const context = await browser.newContext({ baseURL, viewport: { width: 1366, height: 900 },
            locale: 'pt-BR', timezoneId: 'America/Sao_Paulo' });
        const page = await context.newPage();
        let expectedHttpError = false;
        page.on('pageerror', e => errors.push(e.message));
        page.on('console', msg => {
            if (msg.type() === 'error' && !expectedHttpError) errors.push(msg.text());
        });
        page.on('response', response => {
            if (response.status() >= 500 || (response.status() >= 400 && !expectedHttpError)) {
                errors.push(`${response.status()} ${new URL(response.url()).pathname}`);
            }
        });
        page.on('requestfailed', request => errors.push(request.failure()?.errorText || 'Request failed'));

        let response = await context.request.get('/health');
        check('Health consulta o banco e responde UP', response.ok() && (await response.json()).status === 'UP');
        response = await context.request.get('/v3/api-docs');
        const contract = await response.json();
        check('OpenAPI possui esquema Bearer JWT', Object.values(contract.components.securitySchemes)
            .some(scheme => scheme.type === 'http' && scheme.scheme === 'bearer'));
        response = await context.request.get('/api/pets');
        check('API sem token retorna 401', response.status() === 401);

        for (const width of viewports) {
            await page.setViewportSize({ width, height: 844 });
            response = await page.goto('/login');
            check(`Layout ${width}px /login`, response.status() === 200 && await page.evaluate(() =>
                document.documentElement.scrollWidth <= document.documentElement.clientWidth));
            if (width === 390) await page.screenshot({ path: path.join(output, 'login-mobile.png'), fullPage: true });
        }
        check('Login nao expoe contas nem texto academico', !/admin@clyvovet|gabriel@clyvovet|admin123|tutor123|FIAP|acadêmico/i.test(await page.locator('body').innerText()));
        await page.setViewportSize({ width: 1366, height: 900 });
        await page.getByLabel('E-mail', { exact: true }).fill(credentials.adminEmail);
        await page.getByLabel('Senha', { exact: true }).fill('senha-incorreta');
        await page.getByRole('button', { name: 'Entrar', exact: true }).click();
        check('Login invalido apresenta feedback', await page.getByText('E-mail ou senha inválidos.').isVisible());
        await page.getByLabel('E-mail', { exact: true }).fill(credentials.adminEmail);
        await page.getByLabel('Senha', { exact: true }).fill(credentials.adminPassword);
        await page.getByRole('button', { name: 'Entrar', exact: true }).click();
        await page.waitForURL('**/dashboard');
        check('Login ADMIN abre painel', await page.getByRole('heading', { name: 'Painel de cuidados' }).isVisible());
        response = await context.request.get('/api/pets');
        check('Cookie da sessao web nao autentica a API', response.status() === 401);
        response = await context.request.post('/logout');
        check('Logout sem CSRF e rejeitado', response.status() === 403);
        await page.screenshot({ path: path.join(output, 'dashboard-desktop.png'), fullPage: true, mask: [page.locator('.usuario-nome')], maskColor: '#dfe9e4' });

        const links = new Set();
        for (const width of viewports) {
            await page.setViewportSize({ width, height: 900 });
            for (const route of ['/dashboard', '/pets', '/pets/1', '/pets/novo', '/pets/1/editar', '/pets/1/vacinas/novo']) {
                response = await page.goto(route);
                check(`Layout ${width}px ${route}`, response.status() === 200 && await page.evaluate(() =>
                    document.documentElement.scrollWidth <= document.documentElement.clientWidth));
                const brand = await page.locator('.marca').boundingBox();
                const logout = await page.getByRole('button', { name: 'Sair', exact: true }).boundingBox();
                check(`Cabecalho ${width}px ${route} mantem marca e saida na mesma linha`,
                    brand && logout && brand.y < logout.y + logout.height && logout.y < brand.y + brand.height);
                for (const href of await page.locator('a[href]').evaluateAll(elements => elements.map(element => element.getAttribute('href')))) {
                    if (href.startsWith('/') && !href.startsWith('//')) links.add(href);
                }
            }
        }
        for (const href of links) {
            response = await context.request.get(href);
            check(`Link interno ${href}`, response.ok());
        }
        await page.setViewportSize({ width: 390, height: 844 });
        await page.goto('/dashboard');
        await page.screenshot({ path: path.join(output, 'dashboard-mobile.png'), fullPage: true, mask: [page.locator('.usuario-nome')], maskColor: '#dfe9e4' });
        await page.goto('/pets/1');
        check('Ficha exibe historico real de consultas', await page.getByRole('table', { name: 'Consultas do pet' }).isVisible());
        await page.screenshot({ path: path.join(output, 'ficha-mobile.png'), fullPage: true, mask: [page.locator('.usuario-nome')], maskColor: '#dfe9e4' });

        await page.setViewportSize({ width: 1366, height: 900 });
        await page.goto('/pets');
        await page.getByLabel('Buscar pet').fill('animal-inexistente-qa');
        await page.getByRole('button', { name: 'Buscar', exact: true }).click();
        check('Busca vazia oferece orientacao', await page.getByText('Nenhum pet encontrado para esta busca.').isVisible());
        await page.getByRole('link', { name: 'Limpar', exact: true }).click();
        check('Limpar busca restaura pets', await page.getByRole('link', { name: 'Rex', exact: true }).isVisible());
        await page.goto('/pets/novo');
        await page.getByRole('button', { name: 'Cadastrar pet', exact: true }).click();
        check('Formulario invalido destaca campos', await page.locator('.erro-campo').count() >= 6);
        await page.screenshot({ path: path.join(output, 'validacao-formulario.png'), fullPage: true, mask: [page.locator('.usuario-nome')], maskColor: '#dfe9e4' });
        await page.setViewportSize({ width: 390, height: 844 });
        await page.getByRole('button', { name: 'Cadastrar pet', exact: true }).click();
        check('Formulario mobile preserva feedback de validacao', await page.locator('.erro-campo').count() >= 6);
        await page.screenshot({ path: path.join(output, 'validacao-formulario-mobile.png'), fullPage: true, mask: [page.locator('.usuario-nome')], maskColor: '#dfe9e4' });
        await page.setViewportSize({ width: 1366, height: 900 });
        const petName = 'Luna QA ' + Date.now();
        await page.getByLabel('Nome', { exact: true }).fill(petName);
        await page.getByLabel('Espécie', { exact: true }).fill('Gato');
        await page.getByLabel('Raça', { exact: true }).fill('SRD');
        await page.getByLabel('Data de nascimento').fill('2024-01-10');
        await page.getByLabel('Peso (kg)').fill('4.50');
        await page.getByLabel('Tutor responsável').selectOption('1');
        await page.getByRole('button', { name: 'Cadastrar pet', exact: true }).click();
        await page.waitForURL(/\/pets\/\d+$/);
        const petRoute = new URL(page.url()).pathname;
        check('Cadastro redireciona a ficha com estado vazio',
            await page.getByRole('heading', { name: petName, exact: true }).isVisible()
            && await page.getByText('Nenhuma vacina registrada.', { exact: true }).isVisible());
        await page.getByRole('link', { name: 'Editar cadastro' }).click();
        await page.getByLabel('Peso (kg)').fill('4.70');
        await page.getByRole('button', { name: 'Salvar alterações' }).click();
        await page.waitForURL('**' + petRoute);
        check('Edicao de pet mostra confirmacao', (await page.locator('.aviso-ok').innerText()).includes('atualizados'));
        await page.getByRole('link', { name: 'Agendar vacina', exact: true }).click();
        await page.getByRole('button', { name: 'Agendar dose' }).click();
        check('Agendamento vazio mostra erro no nome', await page.locator('#nome-erro').isVisible());
        await page.getByLabel('Nome da vacina').fill('Dose QA Sprint 4');
        await page.getByLabel('Intervalo de reforço (dias, opcional)').fill('30');
        await page.getByRole('button', { name: 'Agendar dose' }).click();
        await page.waitForURL('**' + petRoute);
        const vaccineRow = page.getByRole('row').filter({ hasText: 'Dose QA Sprint 4' });
        const vaccineAction = await vaccineRow.locator('form').getAttribute('action');
        check('Agendamento cria dose pendente', await vaccineRow.getByText('Agendada', { exact: true }).isVisible());
        await vaccineRow.getByRole('button').click();
        await page.waitForURL('**' + petRoute);
        check('Baixa cria reforco e mensagem de sucesso',
            (await page.locator('.aviso-ok').innerText()).includes('Próxima dose agendada automaticamente')
            && await page.getByRole('row').filter({ hasText: 'Dose QA Sprint 4' }).count() === 2);
        await page.setViewportSize({ width: 390, height: 844 });
        await page.getByRole('link', { name: 'Editar cadastro' }).click();
        await page.getByLabel('Peso (kg)').fill('4.80');
        await page.getByRole('button', { name: 'Salvar alterações' }).click();
        await page.waitForURL('**' + petRoute);
        const mobileSaved = (await page.locator('.aviso-ok').innerText()).includes('atualizados');
        await page.getByRole('link', { name: 'Editar cadastro' }).click();
        check('Edicao mobile salva e retorna a ficha com confirmacao',
            mobileSaved && Number(await page.getByLabel('Peso (kg)').inputValue()) === 4.8);
        await page.goto(petRoute);
        await page.setViewportSize({ width: 1366, height: 900 });
        const login = await context.request.post('/api/auth/login', { data: { email: credentials.adminEmail, senha: credentials.adminPassword } });
        check('Login API ADMIN com credenciais de demonstracao', login.ok());
        const token = (await login.json()).token;
        const vaccineId = vaccineAction.match(/\/vacinas\/(\d+)\/aplicar/)[1];
        response = await context.request.post(`/api/vacinas/${vaccineId}/aplicar`, { headers: { Authorization: `Bearer ${token}` }, data: {} });
        check('Reaplicacao da mesma dose retorna 409', response.status() === 409);

        const consultation = {
            petId: Number(petRoute.split('/').at(-1)), funcionarioId: 1,
            tipo: 'Retorno UI', descricao: 'Acompanhamento preventivo da regressao local',
            data: '2024-02-10T10:00:00', status: 'A', valor: 175.50
        };
        response = await context.request.post('/api/consultas', { headers: { Authorization: `Bearer ${token}` }, data: consultation });
        check('API cadastra consulta para o pet criado', response.status() === 201);
        const consultationId = (await response.json()).id;
        await page.goto(petRoute);
        const consultationRow = page.getByRole('row').filter({ hasText: 'Retorno UI' });
        check('Consulta da API aparece na ficha com valor e situacao',
            await consultationRow.getByText('Agendada', { exact: true }).isVisible()
            && (await consultationRow.innerText()).includes('175,50'));
        response = await context.request.put(`/api/consultas/${consultationId}`, {
            headers: { Authorization: `Bearer ${token}` }, data: { ...consultation, status: 'R' }
        });
        check('API registra consulta realizada', response.ok());
        await page.reload();
        check('Ficha reflete consulta realizada', await consultationRow.getByText('Realizada', { exact: true }).isVisible());

        expectedHttpError = true;
        response = await page.goto('/pets/99999999');
        check('Registro inexistente mostra pagina HTML 404', response.status() === 404 &&
            (await page.locator('h1').innerText()).includes('Registro não encontrado'));
        response = await page.goto('/pagina-inexistente-ui');
        check('Endereco inexistente mostra pagina 404', response.status() === 404 &&
            await page.getByRole('heading', { name: 'Página não encontrada' }).isVisible());
        expectedHttpError = false;
        await page.goto('/dashboard');
        await page.getByRole('button', { name: 'Sair', exact: true }).click();
        await page.waitForURL('**/login?logout');
        check('Logout encerra sessao', await page.getByText('Sessão encerrada com sucesso.').isVisible());
        await page.getByLabel('E-mail', { exact: true }).fill(credentials.tutorEmail);
        await page.getByLabel('Senha', { exact: true }).fill(credentials.tutorPassword);
        await page.getByRole('button', { name: 'Entrar', exact: true }).click();
        await page.waitForURL('**/dashboard');
        check('Tutor ve Rex e nao ve Mel nem cadastro administrativo',
            await page.getByRole('link', { name: 'Rex', exact: true }).isVisible()
            && await page.getByRole('link', { name: 'Mel', exact: true }).count() === 0
            && await page.getByRole('link', { name: 'Cadastrar pet', exact: true }).count() === 0);
        for (const width of viewports) {
            await page.setViewportSize({ width, height: 900 });
            for (const route of ['/dashboard', '/pets', '/pets/1']) {
                response = await page.goto(route);
                check(`Layout TUTOR ${width}px ${route}`, response.status() === 200 && await page.evaluate(() =>
                    document.documentElement.scrollWidth <= document.documentElement.clientWidth));
                const brand = await page.locator('.marca').boundingBox();
                const logout = await page.getByRole('button', { name: 'Sair', exact: true }).boundingBox();
                check(`Cabecalho TUTOR ${width}px ${route} mantem marca e saida na mesma linha`,
                    brand && logout && brand.y < logout.y + logout.height && logout.y < brand.y + brand.height);
            }
        }
        await page.setViewportSize({ width: 320, height: 844 });
        await page.goto('/dashboard');
        await page.screenshot({ path: path.join(output, 'tutor-mobile-320.png'), fullPage: true, mask: [page.locator('.usuario-nome')], maskColor: '#dfe9e4' });
        await page.goto('/pets/1');
        check('Tutor consulta historico sem acoes clinicas', await page.getByRole('table', { name: 'Consultas do pet' }).isVisible()
            && await page.getByRole('button', { name: /Registrar aplicação/ }).count() === 0
            && await page.getByRole('link', { name: 'Editar cadastro' }).count() === 0);
        const tutorLogin = await context.request.post('/api/auth/login', { data: {
            email: credentials.tutorEmail, senha: credentials.tutorPassword
        } });
        check('Login API TUTOR com credenciais de demonstracao', tutorLogin.ok());
        const tutorToken = (await tutorLogin.json()).token;
        response = await context.request.get('/api/pets/2', { headers: { Authorization: `Bearer ${tutorToken}` } });
        check('API impede acesso TUTOR ao pet de outra conta', response.status() === 403);
        response = await context.request.post(`/api/vacinas/${vaccineId}/aplicar`, {
            headers: { Authorization: `Bearer ${tutorToken}` }, data: {}
        });
        check('API impede TUTOR de registrar aplicacao', response.status() === 403);
        expectedHttpError = true;
        response = await page.goto('/pets/2');
        check('Tutor recebe 403 HTML para pet de outro tutor', response.status() === 403 &&
            await page.getByRole('heading', { name: 'Acesso restrito' }).isVisible());
        response = await page.goto('/pets/novo');
        check('Tutor nao acessa cadastro da clinica', response.status() === 403);
        expectedHttpError = false;
        await page.goto('/dashboard');
        await page.getByRole('button', { name: 'Sair', exact: true }).click();
        await page.waitForURL('**/login?logout');
        await page.goto('/dashboard');
        check('Logout TUTOR impede retorno a rota protegida', new URL(page.url()).pathname === '/login');
        await page.setViewportSize({ width: 1366, height: 900 });
        response = await page.goto('/swagger-ui.html');
        await page.locator('.swagger-ui .info .title').waitFor();
        check('Swagger renderiza contrato e oferece autorizacao JWT', response.ok()
            && (await page.locator('.swagger-ui .info .title').innerText()).includes('ClyvoVet API')
            && await page.getByRole('button', { name: 'Authorize', exact: true }).first().isVisible());
        if (errors.length) console.error(errors);
        check('Console e recursos sem erros inesperados', errors.length === 0);
        const report = { executedAt: new Date().toISOString(), baseURL, browser: browser.version(),
            viewports, checks, errors };
        await fs.writeFile(path.join(output, 'validacao-ui.json'), JSON.stringify(report, null, 2) + '\n');
        console.log(`${checks.length} verificacoes aprovadas; capturas em target/evidencias/ui.`);
    } finally {
        await browser.close();
    }
})().catch(error => {
    let message = error.stack || String(error);
    for (const value of Object.values(credentials)) message = message.replaceAll(value, '[redigido]');
    console.error(message);
    process.exitCode = 1;
});
