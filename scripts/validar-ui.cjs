// Regressao local com Chrome headless. Veja a secao de testes do README.
const { chromium } = require('../target/browser-tools/node_modules/playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs/promises');
const path = require('node:path');

const baseURL = process.env.CLYVOVET_BASE_URL || 'http://localhost:18080';
if (!['localhost', '127.0.0.1'].includes(new URL(baseURL).hostname)) {
    throw new Error('Execute esta regressao apenas sobre a instancia local de demonstracao.');
}
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
            if (response.status() >= 500) errors.push(`${response.status()} ${new URL(response.url()).pathname}`);
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

        for (const width of [1366, 768, 390, 360, 320]) {
            await page.setViewportSize({ width, height: 844 });
            response = await page.goto('/login');
            check(`Layout ${width}px /login`, response.status() === 200 && await page.evaluate(() =>
                document.documentElement.scrollWidth <= document.documentElement.clientWidth));
            if (width === 390) await page.screenshot({ path: path.join(output, 'login-mobile.png'), fullPage: true });
        }
        await page.setViewportSize({ width: 1366, height: 900 });
        await page.getByLabel('E-mail', { exact: true }).fill('admin@clyvovet.com.br');
        await page.getByLabel('Senha', { exact: true }).fill('senha-incorreta');
        await page.getByRole('button', { name: 'Entrar', exact: true }).click();
        check('Login invalido apresenta feedback', await page.getByText('E-mail ou senha inválidos.').isVisible());
        await page.getByLabel('E-mail', { exact: true }).fill('admin@clyvovet.com.br');
        await page.getByLabel('Senha', { exact: true }).fill('admin123');
        await page.getByRole('button', { name: 'Entrar', exact: true }).click();
        await page.waitForURL('**/dashboard');
        check('Login ADMIN abre painel', await page.getByRole('heading', { name: 'Painel de cuidados' }).isVisible());
        await page.screenshot({ path: path.join(output, 'dashboard-desktop.png'), fullPage: true });

        for (const width of [1366, 768, 390, 360, 320]) {
            await page.setViewportSize({ width, height: 900 });
            for (const route of ['/dashboard', '/pets', '/pets/1', '/pets/novo', '/pets/1/editar', '/pets/1/vacinas/novo']) {
                response = await page.goto(route);
                check(`Layout ${width}px ${route}`, response.status() === 200 && await page.evaluate(() =>
                    document.documentElement.scrollWidth <= document.documentElement.clientWidth));
            }
        }
        await page.setViewportSize({ width: 390, height: 844 });
        await page.goto('/dashboard');
        await page.screenshot({ path: path.join(output, 'dashboard-mobile.png'), fullPage: true });
        await page.goto('/pets/1');
        check('Ficha exibe historico real de consultas', await page.getByRole('table', { name: 'Consultas do pet' }).isVisible());
        await page.screenshot({ path: path.join(output, 'ficha-mobile.png'), fullPage: true });

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
        await page.screenshot({ path: path.join(output, 'validacao-formulario.png'), fullPage: true });
        await page.setViewportSize({ width: 390, height: 844 });
        await page.screenshot({ path: path.join(output, 'validacao-formulario-mobile.png'), fullPage: true });
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
        const login = await context.request.post('/api/auth/login', { data: { email: 'admin@clyvovet.com.br', senha: 'admin123' } });
        const token = (await login.json()).token;
        const vaccineId = vaccineAction.match(/\/vacinas\/(\d+)\/aplicar/)[1];
        response = await context.request.post(`/api/vacinas/${vaccineId}/aplicar`, { headers: { Authorization: `Bearer ${token}` }, data: {} });
        check('Reaplicacao da mesma dose retorna 409', response.status() === 409);

        expectedHttpError = true;
        response = await page.goto('/pets/99999999');
        check('Registro inexistente mostra pagina HTML 404', response.status() === 404 &&
            (await page.locator('h1').innerText()).includes('Registro não encontrado'));
        expectedHttpError = false;
        await page.goto('/dashboard');
        await page.getByRole('button', { name: 'Sair', exact: true }).click();
        await page.waitForURL('**/login?logout');
        check('Logout encerra sessao', await page.getByText('Sessão encerrada com sucesso.').isVisible());
        await page.getByLabel('E-mail', { exact: true }).fill('gabriel@clyvovet.com.br');
        await page.getByLabel('Senha', { exact: true }).fill('tutor123');
        await page.getByRole('button', { name: 'Entrar', exact: true }).click();
        await page.waitForURL('**/dashboard');
        check('Tutor ve Rex e nao ve Mel nem cadastro administrativo',
            await page.getByRole('link', { name: 'Rex', exact: true }).isVisible()
            && await page.getByRole('link', { name: 'Mel', exact: true }).count() === 0
            && await page.getByRole('link', { name: 'Cadastrar pet', exact: true }).count() === 0);
        expectedHttpError = true;
        response = await page.goto('/pets/2');
        check('Tutor recebe 403 HTML para pet de outro tutor', response.status() === 403 &&
            await page.getByRole('heading', { name: 'Acesso restrito' }).isVisible());
        response = await page.goto('/pets/novo');
        check('Tutor nao acessa cadastro da clinica', response.status() === 403);
        expectedHttpError = false;
        if (errors.length) console.error(errors);
        check('Console e recursos sem erros inesperados', errors.length === 0);
        const report = { executedAt: new Date().toISOString(), baseURL, browser: browser.version(),
            viewports: [1366, 768, 390, 360, 320], checks, errors };
        await fs.writeFile(path.join(output, 'validacao-ui.json'), JSON.stringify(report, null, 2) + '\n');
        console.log(`${checks.length} verificacoes aprovadas; capturas em target/evidencias/ui.`);
    } finally {
        await browser.close();
    }
})().catch(error => { console.error(error); process.exitCode = 1; });
