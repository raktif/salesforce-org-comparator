# 📚 ÍNDICE DE DOCUMENTAÇÃO

**Salesforce Org Comparator v1.0**

---

## 📖 Documentos Disponíveis

### 🚀 Começar Aqui
1. **[SETUP_RAPIDO.md](SETUP_RAPIDO.md)** ⭐ **LEIA PRIMEIRO**
   - Guia passo-a-passo em 5 minutos
   - Como obter credenciais Salesforce
   - Primeiros testes
   - FAQ rápido

### 📘 Manuais Principais
2. **[GUIA_DE_USO.md](GUIA_DE_USO.md)**
   - Interface completa explicada
   - Exemplos de uso
   - Interpretação de resultados
   - Casos de uso reais
   - Troubleshooting detalhado

3. **[README.md](README.md)**
   - Visão geral do projeto
   - Funcionalidades
   - Instalação
   - Estrutura do projeto
   - Como compilar manualmente

### 🔧 Documentação Técnica
4. **[DOCUMENTACAO_TECNICA.md](DOCUMENTACAO_TECNICA.md)**
   - Arquitetura do sistema
   - Implementação de novas funcionalidades
   - Detalhes da API Salesforce
   - Modelos de dados
   - Como testar

### 📦 Entrega
5. **[ENTREGA.md](ENTREGA.md)**
   - O que foi entregue
   - Checklist de funcionalidades
   - Status final
   - Requisitos técnicos

### 📋 Este Arquivo
6. **[INDICE.md](INDICE.md)** (você está aqui)
   - Mapa de documentação
   - Onde encontrar cada informação

---

## 🎯 Navegação por Perfil

### 👥 Sou um Usuário Final (Líder Técnico)
1. Comece com: **SETUP_RAPIDO.md**
2. Depois leia: **GUIA_DE_USO.md**
3. Se tiver dúvidas: Veja FAQ em GUIA_DE_USO.md

### 👨‍💻 Sou um Desenvolvedor
1. Comece com: **README.md**
2. Estude: **DOCUMENTACAO_TECNICA.md**
3. Explore: Código-fonte em `src/main/java/`
4. Para expandir: Veja "Como Implementar Novos Metadados"

### 🏢 Sou um Gerente/Lider de Projeto
1. Leia: **ENTREGA.md** (status do projeto)
2. Depois: **README.md** (contexto geral)
3. Para detalhes: **GUIA_DE_USO.md**

---

## 🔍 Encontrar Respostas Rápidas

### "Como comece?"
→ **SETUP_RAPIDO.md** - Primeiros passos

### "Como autentico com Salesforce?"
→ **SETUP_RAPIDO.md** - Seção "Obter Credenciais"
→ **GUIA_DE_USO.md** - Seção "Dados de Autenticação"

### "Como uso a interface?"
→ **GUIA_DE_USO.md** - Seção "Interface da Aplicação"

### "Como entendo os resultados?"
→ **GUIA_DE_USO.md** - Seção "Entendendo os Resultados"

### "Como exporto resultados?"
→ **GUIA_DE_USO.md** - Seção "Exportando Resultados"

### "Como compilo o projeto?"
→ **README.md** - Seção "Instalação"
→ **DOCUMENTACAO_TECNICA.md** - Seção "Build Script"

### "Como estendo a ferramenta?"
→ **DOCUMENTACAO_TECNICA.md** - Seção "Como Implementar Novos Metadados"

### "Qual é a arquitetura?"
→ **DOCUMENTACAO_TECNICA.md** - Seção "Arquitetura"

### "Há um roadmap?"
→ **DOCUMENTACAO_TECNICA.md** - Seção "Próximos Passos"

### "Como debugo problemas?"
→ **DOCUMENTACAO_TECNICA.md** - Seção "Debug"
→ **GUIA_DE_USO.md** - Seção "Troubleshooting"

---

## 📊 Estrutura de Pastas Explicada

