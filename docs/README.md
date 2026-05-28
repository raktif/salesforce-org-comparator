# Salesforce Org Comparator

Uma aplicação Java desktop para comparar metadados e registros entre duas organizações Salesforce diferentes.

## Funcionalidades

- **Comparação de Campos de SObjects**: Verifica campos, tipos de dados e conformidade entre ambas as Orgs
- **Comparação de Metadados**: Suporta comparação de:
  - Page Layouts
  - Validation Rules
  - Flows (Record Triggered)
  - Apex Triggers
  - Custom Metadata
  - Custom Settings
  - Permission Sets
  - Profiles
  - Groups
  - Approval Processes
  - Named Credentials

- **Interface Gráfica Amigável**: Formulários intuitivos para entrada de dados de conexão
- **Relatório Detalhado**: Tabelas com resultados das comparações
- **Exportação em CSV**: Exportar resultados para análise posterior

## Requisitos

- Java 21 ou superior
- Acesso à API REST do Salesforce em ambas as Orgs
- Credenciais de autenticação OAuth2 ou username/password com security token

## Instalação

1. Navegue até a pasta do projeto:
   ```
   cd C:\SalesforceComparator
   ```

2. Execute o build script:
   ```
   build.bat
   ```

   Ou no Linux/Mac:
   ```
   bash build.sh
   ```

## Como Usar

1. Execute a aplicação:
   ```
   run.bat
   ```
   
   Ou alternativamente:
   ```
   java -cp "build;lib\*" com.sfcomparator.ui.MainWindow
   ```

2. **Configurar Organização 1 (Source)**:
   - Instance URL: URL de acesso à Org 1 (ex: https://login.salesforce.com)
   - Username: Usuário da Org 1
   - Password: Senha da Org 1
   - Security Token: Token de segurança (se necessário)
   - Client ID e Client Secret: Para autenticação OAuth2 (opcional)

3. **Configurar Organização 2 (Target)**:
   - Repita o processo anterior com as credenciais da Org 2

4. **Configurar Comparação**:
   - SObject Name: Nome do objeto a ser comparado (ex: Account, Contact)
   - Record Count Limit: Quantidade máxima de registros a verificar
   - Record Type: Type de registro específico (opcional)
   - Selecionar metadados desejados via checkboxes

5. **Iniciar Comparação**:
   - Clique no botão "Start Comparison"
   - Os resultados aparecerão na aba "Results"

6. **Exportar Resultados**:
   - Clique em "Export to CSV" para salvar os resultados

## Estrutura do Projeto

```
SalesforceComparator/
├── src/main/java/com/sfcomparator/
│   ├── api/              # Cliente API Salesforce
│   ├── model/            # Modelos de dados
│   ├── ui/               # Interface gráfica (Swing)
│   └── comparator/       # Engine de comparação
├── lib/                  # Dependências externas
├── build/                # Arquivos compilados
├── dist/                 # JAR executável
├── build.bat             # Script de compilação (Windows)
├── build.sh              # Script de compilação (Linux/Mac)
├── run.bat               # Script de execução (Windows)
└── README.md             # Este arquivo
```

## Dependências

- `json-20231013.jar`: Biblioteca para processamento de JSON (já incluída em `lib/`)

## Autenticação Salesforce

A aplicação suporta dois métodos de autenticação:

### 1. Username/Password com Security Token
- Username: Usuário da Org
- Password: Senha da Org
- Security Token: Obtido em Setup > Personal Setup > Reset Your Security Token

### 2. OAuth2
- Client ID: Obtido em Setup > Apps > App Manager > Connected App
- Client Secret: Obtido na mesma Connected App

## Notas Importantes

- A aplicação foi desenvolvida utilizando apenas as APIs padrão do Java 21 e não requer instalação de dependências externas (exceto a biblioteca JSON fornecida)
- Os dados de autenticação são usados apenas para a sessão atual e não são armazenados no disco
- Recomenda-se usar um usuário técnico com permissões adequadas em ambas as Orgs
- A comparação pode levar alguns minutos dependendo da quantidade de metadados

## Troubleshooting

### Erro de Autenticação
- Verifique se as credenciais estão corretas
- Confirme se o Security Token foi adicionado corretamente à senha
- Valide a Instance URL (deve ser `https://login.salesforce.com` ou `https://test.salesforce.com`)

### Erro ao compilar
- Confirme que Java 21 está instalado: `java -version`
- Verifique o caminho do JAVA_HOME no script build.bat

### Falha de Conexão
- Verifique conectividade com a internet
- Confirme permissões de firewall
- Valide que a API REST está habilitada na Org

## Desenvolvimento Futuro

- [ ] Suporte a comparação completa de todos os metadados
- [ ] Sincronização automática de mudanças
- [ ] Histórico de comparações
- [ ] Integração com Git para tracking de mudanças
- [ ] Interface Web
- [ ] Suporte a deployments automáticos

## Licença

Proprietário - Uso interno apenas

## Suporte

Para relatar bugs ou sugerir melhorias, entre em contato com o time de desenvolvimento.
