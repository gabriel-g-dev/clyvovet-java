$ErrorActionPreference = 'Stop'
$raizProjeto = if ($PSScriptRoot) { Split-Path -Parent $PSScriptRoot } else { (Get-Location).Path }
Set-Location -LiteralPath $raizProjeto
$execucao = [Guid]::NewGuid().ToString('N')
$diretorioTeste = Join-Path $raizProjeto "target/persistencia-$execucao"
New-Item -ItemType Directory -Path $diretorioTeste -Force | Out-Null
$jar = Join-Path $raizProjeto 'target/clyvovet-0.0.1-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $jar)) { throw 'Execute .\mvnw.cmd clean verify antes deste teste.' }

$chaveTeste = New-Object byte[] 32
$geradorTeste = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$geradorTeste.GetBytes($chaveTeste)
$geradorTeste.Dispose()
$env:CLYVOVET_JWT_SECRET = [Convert]::ToBase64String($chaveTeste)
$env:CLYVOVET_DB_PASSWORD = [Guid]::NewGuid().ToString('N')
$env:CLYVOVET_DATA_PATH = ($diretorioTeste -replace '\\', '/') + '/clyvovet'
$env:CLYVOVET_COOKIE_SECURE = 'false'
$env:PORT = '18081'
$enderecoTeste = 'http://localhost:18081'

function Iniciar-AplicacaoTeste([string]$fase) {
    $processo = Start-Process -FilePath java -WorkingDirectory $raizProjeto -ArgumentList '-jar',
        'target/clyvovet-0.0.1-SNAPSHOT.jar', '--spring.profiles.active=persistente', '--debug=false' `
        -WindowStyle Hidden -PassThru -RedirectStandardOutput "$diretorioTeste/$fase.log" `
        -RedirectStandardError "$diretorioTeste/$fase-error.log"
    for ($tentativa = 0; $tentativa -lt 60; $tentativa++) {
        if ($processo.HasExited) { throw "A aplicacao encerrou na fase $fase. Consulte os logs em target." }
        try {
            $saude = Invoke-RestMethod -Uri "$enderecoTeste/health" -TimeoutSec 2
            if ($saude.status -eq 'UP') { return $processo }
        } catch { }
        Start-Sleep -Milliseconds 500
    }
    Stop-Process -Id $processo.Id -ErrorAction SilentlyContinue
    throw "Tempo de inicializacao excedido na fase $fase."
}

function Obter-HeadersTeste {
    $login = Invoke-RestMethod -Uri "$enderecoTeste/api/auth/login" -Method Post -ContentType 'application/json' `
        -Body '{"email":"admin@clyvovet.com.br","senha":"admin123"}'
    return @{ Authorization = "Bearer $($login.token)" }
}

$processoTeste = $null
try {
    $processoTeste = Iniciar-AplicacaoTeste 'primeira-inicializacao'
    $headersTeste = Obter-HeadersTeste
    $nomeTeste = "Persistencia QA $($execucao.Substring(0, 8))"
    $novoPet = @{ nome = $nomeTeste; especie = 'Gato'; raca = 'SRD'; peso = 4.5;
        dataNascimento = '2024-01-01'; tutorId = 1 } | ConvertTo-Json
    $pet = Invoke-RestMethod -Uri "$enderecoTeste/api/pets" -Method Post -Headers $headersTeste `
        -ContentType 'application/json' -Body $novoPet
    # O teste encerra a JVM abruptamente. Aguarda a gravacao em disco do H2,
    # que usa escrita adiada, antes de verificar o registro em outro processo.
    Start-Sleep -Seconds 2
    Stop-Process -Id $processoTeste.Id
    $processoTeste.WaitForExit()
    $processoTeste = Iniciar-AplicacaoTeste 'segunda-inicializacao'
    $headersTeste = Obter-HeadersTeste
    $recuperado = Invoke-RestMethod -Uri "$enderecoTeste/api/pets/$($pet.id)" -Headers $headersTeste
    if ($recuperado.nome -ne $nomeTeste) { throw 'Registro divergiu apos reiniciar.' }
    $segundaInicializacao = Get-Content -LiteralPath "$diretorioTeste/segunda-inicializacao.log" -Raw
    if ($segundaInicializacao -notmatch 'Schema.*up to date') { throw 'Flyway nao confirmou schema atualizado no reinicio.' }
    $relatorioTeste = @{ executedAt = (Get-Date).ToUniversalTime().ToString('o'); profile = 'persistente';
        portFromEnvironment = 18081; createdPetId = $pet.id; survivesRestart = $true;
        flywayUpToDateOnRestart = $true; database = "target/persistencia-$execucao/clyvovet" }
    $saidaTeste = Join-Path $raizProjeto 'target/evidencias'
    New-Item -ItemType Directory -Path $saidaTeste -Force | Out-Null
    $relatorioTeste | ConvertTo-Json | Set-Content -LiteralPath "$saidaTeste/persistencia.json" -Encoding UTF8
    Write-Output 'OK: pet preservado apos reinicio; Flyway validou schema existente; porta obtida de PORT.'
} finally {
    if ($processoTeste -and -not $processoTeste.HasExited) { Stop-Process -Id $processoTeste.Id }
    Remove-Item Env:CLYVOVET_JWT_SECRET, Env:CLYVOVET_DB_PASSWORD, Env:CLYVOVET_DATA_PATH, Env:CLYVOVET_COOKIE_SECURE, Env:PORT -ErrorAction SilentlyContinue
}
