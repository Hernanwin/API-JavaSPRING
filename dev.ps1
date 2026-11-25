<#
.SYNOPSIS
    Levanta Backend (Spring Boot :8080) y Frontend (Angular :4200) del monorepo Stockcomerce.
.DESCRIPTION
    - Detecta JAVA_HOME (JDK 17) automaticamente si no esta configurado.
    - Instala dependencias de npm en frontend/ si falta node_modules/.
    - Inicia ambos procesos en paralelo y los detiene limpiamente al presionar Ctrl+C.
#>

$ErrorActionPreference = 'Stop'

$RootDir = $PSScriptRoot
$BackendDir = Join-Path $RootDir 'backend'
$FrontendDir = Join-Path $RootDir 'frontend'

if (-not (Test-Path $BackendDir) -or -not (Test-Path $FrontendDir)) {
    Write-Error "No se encontraron las carpetas 'backend' y 'frontend' dentro de $RootDir"
    exit 1
}

# 1. Resolver JAVA_HOME si no esta definido o no apunta a un JDK valido
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $candidatosJdk = @()
    $rutasBusqueda = @(
        (Join-Path $env:USERPROFILE '.jdks'),
        'C:\Program Files\Microsoft',
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Java'
    )
    foreach ($base in $rutasBusqueda) {
        if (Test-Path $base) {
            $candidatosJdk += Get-ChildItem -Path $base -Directory -ErrorAction SilentlyContinue |
                Where-Object { Test-Path (Join-Path $_.FullName 'bin\java.exe') } |
                Sort-Object Name -Descending
        }
    }
    if ($candidatosJdk.Count -gt 0) {
        $jdk17 = $candidatosJdk | Where-Object { $_.Name -match '17' } | Select-Object -First 1
        $elegido = if ($jdk17) { $jdk17 } else { $candidatosJdk[0] }
        $env:JAVA_HOME = $elegido.FullName
        Write-Host "[OK] JAVA_HOME configurado automaticamente: $env:JAVA_HOME" -ForegroundColor Cyan
    } else {
        Write-Warning "No se encontro JAVA_HOME. Si Maven falla, defini `$env:JAVA_HOME antes de ejecutar dev.ps1."
    }
}

# 2. Instalar node_modules si el frontend fue movido sin dependencias
if (-not (Test-Path (Join-Path $FrontendDir 'node_modules'))) {
    Write-Host "[INFO] Instalando dependencias de Angular en frontend/ (npm install)..." -ForegroundColor Yellow
    Push-Location $FrontendDir
    try {
        npm install
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    } finally {
        Pop-Location
    }
}

Write-Host ""
Write-Host "=====================================================" -ForegroundColor Green
Write-Host "  Stockcomerce - Entorno Unificado de Desarrollo     " -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Green
Write-Host "  Frontend Angular : http://localhost:4200"
Write-Host "  Backend API      : http://localhost:8080/api"
Write-Host "  Docs Scalar      : http://localhost:8080/docs.html"
Write-Host "  Consola H2       : http://localhost:8080/h2-console"
Write-Host "  Admin inicial    : admin / admin123"
Write-Host "  Presiona Ctrl+C para detener ambos servicios."
Write-Host "=====================================================" -ForegroundColor Green
Write-Host ""

$backendProc = $null
$frontendProc = $null

try {
    $backendProc = Start-Process -FilePath 'cmd.exe' `
        -ArgumentList '/c', 'mvnw.cmd spring-boot:run' `
        -WorkingDirectory $BackendDir `
        -NoNewWindow -PassThru

    $frontendProc = Start-Process -FilePath 'cmd.exe' `
        -ArgumentList '/c', 'npm start' `
        -WorkingDirectory $FrontendDir `
        -NoNewWindow -PassThru

    while (-not $backendProc.HasExited -and -not $frontendProc.HasExited) {
        Start-Sleep -Seconds 1
    }
} finally {
    Write-Host "`n[INFO] Deteniendo servicios..." -ForegroundColor Yellow
    foreach ($proc in @($frontendProc, $backendProc)) {
        if ($proc -and -not $proc.HasExited) {
            # /T mata el arbol completo de procesos (java.exe / node.exe hijos de cmd.exe)
            & taskkill /PID $proc.Id /T /F *> $null
        }
    }
    Write-Host "[OK] Servicios detenidos." -ForegroundColor Green
}
