# 🚀 GUIA RÁPIDO - SETUP E PRIMEIROS PASSOS

## ⚡ Começar em 5 Minutos

### 1️⃣ Download e Extração
```
1. Extraia a pasta SalesforceComparator para seu computador
2. Abra a pasta no Explorador de Arquivos
```

### 2️⃣ Executar a Aplicação
**Windows:**
```
1. Duplo clique em: run.bat
2. A aplicação abrirá em segundos (primeira execução compila)
```

**Linux/Mac:**
```
bash build.sh
java -cp "build:lib/*" com.sfcomparator.ui.MainWindow
```

---

## 🔑 Obter Credenciais Salesforce

### Preparação: Criar um Usuário Técnico (RECOMENDADO)

**Setup → Administração de Usuários → Usuários:**
1. Clique em "Novo Usuário"
2. Preencha:
   - Nome: `Tech Lead Bot`
   - Email: `techbot+[orgname]@company.com`
   - Nome de Usuário: `techbot.[orgname]@company.com`
   - Perfil: `System Administrator` (ou com permissões de API)
3. Clique em "Salvar"
4. Marque "API Enabled" na permission set do usuário

### Opção A: Username/Password/Security Token

**Para cada Org:**

1. **Obtenha o Security Token**
   - Seu Avatar (canto superior direito) → Settings
   - Clique em "Reset Your Security Token"
   - Você receberá um email com o novo token
   - Copie o token

2. **Na aplicação, preencha:**
   - **Instance URL**: `https://login.salesforce.com` (produção) ou `https://test.salesforce.com` (sandbox)
   - **Username**: seu nome de usuário
   - **Password**: sua senha
   - **Security Token**: token recebido por email
   - Client ID e Client Secret: *deixe em branco*

### Opção B: OAuth2 (Client ID + Client Secret)

**Para cada Org:**

1. **Criar Connected App**
   - Setup → Apps → App Manager
   - Clique em "New Connected App"
   - Preencha:
     - App Name: `SalesforceComparator`
     - Contact Email: seu email
   - Rol para baixo, marque "Enable OAuth Settings"
   - Em "Selected OAuth Scopes", clique em Add:
     - Selecione `api`
     - Selecione `refresh_token`
   - Clique "Save"

2. **Obtenha as credenciais**
   - Clique em "Manage" (do lado do app)
   - Clique "Edit"
   - Copie "Consumer Key" (= Client ID)
   - Clique "Show" do lado de "Consumer Secret"
   - Copie "Consumer Secret" (= Client Secret)

3. **Na aplicação, preencha:**
   - **Instance URL**: `https://login.salesforce.com`
   - **Client ID**: Consumer Key copiado
   - **Client Secret**: Consumer Secret copiado
   - Username, Password, Security Token: *deixe em branco*

---

## 📝 Primeiro Teste

### Cenário: Comparar objeto Account entre Dev e QA

**Passo 1: Preencher Org 1 (Dev)**
```
Instance URL: https://login.salesforce.com
Username: techbot.dev@company.com
Password: ••••••••
Security Token: [código recebido por email]
```

**Passo 2: Preencher Org 2 (QA)**
```
Instance URL: https://test.salesforce.com
Username: techbot.qa@company.com
Password: ••••••••
Security Token: [código recebido por email]
```

**Passo 3: Configurar Comparação**
```
SObject Name: Account
Record Count Limit: 100
Record Type: (deixe vazio)
Metadados: deixe todos desmarcados
```

**Passo 4: Clicar "Start Comparison"**

**Resultado esperado:**
- ✅ Se estão sincronizadas: "No differences found!"
- ❌ Se houver campos diferentes: Tabela com divergências

---

## 💡 Interpretando Resultados

### Exemplo 1: Campo Faltando
```
Category: SObject
Name: Account.Rating__c
Org 1: Picklist
Org 2: MISSING
```
→ **Significado:** Campo personalizado existe em Dev mas não em QA
→ **Ação:** Você deve criar este campo em QA

