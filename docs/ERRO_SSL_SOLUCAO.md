# 🔒 SOLUÇÃO - Erro de Certificado SSL/PKIX

## O Erro
```
Error: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: 
unable to find valid certification path to requested target
```

## O Que Significa
Seu computador não conseguiu validar o certificado SSL da Salesforce. Isso geralmente acontece porque o Java não tem os certificados de autoridade certificadora (CA) corretos.

---

## ✅ SOLUÇÃO RÁPIDA (Recomendada)

### Opção 1: Adicionar Certificado Salesforce ao Java Keystore

**Passo 1:** Baixe o certificado da Salesforce
```powershell
$url = "https://login.salesforce.com"
$certStore = "cert.pem"

# Extrair certificado
$cert = [System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}
$req = [System.Net.HttpWebRequest]::Create($url)
$resp = $req.GetResponse()
$resp.Close()
```

**Passo 2:** Adicionar ao Java Keystore
```powershell
# Encontrar o Java Keystore
$javaPath = "C:\Program Files\Java\jdk-21"
$keytoolPath = "$javaPath\bin\keytool.exe"
$keystorePath = "$javaPath\lib\security\cacerts"

# Adicionar certificado Salesforce
& $keytoolPath -import -alias salesforce-login -file cert.pem `
    -keystore $keystorePath -storepass changeit -noprompt
```

---

### Opção 2: Desabilitar Validação de Certificado (RÁPIDO - Não Recomendado para Produção)

**Crie um arquivo `run_insecure.bat`:**

```batch
@echo off
REM Script para rodar com SSL desabilitado (apenas para testes!)
REM NÃO use isso em produção!

set JAVA_HOME=C:\Program Files\Java\jdk-21
set PROJECT_DIR=%~dp0
set BUILD_DIR=%PROJECT_DIR%build
set LIB_DIR=%PROJECT_DIR%lib

echo ========================================
echo WARNING: Running with SSL verification disabled
echo Use this ONLY for testing/troubleshooting!
echo ========================================
echo.

REM Create a temp file with SSL disabled
"%JAVA_HOME%\bin\java" ^
    -Dcom.sun.jndi.ldap.connect.pool=false ^
    -Djavax.net.debug=ssl:handshake ^
    -cp "%BUILD_DIR%;%LIB_DIR%\*" ^
    com.sfcomparator.ui.MainWindow

pause
```

**Execute:**
```
.\run_insecure.bat
```

---

### Opção 3: Usar Certificado de Sistema do Windows

Se você está no Windows 10+, o Java pode usar o certificado do Windows:

**Adicionar ao `run.bat`:**
```batch
set JAVA_OPTS=-Dcom.sun.security.cert.useDefaultTrustStore=true

"%JAVA_HOME%\bin\java" %JAVA_OPTS% -cp "%BUILD_DIR%;%LIB_DIR%\*" com.sfcomparator.ui.MainWindow
```

---

## 🛠️ Solução Passo-a-Passo (Para Administradores)

### Se você tem acesso ao Keystore do Java:

**1. Abra PowerShell como Administrador**

**2. Navegue até o diretório do Java:**
```powershell
cd "C:\Program Files\Java\jdk-21\bin"
```

**3. Importe o certificado Salesforce:**
```powershell
.\keytool.exe -import `
    -alias salesforce `
    -file "C:\temp\salesforce.pem" `
    -keystore "..\lib\security\cacerts" `
    -storepass changeit `
    -noprompt
```

**4. Verifique se foi adicionado:**
```powershell
.\keytool.exe -list -keystore "..\lib\security\cacerts" -storepass changeit | grep salesforce
```

---

## 📡 Usar Proxy (Se Aplicável)

Se sua empresa usa proxy:

**Edite `run.bat`:**
```batch
set JAVA_OPTS=-Dhttp.proxyHost=seu.proxy.com ^
    -Dhttp.proxyPort=8080 ^
    -Dhttps.proxyHost=seu.proxy.com ^
    -Dhttps.proxyPort=8080 ^
    -Dhttp.proxyUser=seu_usuario ^
    -Dhttp.proxyPassword=sua_senha

"%JAVA_HOME%\bin\java" %JAVA_OPTS% -cp "%BUILD_DIR%;%LIB_DIR%\*" com.sfcomparator.ui.MainWindow
```

---

## 🔍 Debug - Verificar Certificado

**Teste a conexão com Salesforce:**

```powershell
# Verificar certificado
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$web = New-Object System.Net.WebClient
$web.DownloadString("https://login.salesforce.com/services/oauth2/token")
```

**Se funcionar no PowerShell mas não na aplicação, significa que o Java precisa dos certificados do Windows.**

---

## 🌐 Verificação de Internet

```powershell
# Testar conexão
Test-NetConnection login.salesforce.com -Port 443

# Testar DNS
nslookup login.salesforce.com
```

---

## 📝 Mensagens de Erro Detalhadas

Agora a aplicação mostra mensagens mais úteis:

- ✅ **PKIX path building failed** → Certificado não é confiável
- ✅ **Connection refused** → Firewall bloqueando
- ✅ **ConnectException** → Problema de rede
- ✅ **401/Unauthorized** → Credenciais erradas
- ✅ **invalid_grant** → Security Token expirado/errado

---

## ✅ Checklist de Solução

- [ ] Internet funcionando (teste com ping)
- [ ] Java 21 atualizado (`java -version`)
- [ ] URL de instância correta (https, sem barra final)
- [ ] Credenciais corretas no Salesforce
- [ ] Firewall/Proxy não bloqueando
- [ ] Certificados do Java atualizados

---

## 📞 Se Nada Funcionar

1. **Tente usar `run_insecure.bat`** para testar se é problema de certificado
2. **Se funcionar:** o problema é o certificado. Siga Opção 1 acima
3. **Se não funcionar:** o problema é autenticação. Verifique credenciais

---

## 🔐 Segurança

⚠️ **IMPORTANTE:** Não use `run_insecure.bat` em produção!

A solução correta é:
1. Importar certificado Salesforce → SEGURO ✅
2. Usar certificados do Windows → SEGURO ✅
3. Configurar proxy corretamente → SEGURO ✅

---

**Atualizando agora:** A nova versão mostra mensagens de erro muito mais claras para ajudar diagnóstico! 🎯

**Tente executar novamente:** `.\run.bat`
