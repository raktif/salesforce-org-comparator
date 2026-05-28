# Guia de Uso - Salesforce Org Comparator

## 🎯 Visão Geral

O **Salesforce Org Comparator** é uma ferramenta desktop desenvolvida em Java que permite comparar metadados e registros entre duas organizações Salesforce diferentes. Foi projetado para que líderes técnicos possam identificar e corrigir divergências entre ambientes regularmente.

---

## 🚀 Como Começar

### Pré-requisitos
- **Java 21** ou superior instalado
- Acesso à API REST do Salesforce em ambas as Orgs
- Credenciais de autenticação para ambas as Orgs

### Instalação Rápida

1. Extraia os arquivos do projeto em uma pasta (ex: `C:\SalesforceComparator`)
2. Abra o PowerShell ou CMD nessa pasta
3. Execute:
   ```
   .\run.bat
   ```

A aplicação compilará (primeira vez) e iniciará automaticamente.

---

## 📋 Interface da Aplicação

### Guia 1: Configuração (Configuration)

A primeira aba contém três seções:

#### **Organização 1 (Source)**
Preencha com os dados de conexão da primeira Org:

| Campo | Descrição | Exemplo |
|-------|-----------|---------|
| **Instance URL** | URL da instância Salesforce | `https://login.salesforce.com` ou `https://test.salesforce.com` |
| **Username** | Usuário da Org | `tech.lead@company.com` |
| **Password** | Senha do usuário | `***` |
| **Security Token** | Token de segurança (se necessário) | Obtém em Setup > Personal Setup > Reset Your Security Token |
| **Client ID** | ID do Connected App (OAuth) | `3MVG...` |
| **Client Secret** | Secret do Connected App (OAuth) | `***` |

**Nota:** Você pode usar EITHER (username/password + security token) OR (Client ID + Client Secret)

#### **Organização 2 (Target)**
Repita o processo acima com as credenciais da segunda Org.

#### **Configurações de Comparação**

- **SObject Name**: O objeto que deseja comparar
  - Exemplos: `Account`, `Contact`, `Lead`, `Opportunity`, `CustomObject__c`
  
- **Record Count Limit**: Limite de registros a verificar
  - Padrão: 100 registros
  - Aumentar para análises mais completas (pode aumentar tempo)
  
- **Record Type** (opcional): Filtrar por um Record Type específico
  - Deixe vazio para comparar todos os Record Types
  - Digite o Developer Name do Record Type (ex: `Standard`, `Premium`)

#### **Metadados para Comparar** (Checkboxes)

Selecione quais metadados você quer comparar. Cada tipo de metadado selecionado será verificado em ambas as Orgs:

- ☑️ **Page Layouts**: Layouts de página do objeto
- ☑️ **Validation Rules**: Regras de validação customizadas
- ☑️ **Flows**: Flows acionados por registros (Record Triggered)
- ☑️ **Apex Triggers**: Triggers Apex relacionadas ao objeto
- ☑️ **Custom Metadata**: Tipos de Custom Metadata
- ☑️ **Custom Settings**: Custom Settings (List ou Hierarchy)
- ☑️ **Permission Sets**: Permission Sets
- ☑️ **Profiles**: Profiles
- ☑️ **Groups**: Grupos de usuários (todos os tipos)
- ☑️ **Approval Processes**: Processos de aprovação
- ☑️ **Named Credentials**: Named Credentials

---

## 🔍 Executando uma Comparação

### Passo 1: Preencher Dados de Conexão
Complete os campos de autenticação para ambas as Orgs com credenciais de um usuário técnico com permissões adequadas.

### Passo 2: Configurar Parâmetros
1. Digite o nome do objeto SObject (ex: `Account`)
2. Defina o limite de registros a verificar
3. (Opcional) Especifique um Record Type
4. Selecione os metadados que deseja comparar

### Passo 3: Iniciar Comparação
Clique no botão verde **"Start Comparison"**.

A aplicação fará:
1. ✅ Autenticação em ambas as Orgs
2. ✅ Obtenção da estrutura do SObject
3. ✅ Comparação dos campos e tipos de dados
4. ✅ Análise dos metadados selecionados
5. ✅ Exibição dos resultados

⏱️ Tempo estimado: 10-60 segundos (depende da quantidade de metadados)

---

## 📊 Entendendo os Resultados

### Guia 2: Resultados (Results)

Após a comparação, você verá uma tabela com as seguintes colunas:

| Coluna | Descrição |
|--------|-----------|
| **Category** | Tipo de elemento (SObject, Apex Class, Flow, etc) |
| **Name** | Nome do elemento (ex: Account.Email__c) |
| **Org 1** | Valor/Status na Organização 1 |
| **Org 2** | Valor/Status na Organização 2 |
| **Details** | Detalhes da divergência encontrada |

### Tipos de Resultados

#### ✅ Sem Divergências
Se nenhuma diferença for encontrada:
```
✓ No differences found!
```

#### ❌ Divergências Encontradas
Exemplos:

| Category | Name | Org 1 | Org 2 | Details |
|----------|------|-------|-------|---------|
| SObject | Account.CustomField1__c | Text | MISSING | Field exists only in Org 1 |
| SObject | Account.Phone | Phone | Email | Type mismatch detected |
| Apex Class | ACCOUNT_TriggerHandler | Custom Code | MISSING | Apex class missing in Org 2 |

### Interpretando Divergências Comuns

**Campo Faltante:**
```
Category: SObject
Name: Account.SpecialDiscount__c
Org 1: Number
Org 2: MISSING
```
→ O campo existe em Org 1 mas não em Org 2

**Tipo de Dado Diferente:**
```
Category: SObject
Name: Account.Status
Org 1: Picklist
Org 2: Text
```
→ Mesmo campo, mas tipos de dados diferentes