### Exemplo 2: Tipo Diferente
```
Category: SObject
Name: Account.Industry
Org 1: Picklist
Org 2: String
```
→ **Significado:** Mesmo campo, mas tipos diferentes
→ **Ação:** Sincronize o tipo de campo entre os ambientes

---

## ✅ Checklist de Verificação

Antes de usar a ferramenta:

- [ ] Java 21 instalado (`java -version` no terminal)
- [ ] Usuário técnico criado em ambas as Orgs
- [ ] Permissão "API Enabled" ativa
- [ ] Security Tokens obtidos ou Connected Apps criados
- [ ] Arquivo `run.bat` executável
- [ ] Acesso à internet funcionando

---

## ❓ FAQ Rápido

**P: Quanto tempo leva uma comparação?**
R: 10-60 segundos dependendo da quantidade de metadados

**P: Posso rodá-lo em Sandbox?**
R: Sim! Use `https://test.salesforce.com` como Instance URL

**P: Preciso de permissões especiais?**
R: Sim, pelo menos: "API Enabled" e acesso ao objeto que está comparando

**P: Os dados são salvos em algum lugar?**
R: Não. Tudo ocorre em memória. Exporte o CSV se quiser guardar.

**P: Posso usar a mesma Org duas vezes?**
R: Sim, mas não faz sentido (resultado será "sem diferenças")

**P: E se eu cometer erro ao inserir credenciais?**
R: A aplicação mostrará erro de autenticação. Você pode corrigir e tentar novamente.

---

## 🆘 Problemas Comuns

### ❌ "Build failed"
### ❌ Erro de Certificado SSL (PKIX path building failed)

**Mensagem:** `Error: PKIX path building failed... unable to find valid certification path`

**Soluções rápidas (em ordem):**

1. **Tentar atualizar Java:**
   ```
   java -version
   ```
   Instale Java 21 LTS se estiver em versão anterior.

2. **Executar importador automático:**
   ```
   Clique com botão direito em: import_certificates.ps1
   Selecione: Run with PowerShell
   ```

3. **Usar modo debug para diagnosticar:**
   ```
   .\run_debug.bat
   ```

4. **Consulte documentação completa:**
   - Abra: `ERRO_SSL_SOLUCAO.md`
   - Opções 1, 2 ou 3 de solução

**Por que acontece?**
- Seu Java não tem os certificados de segurança atualizados da Salesforce
- Solução: Importar o certificado no Java Keystore

### ❌ "Build failed"
**Solução:**
1. Instale Java 21: https://www.oracle.com/java/technologies/downloads/
2. Reinicie o terminal
3. Execute `run.bat` novamente

### ❌ "Auth failed: invalid_grant"
**Solução:**
- Verifique credenciais (usuário/senha/token)
- Tente fazer login manual no Salesforce com essas credenciais
- Resete o Security Token

### ❌ "Unauthorized"
**Solução:**
- Verifique se usuário tem "API Enabled"
- Verifique permission sets
- Tente com System Administrator

### ❌ Aplicação não inicia
**Solução:**
1. Abra PowerShell como Administrador
2. Execute: `cd C:\SalesforceComparator`
3. Execute: `.\build.bat`
4. Execute: `.\run.bat`

---

## 🎬 Próximas Ações

Após seu primeiro teste bem-sucedido:

1. **Adicione à rotina de QA**
   - Execute antes de sprints
   - Mantenha histórico de resultados

2. **Teste outros objetos**
   - Contact, Lead, Opportunity, etc.

3. **Experimente metadados**
   - Marque checkbox "Validation Rules"
   - Execute comparação novamente

4. **Compartilhe com o time**
   - Copie a pasta SalesforceComparator
   - Distribua para outros líderes técnicos

---

## 📞 Suporte

Se encontrar problemas:

1. Consulte este guia
2. Verifique GUIA_DE_USO.md para mais detalhes
3. Consulte DOCUMENTACAO_TECNICA.md para questões técnicas
4. Verifique README.md para contexto geral

---

**Pronto para começar? Execute `run.bat` agora! 🚀**
