#!/usr/bin/env powershell
# Script para importar certificados Salesforce no Java Keystore
# Execute como Administrador

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Importador de Certificados Salesforce" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se está como Admin
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")
if (-not $isAdmin) {
    Write-Host "ERRO: Este script precisa rodar como Administrador!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Instruções:" -ForegroundColor Yellow
    Write-Host "1. Clique com botão direito neste arquivo .ps1" -ForegroundColor Yellow
    Write-Host "2. Selecione 'Run with PowerShell'" -ForegroundColor Yellow
    Write-Host "3. Escolha 'A' para executar para todo este usuário" -ForegroundColor Yellow
    Write-Host ""
    pause
    exit 1
}

# Encontrar Java
$javaPath = "C:\Program Files\Java\jdk-21"
if (-not (Test-Path $javaPath)) {
    Write-Host "ERRO: Java 21 não encontrado em $javaPath" -ForegroundColor Red
    pause
    exit 1
}

$keytoolPath = "$javaPath\bin\keytool.exe"
$keystorePath = "$javaPath\lib\security\cacerts"

Write-Host "Caminho do Java: $javaPath" -ForegroundColor Green
Write-Host "Caminho do Keystore: $keystorePath" -ForegroundColor Green
Write-Host ""

# Baixar certificados Salesforce
Write-Host "Baixando certificado de login.salesforce.com..." -ForegroundColor Yellow
try {
    # Criar conexão SSL
    $url = "https://login.salesforce.com"
    $cert = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2
    
    # Usar OpenSSL se disponível
    $opensslPath = "C:\Program Files\Git\usr\bin\openssl.exe"
    if (Test-Path $opensslPath) {
        & $opensslPath s_client -connect login.salesforce.com:443 -showcerts | `
            & $opensslPath x509 -outform PEM -out cert_login.pem
        Write-Host "Certificado salvo: cert_login.pem" -ForegroundColor Green
    } else {
        Write-Host "OpenSSL não encontrado. Você precisará importar manualmente." -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Procedimento manual:" -ForegroundColor Yellow
        Write-Host "1. Abra https://login.salesforce.com em um navegador" -ForegroundColor Yellow
        Write-Host "2. Clique no cadeado de segurança" -ForegroundColor Yellow
        Write-Host "3. Exportar certificado como PEM" -ForegroundColor Yellow
        Write-Host "4. Rodar: keytool.exe -import -alias salesforce ..." -ForegroundColor Yellow
        pause
        exit 0
    }
} catch {
    Write-Host "Erro ao baixar certificado: $_" -ForegroundColor Red
    pause
    exit 1
}

# Importar certificado
Write-Host ""
Write-Host "Importando certificado no Java Keystore..." -ForegroundColor Yellow
try {
    & $keytoolPath -import -alias salesforce-login -file cert_login.pem `
        -keystore $keystorePath -storepass changeit -noprompt
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Certificado importado com sucesso!" -ForegroundColor Green
    } else {
        Write-Host "Erro ao importar certificado (código: $LASTEXITCODE)" -ForegroundColor Red
    }
} catch {
    Write-Host "Erro: $_" -ForegroundColor Red
    pause
    exit 1
}

# Verificar
Write-Host ""
Write-Host "Verificando certificado..." -ForegroundColor Yellow
& $keytoolPath -list -keystore $keystorePath -storepass changeit | grep salesforce

Write-Host ""
Write-Host "SUCESSO!" -ForegroundColor Green
Write-Host ""
Write-Host "Você pode agora executar: .\run.bat" -ForegroundColor Green
Write-Host ""
pause
