package com.sfcomparator.ui;

import java.text.MessageFormat;
import java.util.*;

/**
 * Gerenciador de idiomas da interface.
 * Suporta Português (Brasil) e English.
 * Notifica todos os componentes registrados quando o idioma é alterado.
 */
public class LanguageManager {

    public enum Language { PT_BR, EN }

    private static Language current = Language.PT_BR;
    private static final List<Runnable> listeners = new ArrayList<>();

    // Cada entrada: String[]{ texto em PT_BR, texto em EN }
    private static final Map<String, String[]> S = new LinkedHashMap<>();

    static {
        // ── Aplicação ──────────────────────────────────────────────────────
        a("app.title",        "Salesforce Org Comparator - v1.1",      "Salesforce Org Comparator - v1.1");
        a("header.title",     "Salesforce Org Comparator - v1.1",      "Salesforce Org Comparator - v1.1");

        // ── Abas ───────────────────────────────────────────────────────────
        a("tab.config",       "Configuração",    "Configuration");
        a("tab.compareOrgs",  "Comparar Orgs",   "Compare Orgs");
        a("tab.compareRepo",  "Comparar Repositório", "Compare Repository");
        a("tab.results",      "Resultados",      "Results");

        // ── Toolbar ────────────────────────────────────────────────────────
        a("toolbar.save",     "Salvar",          "Save");
        a("toolbar.load",     "Carregar",        "Load");
        a("toolbar.lang",     "Alterar Idioma - English",       "Switch language - Português (Brasil)");
        a("toolbar.help",     "Ajuda",           "Help");

        // ── Botão de comparação ────────────────────────────────────────────
        a("btn.compare",      "Iniciar Comparação",  "Start Comparison");
        a("btn.comparing",    "Comparando...",        "Comparing...");

        // ── Painel de conexão ──────────────────────────────────────────────
        a("org1.title",       "Organização 1 (Origem)",   "Organization 1 (Source)");
        a("org2.title",       "Organização 2 (Destino)",  "Organization 2 (Target)");
        a("lbl.instanceUrl",  "Instance URL:",            "Instance URL:");
        a("lbl.securityToken","Token de Seguran\u00e7a:",     "Security Token:");
        a("lbl.clientId",     "Chave do Consumidor (OAuth):", "Client ID (OAuth):");
        a("lbl.clientSecret", "Segredo do Consumidor (OAuth):","Client Secret (OAuth):");
        a("tip.instanceUrl",  "Ex: https://login.salesforce.com ou https://sua-org.my.salesforce.com",
                              "E.g., https://login.salesforce.com or https://your-org.my.salesforce.com");
        a("tip.securityToken","Utilize o Token de Seguran\u00e7a do usu\u00e1rio selecionado no ambiente",
                              "Use the Security Token of the selected user in the environment");
        a("tip.clientId",     "Para autenticação OAuth", "For OAuth authentication");
        a("tip.clientSecret", "Para autenticação OAuth", "For OAuth authentication");

        // ── Painel de configurações do projeto ─────────────────────────────
        a("project.title",   "Configurações do Projeto",    "Project Settings");
        a("lbl.localPath",   "Endereço local do projeto:",  "Local project path:");
        a("tip.localPath",   "Exemplo: C:\\nomeDoProjeto",  "Example: C:\\myProject");

        // ── Painel de configuração ─────────────────────────────────────────
        a("config.title",           "Configurações de Comparação",                           "Comparison Settings");
        a("lbl.sobject",            "SObject Name (API):",                                   "SObject Name (API):");
        a("tip.sobject",            "Ex: Account, Contact, Opportunity, MyObject__c",        "E.g.: Account, Contact, Opportunity, MyObject__c");
        a("config.section.object",  "Metadados vinculados ao Objeto (requerem SObject Name):", "Object-Bound Metadata (require SObject Name):");
        a("config.section.org",     "Metadados da Org (independentes do objeto — compara TODOS):", "Org-Level Metadata (object-independent — compares ALL):");

        // Checkboxes — PT-BR usa a terminologia oficial da interface Salesforce em português
        a("chk.apexTriggers",      "Apex Triggers",                        "Apex Triggers");
        a("chk.objectFields",      "Campos do Objeto",                     "Object Fields");
        a("chk.flows",             "Fluxos (Acionados por Registro)",      "Flows (Record-Triggered)");
        a("chk.pageLayouts",       "Layouts de P\u00e1gina",               "Page Layouts");
        a("chk.recordTypes",       "Tipos de Registro",                    "Record Types");
        a("chk.validationRules",   "Regras de Valida\u00e7\u00e3o",        "Validation Rules");
        a("chk.approvalProcesses", "Processos de Aprova\u00e7\u00e3o",     "Approval Processes");
        a("chk.customMetadata",    "Tipos de Metadados Personalizados",    "Custom Metadata Types");
        a("chk.customSettings",    "Configura\u00e7\u00f5es Personalizadas","Custom Settings");
        a("chk.namedCredentials",  "Credenciais Nomeadas",                 "Named Credentials");
        a("chk.permissionSets",    "Conjuntos de Permiss\u00f5es",         "Permission Sets");
        a("chk.profiles",          "Perfis",                               "Profiles");
        a("chk.publicGroups",      "Grupos P\u00FAblicos",                 "Public Groups");
        a("tip.permSetsFilter",
            "Filtre por nome ou label (separe múltiplos valores por vírgula). Ex: Admin, Sales Rep. Deixe em branco para comparar TODOS.",
            "Filter by name or label (separate multiple values with commas). E.g.: Admin, Sales Rep. Leave blank to compare ALL.");
        a("tip.profilesFilter",
            "Filtre por nome do perfil (separe múltiplos valores por vírgula). Ex: System Administrator, Sales. Deixe em branco para comparar TODOS.",
            "Filter by profile name (separate multiple values with commas). E.g.: System Administrator, Sales. Leave blank to compare ALL.");

        // ── Painel de resultados ───────────────────────────────────────────
        a("results.border",  "Resultados da Comparação", "Comparison Results");
        a("col.category",    "Categoria",   "Category");
        a("col.name",        "Nome",        "Name");
        a("col.org1",        "Org 1",       "Org 1");
        a("col.org2",        "Org 2",       "Org 2");
        a("col.details",     "Detalhes",    "Details");
        a("status.ready",    "Pronto para comparar",             "Ready to compare");
        a("status.noDiff",   "✓ Nenhuma diferença encontrada!",  "✓ No differences found!");
        a("status.found",    "✗ {0} diferença(s) encontrada(s)", "✗ Found {0} difference(s)");
        a("btn.export",      "Exportar para CSV", "Export to CSV");
        a("btn.clear",       "Limpar",            "Clear");
        a("filter.title",    "Filtros",           "Filters");
        a("filter.all",      "(Todos)",           "(All)");
        a("btn.clearFilters","Limpar filtros",    "Clear filters");
        a("details.title",   "Detalhes: ",        "Details: ");

        // ── Categorias e status da tabela de resultados ────────────────────
        // Usados por ResultsPanel.translateCategory() / translateStatus()
        a("cat.objectFields",      "Campos do Objeto",                  "Object Fields");
        a("cat.pageLayouts",       "Layouts de P\u00e1gina",            "Page Layouts");
        a("cat.validationRules",   "Regras de Valida\u00e7\u00e3o",     "Validation Rules");
        a("cat.flows",             "Fluxos",                            "Flows");
        a("cat.apexTriggers",      "Apex Triggers",                     "Apex Triggers");
        a("cat.recordTypes",       "Tipos de Registro",                 "Record Types");
        a("cat.customMetadata",    "Tipos de Metadados Personalizados", "Custom Metadata Types");
        a("cat.customSettings",    "Configura\u00e7\u00f5es Personalizadas", "Custom Settings");
        a("cat.permissionSets",    "Conjuntos de Permiss\u00f5es",      "Permission Sets");
        a("cat.profiles",          "Perfis",                            "Profiles");
        a("cat.publicGroups",      "Grupos P\u00FAblicos",              "Public Groups");
        a("cat.approvalProcesses", "Processos de Aprova\u00e7\u00e3o", "Approval Processes");
        a("cat.namedCredentials",  "Credenciais Nomeadas",              "Named Credentials");
        // Status nas colunas Org 1 / Org 2
        a("val.present",    "Presente",   "Present");
        a("val.absent",     "Ausente",    "Absent");
        a("val.divergent",  "Divergente", "Divergent");

        // ── Progresso do ComparisonEngine ──────────────────────────────────
        a("progress.fields",          "Comparando campos do objeto ''{0}''...",
                                      "Comparing fields for object ''{0}''...");
        a("progress.pageLayouts",     "Verificando Layouts de P\u00e1gina...",
                                      "Verifying Page Layouts...");
        a("progress.pageLayoutsMeta", "Verificando Layouts de P\u00e1gina \u2014 recuperando metadados...",
                                      "Verifying Page Layouts \u2014 retrieving metadata...");
        a("progress.validationRules", "Verificando Regras de Valida\u00e7\u00e3o...",
                                      "Verifying Validation Rules...");
        a("progress.flows",           "Verificando Fluxos...",                 "Verifying Flows...");
        a("progress.apexTriggers",    "Verificando Apex Triggers...",          "Verifying Apex Triggers...");
        a("progress.recordTypes",     "Verificando Tipos de Registro...",      "Verifying Record Types...");
        a("progress.recordTypesMeta", "Verificando Tipos de Registro \u2014 recuperando metadados...",
                                      "Verifying Record Types \u2014 retrieving metadata...");
        a("progress.customMetadata",  "Verificando Tipos de Metadados Personalizados...",
                                      "Verifying Custom Metadata Types...");
        a("progress.customSettings",  "Verificando Configura\u00e7\u00f5es Personalizadas...",
                                      "Verifying Custom Settings...");
        a("progress.permSets",        "Verificando Conjuntos de Permiss\u00f5es...",
                                      "Verifying Permission Sets...");
        a("progress.permSetsMeta",    "Verificando Conjuntos de Permiss\u00f5es \u2014 recuperando metadados...",
                                      "Verifying Permission Sets \u2014 retrieving metadata...");
        a("progress.profiles",        "Verificando Perfis...",                 "Verifying Profiles...");
        a("progress.profilesMeta",    "Verificando Perfis \u2014 recuperando metadados da Org1 e Org2 em paralelo...",
                                      "Verifying Profiles \u2014 retrieving Org1 and Org2 metadata in parallel...");
        a("progress.groups",          "Verificando Grupos P\u00FAblicos...",   "Verifying Public Groups...");
        a("progress.approvalProcesses","Verificando Processos de Aprova\u00e7\u00e3o...",
                                       "Verifying Approval Processes...");
        a("progress.namedCredentials","Verificando Credenciais Nomeadas...",   "Verifying Named Credentials...");

        // ── Diálogo de progresso ───────────────────────────────────────────
        a("dlg.progress.title",    "Verificação em Andamento",      "Verification in Progress");
        a("dlg.cancel",            "Cancelar",                      "Cancel");
        a("dlg.status.startAuth",  "Iniciando autenticação...",     "Starting authentication...");
        a("dlg.status.authOrg1",   "Autenticando Org 1...",         "Authenticating Org 1...");
        a("dlg.status.authOrg2",   "Autenticando Org 2...",         "Authenticating Org 2...");
        a("dlg.status.cli",        "Conectando ao Salesforce CLI...","Connecting to Salesforce CLI...");
        a("dlg.status.cancelling", "Cancelando...",                 "Cancelling...");

        // ── Mensagens de conclusão ─────────────────────────────────────────
        a("dlg.done.title",  "Concluído",   "Completed");
        a("msg.noDiff",      "Comparação concluída. Nenhuma diferença encontrada!",
                             "Comparison completed. No differences found!");
        a("msg.foundDiff",   "Comparação concluída. {0} diferença(s) encontrada(s).",
                             "Comparison completed. {0} difference(s) found.");

        // ── Validações ─────────────────────────────────────────────────────
        a("val.nothingSelected", "Informe o SObject Name e/ou marque ao menos um tipo de metadado para comparar.",
                                 "Please enter a SObject Name and/or select at least one metadata type to compare.");
        a("val.nothingTitle",    "Nada a Comparar",      "Nothing to Compare");
        a("val.noSobjectMsg",
            "Os metadados vinculados ao Objeto (Page Layouts, Validation Rules, Flows, Apex Triggers, Record Types)\n"
          + "serão ignorados pois o campo 'SObject Name (API)' está vazio.\n\n"
          + "Deseja prosseguir comparando apenas os metadados da Org?",
            "Object-bound metadata (Page Layouts, Validation Rules, Flows, Apex Triggers, Record Types)\n"
          + "will be ignored because 'SObject Name (API)' is empty.\n\n"
          + "Do you want to proceed comparing only Org-level metadata?");
        a("val.noSobjectTitle",   "SObject Name não informado", "SObject Name Not Provided");
        a("val.instanceRequired", "Preencha a Instance URL de ambas as organizações.",
                                  "Please fill in the Instance URL for both organizations.");
        a("val.clientIdRequired", "Preencha o Client ID (OAuth) de ambas as organizações.",
                                  "Please fill in the Client ID (OAuth) for both organizations.");
        a("val.requiredField",    "Campo Obrigatório",  "Required Field");

        // ── Configurações ──────────────────────────────────────────────────
        a("dlg.save.title",    "Salvar Configurações",  "Save Settings");
        a("dlg.load.title",    "Carregar Configurações","Load Settings");
        a("dlg.sfcs.filter",   "Salesforce Comparator Settings (*.sfcs)", "Salesforce Comparator Settings (*.sfcs)");
        a("msg.saved",         "Configurações salvas com sucesso em:\n{0}",  "Settings saved successfully to:\n{0}");
        a("msg.savedTitle",    "Configurações Salvas",  "Settings Saved");
        a("msg.loaded",        "Configurações carregadas com sucesso!", "Settings loaded successfully!");
        a("msg.loadedTitle",   "Configurações Carregadas", "Settings Loaded");
        a("msg.saveError",     "Erro ao salvar configurações:\n{0}",  "Error saving settings:\n{0}");
        a("msg.loadErrorTitle","Erro ao Carregar",      "Load Error");

        // ── Exportação ─────────────────────────────────────────────────────
        a("export.noResults",  "Sem resultados para exportar.", "No results to export.");
        a("export.info",       "Informação",   "Info");
        a("export.success",    "Resultados exportados com sucesso!", "Results exported successfully!");
        a("export.error",      "Erro ao exportar: {0}", "Error exporting: {0}");

        // ── Ajuda / Idioma ─────────────────────────────────────────────────
        a("help.title",  "Manual do Usuário",    "User Manual");
        a("btn.close",   "Fechar",              "Close");

        // ── Erros ──────────────────────────────────────────────────────────
        a("err.comparison", "Erro na Compara\u00e7\u00e3o", "Comparison Error");
        a("err.title",      "Erro",               "Error");

        // ── Aba Comparar Reposit\u00f3rio ────────────────────────────────────────
        a("lbl.repo.selectMetadata",
            "Escolha quais metadados comparar do seu reposit\u00f3rio local com sua org:",
            "Choose which metadata to compare from your local repository with your org:");

        // Tooltips dos checkboxes — nomenclatura oficial Salesforce (PT-BR / EN)
        a("tip.repo.ApexClass",                            "Classes Apex",                                              "Apex Classes");
        a("tip.repo.ApexComponent",                        "Componentes Visualforce",                                   "Visualforce Components");
        a("tip.repo.ApexPage",                             "P\u00e1ginas Visualforce",                                  "Visualforce Pages");
        a("tip.repo.ApexTestSuite",                        "Su\u00edtes de Teste Apex",                                 "Apex Test Suites");
        a("tip.repo.ApexTrigger",                          "Apex Triggers",                                             "Apex Triggers");
        a("tip.repo.ApprovalProcess",                      "Processos de Aprova\u00e7\u00e3o",                          "Approval Processes");
        a("tip.repo.AssignmentRule",                       "Regra de Atribui\u00e7\u00e3o",                             "Assignment Rule");
        a("tip.repo.AssignmentRules",                      "Regras de Atribui\u00e7\u00e3o",                            "Assignment Rules");
        a("tip.repo.AuraDefinitionBundle",                 "Componentes Aura",                                          "Aura Components");
        a("tip.repo.AuthProvider",                         "Provedores de Autentica\u00e7\u00e3o",                       "Auth Providers");
        a("tip.repo.Bot",                                  "Bots",                                                      "Bots");
        a("tip.repo.BrandingSet",                          "Conjuntos de Identidade Visual",                            "Branding Sets");
        a("tip.repo.CompactLayout",                        "Layouts Compactos",                                         "Compact Layouts");
        a("tip.repo.ConnectedApp",                         "Aplicativos Conectados",                                    "Connected Apps");
        a("tip.repo.ContentAsset",                         "Ativos de Conte\u00fado",                                   "Content Assets");
        a("tip.repo.CustomApplication",                    "Aplicativos Personalizados",                                "Custom Applications");        a("tip.repo.CustomIndex",                          "Índices Personalizados",                                   "Custom Indexes");        a("tip.repo.CustomLabel",                          "R\u00f3tulo Personalizado",                                 "Custom Label");
        a("tip.repo.CustomLabels",                         "R\u00f3tulos Personalizados",                               "Custom Labels");
        a("tip.repo.CustomMetadata",                       "Tipos de Metadados Personalizados",                         "Custom Metadata Types");
        a("tip.repo.CustomNotificationType",               "Tipos de Notifica\u00e7\u00e3o Personalizados",             "Custom Notification Types");
        a("tip.repo.CustomObject",                         "Objetos Personalizados",                                    "Custom Objects");
        a("tip.repo.CustomObjectTranslation",              "Tradu\u00e7\u00f5es de Objetos Personalizados",             "Custom Object Translations");
        a("tip.repo.CustomPermission",                     "Permiss\u00f5es Personalizadas",                            "Custom Permissions");
        a("tip.repo.CustomTab",                            "Guias Personalizadas",                                      "Custom Tabs");
        a("tip.repo.Dashboard",                            "Pain\u00e9is",                                              "Dashboards");
        a("tip.repo.DataCategoryGroup",                    "Grupos de Categorias de Dados",                             "Data Category Groups");
        a("tip.repo.DuplicateRule",                        "Regras de Duplicidade",                                     "Duplicate Rules");
        a("tip.repo.EclairGeoData",                        "Dados Geogr\u00e1ficos Eclair",                             "Eclair Geo Data");
        a("tip.repo.EmailServicesFunction",                "Servi\u00e7os de E-mail",                                   "Email Services");
        a("tip.repo.EmailTemplate",                        "Modelos de E-mail",                                         "Email Templates");
        a("tip.repo.ExternalClientApplication",            "Aplica\u00e7\u00f5es Cliente Externas",                     "External Client Applications");
        a("tip.repo.ExternalCredential",                   "Credenciais Externas",                                      "External Credentials");
        a("tip.repo.ExtlClntAppConfigurablePolicies",      "Pol\u00edticas Configur\u00e1veis de App Cliente Externo",   "External Client App Configurable Policies");
        a("tip.repo.ExtlClntAppGlobalOauthSettings",       "Configura\u00e7\u00f5es OAuth Globais de App Cliente Externo", "External Client App Global OAuth Settings");
        a("tip.repo.ExtlClntAppOauthConfigurablePolicies", "Pol\u00edticas OAuth Configur\u00e1veis de App Cliente Externo", "External Client App OAuth Configurable Policies");
        a("tip.repo.ExtlClntAppOauthSettings",             "Configura\u00e7\u00f5es OAuth de App Cliente Externo",      "External Client App OAuth Settings");
        a("tip.repo.FieldSet",                             "Conjuntos de Campos",                                       "Field Sets");
        a("tip.repo.FlexiPage",                            "P\u00e1ginas Lightning",                                    "Lightning Pages");
        a("tip.repo.Flow",                                 "Fluxos",                                                    "Flows");
        a("tip.repo.FlowDefinition",                       "Defini\u00e7\u00f5es de Fluxo",                             "Flow Definitions");
        a("tip.repo.FlowTest",                             "Testes de Fluxo",                                           "Flow Tests");
        a("tip.repo.GenAiFunction",                        "Fun\u00e7\u00f5es de IA Generativa",                        "Generative AI Functions");
        a("tip.repo.GenAiPlannerBundle",                   "Pacotes de Planejamento de IA Generativa",                  "Generative AI Planner Bundles");
        a("tip.repo.GenAiPromptTemplate",                  "Modelos de Prompt de IA Generativa",                        "Generative AI Prompt Templates");
        a("tip.repo.GlobalValueSet",                       "Conjuntos de Valores Globais",                              "Global Value Sets");
        a("tip.repo.GlobalValueSetTranslation",            "Tradu\u00e7\u00f5es de Conjuntos de Valores Globais",       "Global Value Set Translations");
        a("tip.repo.Group",                                "Grupos P\u00FAblicos",                                      "Public Groups");
        a("tip.repo.Layout",                               "Layouts de P\u00e1gina",                                    "Page Layouts");
        a("tip.repo.LeadConvertSettings",                  "Configura\u00e7\u00f5es de Convers\u00e3o de Lead",         "Lead Convert Settings");
        a("tip.repo.Letterhead",                           "Timbres",                                                   "Letterheads");
        a("tip.repo.LightningComponentBundle",             "Lightning Web Components",                                  "Lightning Web Components");
        a("tip.repo.LightningExperienceTheme",             "Temas da Lightning Experience",                             "Lightning Experience Themes");
        a("tip.repo.LightningMessageChannel",              "Canais de Mensagem Lightning",                              "Lightning Message Channels");
        a("tip.repo.ListView",                             "Visualiza\u00e7\u00f5es de Lista",                          "List Views");
        a("tip.repo.MatchingRule",                         "Regra de Correspond\u00eancia",                             "Matching Rule");
        a("tip.repo.MatchingRules",                        "Regras de Correspond\u00eancia",                            "Matching Rules");
        a("tip.repo.NamedCredential",                      "Credenciais Nomeadas",                                      "Named Credentials");
        a("tip.repo.NotificationTypeConfig",               "Configura\u00e7\u00f5es de Tipos de Notifica\u00e7\u00e3o", "Notification Type Configurations");
        a("tip.repo.OmniDataTransform",                    "Transforma\u00e7\u00f5es de Dados OmniStudio",              "OmniStudio Data Transforms");
        a("tip.repo.OmniIntegrationProcedure",             "Procedimentos de Integra\u00e7\u00e3o OmniStudio",          "OmniStudio Integration Procedures");
        a("tip.repo.OmniInteractionConfig",                "Configura\u00e7\u00f5es de Intera\u00e7\u00e3o OmniStudio", "OmniStudio Interaction Configurations");
        a("tip.repo.OmniScript",                           "Scripts OmniStudio",                                        "OmniStudio Scripts");
        a("tip.repo.OmniUiCard",                           "Cards de Interface OmniStudio",                             "OmniStudio UI Cards");
        a("tip.repo.ParticipantRole",                      "Fun\u00e7\u00f5es de Participante",                         "Participant Roles");
        a("tip.repo.PathAssistant",                        "Assistentes de Caminho (Path)",                             "Path Assistants");
        a("tip.repo.PermissionSet",                        "Conjuntos de Permiss\u00f5es",                              "Permission Sets");
        a("tip.repo.PermissionSetGroup",                   "Grupos de Conjuntos de Permiss\u00f5es",                    "Permission Set Groups");
        a("tip.repo.PlatformCachePartition",               "Parti\u00e7\u00f5es de Cache da Plataforma",                "Platform Cache Partitions");
        a("tip.repo.PlatformEventChannelMember",           "Membros de Canal de Evento da Plataforma",                  "Platform Event Channel Members");
        a("tip.repo.PostTemplate",                         "Modelos de Publica\u00e7\u00e3o",                           "Post Templates");
        a("tip.repo.PresenceUserConfig",                   "Configura\u00e7\u00f5es de Usu\u00e1rio de Presen\u00e7a",  "Presence User Configurations");
        a("tip.repo.Profile",                              "Perfis",                                                    "Profiles");
        a("tip.repo.ProfilePasswordPolicy",                "Pol\u00edticas de Senha de Perfil",                         "Profile Password Policies");
        a("tip.repo.ProfileSessionSetting",                "Configura\u00e7\u00f5es de Sess\u00e3o de Perfil",          "Profile Session Settings");
        a("tip.repo.Prompt",                               "Prompts",                                                   "Prompts");
        a("tip.repo.Queue",                                "Filas",                                                     "Queues");
        a("tip.repo.QueueRoutingConfig",                   "Configura\u00e7\u00f5es de Roteamento de Fila",             "Queue Routing Configurations");
        a("tip.repo.QuickAction",                          "A\u00e7\u00f5es R\u00e1pidas",                              "Quick Actions");
        a("tip.repo.RecordAlertCategory",                  "Categorias de Alertas de Registro",                         "Record Alert Categories");
        a("tip.repo.RecordType",                           "Tipos de Registro",                                         "Record Types");
        a("tip.repo.RemoteSiteSetting",                    "Configura\u00e7\u00f5es de Site Remoto",                    "Remote Site Settings");
        a("tip.repo.Report",                               "Relat\u00f3rios",                                           "Reports");
        a("tip.repo.ReportType",                           "Tipos de Relat\u00f3rio",                                   "Report Types");
        a("tip.repo.Role",                                 "Fun\u00e7\u00f5es (Hierarquia)",                            "Roles");
        a("tip.repo.ServiceChannel",                       "Canais de Servi\u00e7o",                                    "Service Channels");
        a("tip.repo.Settings",                             "Configura\u00e7\u00f5es da Org",                            "Org Settings");
        a("tip.repo.SharingRules",                         "Regras de Compartilhamento",                                "Sharing Rules");
        a("tip.repo.StandardValueSetTranslation",          "Tradu\u00e7\u00f5es de Conjuntos de Valores Padr\u00e3o",   "Standard Value Set Translations");
        a("tip.repo.StaticResource",                       "Recursos Est\u00e1ticos",                                   "Static Resources");
        a("tip.repo.Territory2",                           "Territ\u00f3rios (Gerenciamento)",                          "Territory Management Territories");
        a("tip.repo.Territory2Model",                      "Modelos de Gerenciamento de Territ\u00f3rios",              "Territory Management Models");
        a("tip.repo.Territory2Rule",                       "Regras de Gerenciamento de Territ\u00f3rios",               "Territory Management Rules");
        a("tip.repo.Territory2Type",                       "Tipos de Gerenciamento de Territ\u00f3rios",                "Territory Management Types");
        a("tip.repo.TopicsForObjects",                     "T\u00f3picos para Objetos",                                 "Topics for Objects");
        a("tip.repo.Translations",                         "Tradu\u00e7\u00f5es",                                       "Translations");
        a("tip.repo.ValidationRule",                       "Regras de Valida\u00e7\u00e3o",                             "Validation Rules");
        a("tip.repo.Workflow",                             "Regras de Workflow",                                        "Workflow Rules");
        a("lbl.path.notFound",                             "O endere\u00e7o especificado n\u00e3o foi encontrado",       "The specified path was not found");
        a("btn.compareRepo",                               "Comparar com Reposit\u00f3rio",                            "Compare with Repository");
        a("btn.compareWithOrg1",                           "Comparar com Org 1",                                       "Compare with Org 1");
        a("btn.compareWithOrg2",                           "Comparar com Org 2",                                       "Compare with Org 2");
        a("msg.repo.warn.text",                            "Selecionar mais de 3 metadados para comparar, pode demorar muito a responder. Deseja continuar?", "Selecting more than 3 metadata types to compare may take a long time. Do you want to continue?");
        a("msg.repo.warn.title",                           "Aten\u00e7\u00e3o",                                        "Warning");
        a("btn.yes",                                       "Sim",                                                      "Yes");
        a("btn.no",                                        "N\u00e3o",                                                  "No");
    }

    private static void a(String key, String pt, String en) {
        S.put(key, new String[]{pt, en});
    }

    /** Retorna o texto no idioma atual para a chave fornecida. */
    public static String get(String key) {
        String[] v = S.get(key);
        if (v == null) return key;
        return current == Language.PT_BR ? v[0] : v[1];
    }

    /** Retorna o texto formatado com os argumentos (usando MessageFormat). */
    public static String get(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }

    /** Altera o idioma ativo e notifica todos os listeners registrados. */
    public static void setLanguage(Language lang) {
        current = lang;
        new ArrayList<>(listeners).forEach(Runnable::run);
    }

    public static Language getLanguage() { return current; }

    /** Registra um callback para ser invocado sempre que o idioma mudar. */
    public static void addChangeListener(Runnable r) { listeners.add(r); }
}