**Metadado Faltante:**
```
Category: Apex Trigger
Name: ACCOUNT_UpdateHandler
Org 1: Custom Code (Version: 2.0)
Org 2: MISSING
```
→ Trigger existe em Org 1 mas não em Org 2

---

## 💾 Exportando Resultados

1. Clique no botão **"Export to CSV"** (após uma comparação)
2. Escolha o local para salvar o arquivo
3. O arquivo será salvo no formato CSV com todas as divergências

**Arquivo gerado:**
```
comparison_results_1234567890.csv
```

O arquivo pode ser aberto em Excel, Google Sheets ou qualquer editor de texto e facilita o compartilhamento com o time.

---

## ⚙️ Dados de Autenticação Salesforce

### Opção 1: Username/Password + Security Token

**Como obter o Security Token:**
1. Acesse sua Org Salesforce
2. Clique no seu avatar (canto superior direito)
3. Selecione **Settings** → **Personal Setup**
4. Clique em **Reset Your Security Token**
5. Você receberá um email com o novo token

**Na aplicação:**
- Username: seu usuário
- Password: sua senha
- Security Token: o código recebido por email

### Opção 2: OAuth2 (Client ID + Client Secret)

**Como obter as credenciais:**
1. Acesse Setup → Apps → App Manager
2. Clique em **New Connected App**
3. Preencha:
   - App Name: `SalesforceComparator`
   - Contact Email: seu email
   - Abilitar "Enable OAuth Settings"
4. Em "Selected OAuth Scopes", adicione:
   - `api`
   - `refresh_token`
5. Clique em **Save**
6. Copie o **Consumer Key** (Client ID) e **Consumer Secret** (Client Secret)

---

## 🔧 Troubleshooting

### Erro: "Auth failed"
**Causa:** Credenciais incorretas
- Verifique username/password/security token
- Confirme que o usuário tem permissões suficientes
- Teste as credenciais primeiro direto no Salesforce

### Erro: "Connection timeout"
**Causa:** Problema de conexão
- Verifique sua conectividade de internet
- Confirme que a Instance URL está correta
- Verifique permissões de firewall/proxy

### Erro: "SObject not found"
**Causa:** Nome do objeto está incorreto
- Verifique o API Name do objeto (Setup → Objects)
- Objetos customizados terminam em `__c`
- Case-sensitive: `Account` ≠ `account`

### Erro: "Build failed"
**Causa:** Java 21 não encontrado
- Verifique se Java está instalado: `java -version`
- Confirme que Java 21 está no PATH
- Reinicie o terminal após instalar Java

### Aplicação não inicia
**Solução:**
1. Abra PowerShell como Administrador
2. Navegue até a pasta do projeto
3. Execute: `.\build.bat` (para recompilar)
4. Depois: `.\run.bat`

---

## 📈 Casos de Uso

### Caso 1: Verificar Campos Antes de Deploy
```
Org 1 (Dev) → Org 2 (QA)
SObject: Account
Record Type: (deixar vazio)
Metadados: nenhum selecionado
```
Isso verificará se todos os campos personalizados estão alinhados.

### Caso 2: Auditoria Completa
```
Org 1 (Dev) → Org 2 (Prod)
SObject: Opportunity
Record Type: Enterprise
Metadados: ☑ Validation Rules, ☑ Apex Triggers, ☑ Flows
```
Auditoria completa incluindo lógica de negócio.

### Caso 3: Verificar Inconsistência de Sandbox
```
Org 1 (Sandbox 1) → Org 2 (Sandbox 2)
SObject: Contact
Metadados: ☑ Page Layouts, ☑ Profiles, ☑ Permission Sets
```
Garantir que ambos sandboxes têm configurações consistentes.

---

## 💡 Dicas e Boas Práticas

✅ **Use um usuário técnico** com permissões de API em ambas as Orgs

✅ **Inicie com 100 registros** e aumente se necessário

✅ **Salve os resultados** para histórico e auditoria

✅ **Execute regularmente** (ex: antes de sprints de QA)

✅ **Corrija divergências** imediatamente para evitar erros em produção

❌ **Não use credenciais compartilhadas** - crie usuários específicos

❌ **Não ignore as divergências** - investigue sempre

---

## 🔐 Segurança

- ✅ Credenciais não são armazenadas em disco
- ✅ Senhas são mascaradas na interface
- ✅ Comunicação via HTTPS apenas
- ✅ Acesso somente leitura (sem modificações)

**Recomendações:**
- Use senhas fortes
- Não compartilhe credenciais
- Revoque tokens periodicamente
- Use um usuário específico para esta ferramenta

---

## 📞 Suporte

### Erros Frequentes

**"Invalid URL"** → Remova trailing slashes: `https://login.salesforce.com` (não `/`)

**"Unauthorized"** → Verifique permissões do usuário: `API Enabled` na permission set

**"Rate limit exceeded"** → Reduzir `Record Count Limit` ou aguardar alguns minutos

---

## 🚀 Próximas Versões

Funcionalidades planejadas:
- [ ] Implementação completa de comparação de metadados
- [ ] Histórico de comparações
- [ ] Sincronização automática de mudanças
- [ ] Interface Web
- [ ] Integração com CI/CD (Jenkins, GitHub Actions)
- [ ] Relatórios em PDF
- [ ] Notificações via Slack/Email

---

## 📝 Notas Técnicas

- **Linguagem:** Java 21
- **GUI:** Swing (nativa do Java)
- **API:** REST API v59.0 do Salesforce
- **HTTP Client:** Java 11+ HttpClient
- **Dependências:** org.json

---

Última atualização: **26 de Maio de 2026**