```
SalesforceComparator/
│
├── 📄 Documentação (LEIA ESTES)
│   ├── SETUP_RAPIDO.md ..................... 🌟 Começar aqui
│   ├── GUIA_DE_USO.md ...................... Manual completo
│   ├── README.md ........................... Visão geral
│   ├── DOCUMENTACAO_TECNICA.md ............ Para desenvolvedores
│   ├── ENTREGA.md .......................... Status do projeto
│   └── INDICE.md ........................... Este arquivo
│
├── 🖥️ Executáveis (RUN ESTES)
│   ├── run.bat ............................. ⭐ Iniciar aplicação
│   └── build.bat ........................... Recompilar (se necessário)
│
├── 📦 Distribuição (USAR ESTES)
│   ├── dist/
│   │   └── SalesforceComparator.jar ....... JAR executável
│   └── lib/
│       └── json-20231013.jar ............. Dependência
│
├── 💻 Código-Fonte (EDITE ESTES)
│   └── src/main/java/com/sfcomparator/
│       ├── api/ ........................... Cliente Salesforce
│       ├── model/ ......................... Modelos de dados
│       ├── comparator/ ................... Engine de comparação
│       └── ui/ ........................... Interface gráfica
│
└── 🔨 Compilação (IGNORE ESTES)
    └── build/ ............................ Arquivos compilados
```

---

## ✅ Checklist de Leitura Recomendada

### Antes de Usar a Ferramenta
- [ ] Leia SETUP_RAPIDO.md (5 min)
- [ ] Obtenha credenciais Salesforce (10 min)
- [ ] Execute run.bat e faça primeiro teste (5 min)

### Antes de Usar Regularmente
- [ ] Leia GUIA_DE_USO.md (20 min)
- [ ] Entenda os tipos de resultados (5 min)
- [ ] Teste exportação CSV (5 min)

### Antes de Personalizar/Estender
- [ ] Leia README.md (10 min)
- [ ] Estude DOCUMENTACAO_TECNICA.md (30 min)
- [ ] Explore código-fonte (1-2 horas)

---

## 🔗 Links Rápidos

**Executar agora:**
```
.\run.bat
```

**Compilar manualmente:**
```
.\build.bat
java -cp "build;lib\*" com.sfcomparator.ui.MainWindow
```

**Abrir arquivo de configuração:**
```
Windows: start SETUP_RAPIDO.md
Linux: xdg-open SETUP_RAPIDO.md
Mac: open SETUP_RAPIDO.md
```

---

## 📞 Perguntas Frequentes por Documento

### SETUP_RAPIDO.md
- Como instalar?
- Como criar credenciais?
- Como fazer primeiro teste?
- O que fazer se houver erro?

### GUIA_DE_USO.md
- Como funciona a interface?
- Como interpretar resultados?
- Como exportar?
- Quais são os casos de uso?
- Dicas e boas práticas?

### README.md
- O que é este software?
- Qual é a estrutura?
- Como compilar?
- Quais são os requisitos?

### DOCUMENTACAO_TECNICA.md
- Como a arquitetura funciona?
- Como estender o software?
- Qual é o roadmap?
- Como debugar?

### ENTREGA.md
- O que foi entregue?
- Qual é o status?
- Quais são os requisitos técnicos?

---

## 🎓 Exemplo de Leitura Completa

**Novo Usuário - Primeira Vez (Total: 30-45 minutos)**

1. SETUP_RAPIDO.md (5 min)
   ↓ Obter credenciais
   ↓ Executar aplicação
   ↓ Fazer primeiro teste

2. GUIA_DE_USO.md (20 min)
   ↓ Entender interface
   ↓ Aprender a usar
   ↓ Conhecer casos de uso

3. FAQ (10 min)
   ↓ Resolver dúvidas
   ↓ Dicas de uso

**Resultado:** Pronto para usar diariamente ✅

---

## 🚀 Próximas Ações

1. **Comece:** `.\run.bat`
2. **Leia:** SETUP_RAPIDO.md
3. **Teste:** Fazer primeira comparação
4. **Aprenda:** GUIA_DE_USO.md completo
5. **Use:** Integrar na rotina de QA

---

**Última atualização:** 26 de Maio de 2026
**Versão:** 1.0 MVP
**Status:** ✅ Pronto para Uso

---

**Comece agora: Abra SETUP_RAPIDO.md e execute `run.bat` 🚀**
