package com.sfcomparator.model;

import java.util.List;

/**
 * Registro central de todos os tipos de metadados Salesforce mapeados no projeto.
 * Fonte de referencia para varredura de pastas e comparacao com repositorio local.
 * Cada entrada contem o nome da API (Salesforce CLI) e o caminho relativo da pasta
 * dentro do projeto SFDX (a partir do endereco raiz informado pelo usuario).
 */
public final class MetadataRegistry {

    /** Representa um tipo de metadado e o caminho relativo de sua pasta no projeto. */
    public record MetadataEntry(String apiName, String folderPath) {}

    /**
     * Todos os tipos de metadados com caminho de pasta no projeto,
     * ordenados alfabeticamente pelo nome da API.
     */
    public static final List<MetadataEntry> ENTRIES = List.of(
        new MetadataEntry("ApexClass",                            "force-app/main/default/classes"),
        new MetadataEntry("ApexComponent",                        "force-app/main/default/components"),
        new MetadataEntry("ApexPage",                             "force-app/main/default/pages"),
        new MetadataEntry("ApexTestSuite",                        "force-app/main/default/testSuites"),
        new MetadataEntry("ApexTrigger",                          "force-app/main/default/triggers"),
        new MetadataEntry("ApprovalProcess",                      "force-app/main/default/approvalProcesses"),
        new MetadataEntry("AssignmentRule",                       "force-app/main/default/assignmentRules"),
        new MetadataEntry("AssignmentRules",                      "force-app/main/default/assignmentRules"),
        new MetadataEntry("AuraDefinitionBundle",                 "force-app/main/default/aura"),
        new MetadataEntry("AuthProvider",                         "force-app/main/default/authproviders"),
        new MetadataEntry("Bot",                                  "force-app/main/default/bots"),
        new MetadataEntry("BrandingSet",                          "force-app/main/default/brandingSets"),
        new MetadataEntry("CompactLayout",                        "force-app/main/default/objects"),
        new MetadataEntry("ConnectedApp",                         "force-app/main/default/connectedApps"),
        new MetadataEntry("ContentAsset",                         "force-app/main/default/contentassets"),
        new MetadataEntry("CustomApplication",                    "force-app/main/default/applications"),
        new MetadataEntry("CustomIndex",                          "force-app/main/default/objects"),
        new MetadataEntry("CustomLabel",                          "force-app/main/default/labels"),
        new MetadataEntry("CustomLabels",                         "force-app/main/default/labels"),
        new MetadataEntry("CustomMetadata",                       "force-app/main/default/customMetadata"),
        new MetadataEntry("CustomNotificationType",               "force-app/main/default/notificationtypes"),
        new MetadataEntry("CustomObject",                         "force-app/main/default/objects"),
        new MetadataEntry("CustomObjectTranslation",              "force-app/main/default/objectTranslations"),
        new MetadataEntry("CustomPermission",                     "force-app/main/default/customPermissions"),
        new MetadataEntry("CustomTab",                            "force-app/main/default/tabs"),
        new MetadataEntry("Dashboard",                            "force-app/main/default/dashboards"),
        new MetadataEntry("DataCategoryGroup",                    "force-app/main/default/datacategorygroups"),
        new MetadataEntry("DuplicateRule",                        "force-app/main/default/duplicateRules"),
        new MetadataEntry("EclairGeoData",                        "force-app/main/default/eclair"),
        new MetadataEntry("EmailServicesFunction",                "force-app/main/default/emailservices"),
        new MetadataEntry("EmailTemplate",                        "force-app/main/default/email"),
        new MetadataEntry("ExternalClientApplication",            "force-app/main/default/externalClientApps"),
        new MetadataEntry("ExternalCredential",                   "force-app/main/default/externalCredentials"),
        new MetadataEntry("ExtlClntAppConfigurablePolicies",      "force-app/main/default/extlClntAppPolicies"),
        new MetadataEntry("ExtlClntAppGlobalOauthSettings",       "force-app/main/default/extlClntAppGlobalOauthSets"),
        new MetadataEntry("ExtlClntAppOauthConfigurablePolicies", "force-app/main/default/extlClntAppOauthPolicies"),
        new MetadataEntry("ExtlClntAppOauthSettings",             "force-app/main/default/extlClntAppOauthSettings"),
        new MetadataEntry("FieldSet",                             "force-app/main/default/objects"),
        new MetadataEntry("FlexiPage",                            "force-app/main/default/flexipages"),
        new MetadataEntry("Flow",                                 "force-app/main/default/flows"),
        new MetadataEntry("FlowDefinition",                       "force-app/main/default/flowDefinitions"),
        new MetadataEntry("FlowTest",                             "force-app/main/default/flowtests"),
        new MetadataEntry("GenAiFunction",                        "force-app/main/default/genAiFunctions"),
        new MetadataEntry("GenAiPlannerBundle",                   "force-app/main/default/genAiPlannerBundles"),
        new MetadataEntry("GenAiPromptTemplate",                  "force-app/main/default/genAiPromptTemplates"),
        new MetadataEntry("GlobalValueSet",                       "force-app/main/default/globalValueSets"),
        new MetadataEntry("GlobalValueSetTranslation",            "force-app/main/default/globalValueSetTranslations"),
        new MetadataEntry("Group",                                "force-app/main/default/groups"),
        new MetadataEntry("Layout",                               "force-app/main/default/layouts"),
        new MetadataEntry("LeadConvertSettings",                  "force-app/main/default/LeadConvertSettings"),
        new MetadataEntry("Letterhead",                           "force-app/main/default/letterhead"),
        new MetadataEntry("LightningComponentBundle",             "force-app/main/default/lwc"),
        new MetadataEntry("LightningExperienceTheme",             "force-app/main/default/lightningExperienceThemes"),
        new MetadataEntry("LightningMessageChannel",              "force-app/main/default/messageChannels"),
        new MetadataEntry("ListView",                             "force-app/main/default/objects"),
        new MetadataEntry("MatchingRule",                         "force-app/main/default/matchingRules"),
        new MetadataEntry("MatchingRules",                        "force-app/main/default/matchingRules"),
        new MetadataEntry("NamedCredential",                      "force-app/main/default/namedCredentials"),
        new MetadataEntry("NotificationTypeConfig",               "force-app/main/default/notificationtypes"),
        new MetadataEntry("OmniDataTransform",                    "force-app/main/default/omniDataTransforms"),
        new MetadataEntry("OmniIntegrationProcedure",             "force-app/main/default/omniIntegrationProcedures"),
        new MetadataEntry("OmniInteractionConfig",                "force-app/main/default/OmniInteractionConfig"),
        new MetadataEntry("OmniScript",                           "force-app/main/default/omniScripts"),
        new MetadataEntry("OmniUiCard",                           "force-app/main/default/omniUiCard"),
        new MetadataEntry("ParticipantRole",                      "force-app/main/default/participantRoles"),
        new MetadataEntry("PathAssistant",                        "force-app/main/default/pathAssistants"),
        new MetadataEntry("PermissionSet",                        "force-app/main/default/permissionsets"),
        new MetadataEntry("PermissionSetGroup",                   "force-app/main/default/permissionsetgroups"),
        new MetadataEntry("PlatformCachePartition",               "force-app/main/default/cachePartitions"),
        new MetadataEntry("PlatformEventChannelMember",           "force-app/main/default/platformEventChannelMembers"),
        new MetadataEntry("PostTemplate",                         "force-app/main/default/postTemplates"),
        new MetadataEntry("PresenceUserConfig",                   "force-app/main/default/presenceUserConfigs"),
        new MetadataEntry("Profile",                              "force-app/main/default/profiles"),
        new MetadataEntry("ProfilePasswordPolicy",                "force-app/main/default/profilePasswordPolicies"),
        new MetadataEntry("ProfileSessionSetting",                "force-app/main/default/profileSessionSettings"),
        new MetadataEntry("Prompt",                               "force-app/main/default/prompts"),
        new MetadataEntry("Queue",                                "force-app/main/default/queues"),
        new MetadataEntry("QueueRoutingConfig",                   "force-app/main/default/queueRoutingConfigs"),
        new MetadataEntry("QuickAction",                          "force-app/main/default/quickActions"),
        new MetadataEntry("RecordAlertCategory",                  "force-app/main/default/recordAlertCategories"),
        new MetadataEntry("RecordType",                           "force-app/main/default/objects"),
        new MetadataEntry("RemoteSiteSetting",                    "force-app/main/default/remoteSiteSettings"),
        new MetadataEntry("Report",                               "force-app/main/default/reports"),
        new MetadataEntry("ReportType",                           "force-app/main/default/reportTypes"),
        new MetadataEntry("Role",                                 "force-app/main/default/roles"),
        new MetadataEntry("ServiceChannel",                       "force-app/main/default/serviceChannels"),
        new MetadataEntry("Settings",                             "force-app/main/default/settings"),
        new MetadataEntry("SharingRules",                         "force-app/main/default/sharingRules"),
        new MetadataEntry("StandardValueSetTranslation",          "force-app/main/default/standardValueSetTranslations"),
        new MetadataEntry("StaticResource",                       "force-app/main/default/staticresources"),
        new MetadataEntry("Territory2",                           "force-app/main/default/territory2Models"),
        new MetadataEntry("Territory2Model",                      "force-app/main/default/territory2Models"),
        new MetadataEntry("Territory2Rule",                       "force-app/main/default/territory2Models"),
        new MetadataEntry("Territory2Type",                       "force-app/main/default/territory2Types"),
        new MetadataEntry("TopicsForObjects",                     "force-app/main/default/topicsForObjects"),
        new MetadataEntry("Translations",                         "force-app/main/default/translations"),
        new MetadataEntry("ValidationRule",                       "force-app/main/default/objects"),
        new MetadataEntry("Workflow",                             "force-app/main/default/workflows")
    );

    private MetadataRegistry() {}
}
