package com.sfcomparator.comparator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONObject;
import com.sfcomparator.cli.CLIExecutor;
import com.sfcomparator.model.ComparisonConfig;
import com.sfcomparator.model.Difference;
import com.sfcomparator.model.Field;
import com.sfcomparator.ui.LanguageManager;

public class ComparisonEngine {
    private final CLIExecutor cli;
    private final ComparisonConfig config;
    private final List<Difference> differences;
    private Consumer<String> progressCallback = msg -> {};
    private AtomicBoolean cancelFlag = null;

    /** Tamanho máximo (bytes) de cada arquivo para armazenar conteúdo no diff viewer. */
    private static final long MAX_DIFF_CONTENT_BYTES = 300_000L;

    public ComparisonEngine(CLIExecutor cli, ComparisonConfig config) {
        this.cli         = cli;
        this.config      = config;
        this.differences = new ArrayList<>();
    }

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    public void setCancelFlag(AtomicBoolean flag) {
        this.cancelFlag = flag;
    }

    private void progress(String msg) {
        progressCallback.accept(msg);
    }

    private void checkCancelled() throws InterruptedException {
        if (cancelFlag != null && cancelFlag.get()) {
            throw new InterruptedException("Verificacao cancelada pelo usuario.");
        }
    }

    public List<Difference> executeComparison() throws Exception {
        String sobject    = config.getSobjectName();
        boolean hasSobject = sobject != null && !sobject.isBlank();

        if (hasSobject) {
            if (config.isCompareObjectFields())    { checkCancelled(); progress(LanguageManager.get("progress.fields", sobject)); compareSObjectFields(); }
            if (config.isComparePageLayouts())     { checkCancelled(); progress(LanguageManager.get("progress.pageLayouts")); comparePageLayouts(sobject); }
            if (config.isCompareValidationRules()) { checkCancelled(); progress(LanguageManager.get("progress.validationRules")); compareValidationRules(sobject); }
            if (config.isCompareFlows())           { checkCancelled(); progress(LanguageManager.get("progress.flows")); compareFlows(sobject); }
            if (config.isCompareApexTriggers())    { checkCancelled(); progress(LanguageManager.get("progress.apexTriggers")); compareApexTriggers(sobject); }
            if (config.isCompareRecordTypes())     { checkCancelled(); progress(LanguageManager.get("progress.recordTypes")); compareRecordTypes(sobject); }
        }

        if (config.isCompareCustomMetadata())    { checkCancelled(); progress(LanguageManager.get("progress.customMetadata")); compareCustomMetadata(); }
        if (config.isCompareCustomSettings())    { checkCancelled(); progress(LanguageManager.get("progress.customSettings")); compareCustomSettings(); }
        if (config.isComparePermissionSets())    { checkCancelled(); progress(LanguageManager.get("progress.permSets")); comparePermissionSets(); }
        if (config.isCompareProfiles())          { checkCancelled(); progress(LanguageManager.get("progress.profiles")); compareProfiles(); }
        if (config.isCompareGroups())            { checkCancelled(); progress(LanguageManager.get("progress.groups")); compareGroups(); }
        if (config.isCompareApprovalProcesses()) { checkCancelled(); progress(LanguageManager.get("progress.approvalProcesses")); compareApprovalProcesses(); }
        if (config.isCompareNamedCredentials())  { checkCancelled(); progress(LanguageManager.get("progress.namedCredentials")); compareNamedCredentials(); }

        return differences;
    }

    // -----------------------------------------------------------------------
    // Campos do Objeto
    // -----------------------------------------------------------------------

    private void compareSObjectFields() throws Exception {
        JSONObject org1Describe = cli.describeOrg1SObject(config.getSobjectName());
        JSONObject org2Describe = cli.describeOrg2SObject(config.getSobjectName());

        JSONArray org1Fields = org1Describe.getJSONArray("fields");
        JSONArray org2Fields = org2Describe.getJSONArray("fields");

        Map<String, Field> org1Map = new HashMap<>();
        Map<String, Field> org2Map = new HashMap<>();

        for (int i = 0; i < org1Fields.length(); i++) {
            JSONObject f = org1Fields.getJSONObject(i);
            org1Map.put(f.getString("name"), new Field(f.getString("name"), f.getString("type")));
        }
        for (int i = 0; i < org2Fields.length(); i++) {
            JSONObject f = org2Fields.getJSONObject(i);
            org2Map.put(f.getString("name"), new Field(f.getString("name"), f.getString("type")));
        }

        String prefix = config.getSobjectName() + ".";
        for (Map.Entry<String, Field> e : org1Map.entrySet()) {
            if (!org2Map.containsKey(e.getKey())) {
                differences.add(new Difference(Difference.DifferenceType.SOBJECT_FIELD_MISSING_IN_ORG2,
                    "Object Fields", prefix + e.getKey(), e.getValue().getType(), "Absent"));
            } else if (!e.getValue().getType().equals(org2Map.get(e.getKey()).getType())) {
                differences.add(new Difference(Difference.DifferenceType.SOBJECT_FIELD_TYPE_MISMATCH,
                    "Object Fields", prefix + e.getKey(),
                    e.getValue().getType(), org2Map.get(e.getKey()).getType()));
            }
        }
        for (String name : org2Map.keySet()) {
            if (!org1Map.containsKey(name)) {
                differences.add(new Difference(Difference.DifferenceType.SOBJECT_FIELD_MISSING_IN_ORG1,
                    "Object Fields", prefix + name, "Absent", org2Map.get(name).getType()));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Metadados vinculados ao Objeto (XML + contagem de linhas)
    // -----------------------------------------------------------------------

    private void comparePageLayouts(String sobject) throws Exception {
        String q = "SELECT Name FROM Layout WHERE TableEnumOrId = '" + sobject + "'";
        JSONArray[] soqlResult = cli.queryBothOrgsToolingSOQL(q);
        Set<String> names1 = extractStringSet(soqlResult[0], "Name");
        Set<String> names2 = extractStringSet(soqlResult[1], "Name");
        String category = "Page Layouts (" + sobject + ")";
        Set<String> common = comparePresenceAbsence(category, names1, names2);
        if (common.isEmpty()) return;
        progress(LanguageManager.get("progress.pageLayoutsMeta"));
        String[] specs = common.stream().map(n -> "Layout:" + n).toArray(String[]::new);
        Path[] dirs = cli.retrieveBothOrgsMetadata(specs);
        Map<String, Path> files1 = findMetadataFiles(dirs[0], ".layout-meta.xml");
        Map<String, Path> files2 = findMetadataFiles(dirs[1], ".layout-meta.xml");
        for (String name : common) {
            checkCancelled();
            compareFileLineCount(category, name, files1.get(name), files2.get(name));
        }
    }

    private void compareValidationRules(String sobject) throws Exception {
        String q = "SELECT ValidationName, Active FROM ValidationRule WHERE EntityDefinitionId = '" + sobject + "'";
        compareByToolingSOQL("Validation Rules (" + sobject + ")", q, "ValidationName", "Active");
    }

    private void compareFlows(String sobject) throws Exception {
        String q = "SELECT ApiName, VersionNumber FROM FlowDefinitionView"
                 + " WHERE TriggerObjectOrEventLabel = '" + sobject + "'";
        JSONArray[] soqlResult = cli.queryBothOrgsSOQL(q);
        JSONArray org1Records = soqlResult[0];
        JSONArray org2Records = soqlResult[1];

        String category = "Flows (" + sobject + ")";

        Map<String, Integer> org1Map = new HashMap<>();
        for (int i = 0; i < org1Records.length(); i++) {
            JSONObject r = org1Records.getJSONObject(i);
            org1Map.put(r.optString("ApiName"), r.optInt("VersionNumber", 0));
        }
        Map<String, Integer> org2Map = new HashMap<>();
        for (int i = 0; i < org2Records.length(); i++) {
            JSONObject r = org2Records.getJSONObject(i);
            org2Map.put(r.optString("ApiName"), r.optInt("VersionNumber", 0));
        }

        // Presentes apenas na Org1
        for (Map.Entry<String, Integer> e : org1Map.entrySet()) {
            checkCancelled();
            String name = e.getKey();
            if (!org2Map.containsKey(name)) {
                differences.add(new Difference(Difference.DifferenceType.METADATA_MISSING_IN_ORG2,
                    category, name, "v" + e.getValue(), "Absent"));
            }
        }

        // Flows comuns: comparar VersionNumber
        for (Map.Entry<String, Integer> e : org1Map.entrySet()) {
            checkCancelled();
            String name = e.getKey();
            if (!org2Map.containsKey(name)) continue;
            int v1 = e.getValue();
            int v2 = org2Map.get(name);
            if (v1 != v2) {
                Difference diff = new Difference(Difference.DifferenceType.METADATA_STRUCTURE_MISMATCH,
                    category, name, "v" + v1, "v" + v2);
                diff.setDetails("VersionNumber: " + v1 + " (Org1) → " + v2 + " (Org2)");
                differences.add(diff);
            }
        }

        // Presentes apenas na Org2
        for (Map.Entry<String, Integer> e : org2Map.entrySet()) {
            checkCancelled();
            String name = e.getKey();
            if (!org1Map.containsKey(name)) {
                differences.add(new Difference(Difference.DifferenceType.METADATA_MISSING_IN_ORG1,
                    category, name, "Absent", "v" + e.getValue()));
            }
        }
    }

    private void compareApexTriggers(String sobject) throws Exception {
        String q = "SELECT Name, Status, ApiVersion, Body FROM ApexTrigger WHERE TableEnumOrId = '" + sobject + "'";
        JSONArray[] soqlResult = cli.queryBothOrgsToolingSOQL(q);
        JSONArray org1Records = soqlResult[0];
        JSONArray org2Records = soqlResult[1];

        String category = "Apex Triggers (" + sobject + ")";
        Map<String, JSONObject> org1Map = indexByField(org1Records, "Name");
        Map<String, JSONObject> org2Map = indexByField(org2Records, "Name");

        for (Map.Entry<String, JSONObject> e : org1Map.entrySet()) {
            String name = e.getKey();
            if (!org2Map.containsKey(name)) {
                differences.add(new Difference(Difference.DifferenceType.METADATA_MISSING_IN_ORG2,
                    category, name, "Present", "Absent"));
            } else {
                JSONObject r1 = e.getValue();
                JSONObject r2 = org2Map.get(name);
                List<String> diffs = new ArrayList<>();

                String st1 = field(r1, "Status"),     st2 = field(r2, "Status");
                if (!st1.equals(st2)) diffs.add("Status: " + st1 + " -> " + st2);

                String av1 = field(r1, "ApiVersion"), av2 = field(r2, "ApiVersion");
                if (!av1.equals(av2)) diffs.add("ApiVersion: " + av1 + " -> " + av2);

                String b1 = field(r1, "Body"), b2 = field(r2, "Body");
                if (!b1.equals(b2)) {
                    diffs.add("Content diverges (Org1: " + b1.length() +
                              " chars / Org2: " + b2.length() + " chars)");
                }

                if (!diffs.isEmpty()) {
                    Difference diff = new Difference(Difference.DifferenceType.METADATA_STRUCTURE_MISMATCH,
                        category, name, "Divergent", "Divergent");
                    diff.setDetails(String.join(" | ", diffs));
                    // Armazena corpo do Apex para o diff viewer quando o conteúdo difere
                    if (!b1.equals(b2)
                            && b1.length() <= MAX_DIFF_CONTENT_BYTES
                            && b2.length() <= MAX_DIFF_CONTENT_BYTES) {
                        diff.setContent1(b1);
                        diff.setContent2(b2);
                    }
                    differences.add(diff);
                }
            }
        }
        for (String name : org2Map.keySet()) {
            if (!org1Map.containsKey(name)) {
                differences.add(new Difference(Difference.DifferenceType.METADATA_MISSING_IN_ORG1,
                    category, name, "Absent", "Present"));
            }
        }
    }

    private void compareRecordTypes(String sobject) throws Exception {
        String q = "SELECT DeveloperName FROM RecordType WHERE SObjectType = '" + sobject + "'";
        JSONArray[] soqlResult = cli.queryBothOrgsSOQL(q);
        Set<String> names1 = extractStringSet(soqlResult[0], "DeveloperName");
        Set<String> names2 = extractStringSet(soqlResult[1], "DeveloperName");
        String category = "Record Types (" + sobject + ")";
        Set<String> common = comparePresenceAbsence(category, names1, names2);
        if (common.isEmpty()) return;
        progress(LanguageManager.get("progress.recordTypesMeta"));
        String[] specs = common.stream().map(n -> "RecordType:" + sobject + "." + n).toArray(String[]::new);
        Path[] dirs = cli.retrieveBothOrgsMetadata(specs);
        Map<String, Path> files1 = findMetadataFiles(dirs[0], ".recordType-meta.xml");
        Map<String, Path> files2 = findMetadataFiles(dirs[1], ".recordType-meta.xml");
        for (String name : common) {
            checkCancelled();
            compareFileLineCount(category, name, files1.get(name), files2.get(name));
        }
    }

    // -----------------------------------------------------------------------
    // Metadados da Org -- independentes do objeto
    // -----------------------------------------------------------------------

    private void compareCustomMetadata() throws Exception {
        String q = "SELECT QualifiedApiName, Label FROM EntityDefinition WHERE QualifiedApiName LIKE '%__mdt'";
        compareByToolingSOQL("Custom Metadata Types", q, "QualifiedApiName", "Label");
    }

    private void compareCustomSettings() throws Exception {
        String q = "SELECT QualifiedApiName, Label FROM EntityDefinition WHERE IsCustomSetting = true";
        compareByToolingSOQL("Custom Settings", q, "QualifiedApiName", "Label");
    }

    private void comparePermissionSets() throws Exception {
        String psFilter = config.getPermissionSetsFilter();
        StringBuilder psq = new StringBuilder("SELECT Name FROM PermissionSet WHERE IsOwnedByProfile = false");
        if (psFilter != null && !psFilter.isBlank()) {
            List<String> conds = new ArrayList<>();
            for (String part : psFilter.split(",")) {
                String v = part.trim().replace("'", "\\'");
                if (!v.isEmpty()) conds.add("(Name LIKE '%" + v + "%' OR Label LIKE '%" + v + "%')");
            }
            if (!conds.isEmpty()) psq.append(" AND (").append(String.join(" OR ", conds)).append(")");
        }
        JSONArray[] soqlResult = cli.queryBothOrgsSOQL(psq.toString());
        Set<String> names1 = extractStringSet(soqlResult[0], "Name");
        Set<String> names2 = extractStringSet(soqlResult[1], "Name");
        Set<String> common = comparePresenceAbsence("Permission Sets", names1, names2);
        if (common.isEmpty()) return;
        progress(LanguageManager.get("progress.permSetsMeta"));
        // Recupera todos de uma vez para evitar linha de comando longa
        Path[] dirs = cli.retrieveBothOrgsMetadata("PermissionSet");
        Map<String, Path> files1 = findMetadataFiles(dirs[0], ".permissionset-meta.xml");
        Map<String, Path> files2 = findMetadataFiles(dirs[1], ".permissionset-meta.xml");
        for (String name : common) {
            checkCancelled();
            compareFileLineCount("Permission Sets", name, files1.get(name), files2.get(name));
        }
    }

    private void compareProfiles() throws Exception {
        progress(LanguageManager.get("progress.profilesMeta"));
        Path[] dirs = cli.retrieveBothOrgsMetadata("Profile");

        Map<String, Path> files1 = findMetadataFiles(dirs[0], ".profile-meta.xml");
        Map<String, Path> files2 = findMetadataFiles(dirs[1], ".profile-meta.xml");

        // Aplicar filtro por nome localmente (sem depender de SOQL/paginação)
        String pfFilter = config.getProfilesFilter();
        if (pfFilter != null && !pfFilter.isBlank()) {
            String[] terms = pfFilter.split(",");
            files1 = filterByName(files1, terms);
            files2 = filterByName(files2, terms);
        }

        Set<String> common = comparePresenceAbsence("Profiles", files1.keySet(), files2.keySet());
        for (String name : common) {
            checkCancelled();
            compareFileLineCount("Profiles", name, files1.get(name), files2.get(name));
        }
    }

    private void compareGroups() throws Exception {
        String q = "SELECT Name, DeveloperName, DoesIncludeBosses FROM Group WHERE Type = 'Regular'";
        compareBySOQL("Public Groups", q, "Name", "DeveloperName", "DoesIncludeBosses");
    }

    private void compareApprovalProcesses() throws Exception {
        String q = "SELECT DeveloperName, Name, TableEnumOrId FROM ProcessDefinition WHERE Type = 'Approval'";
        compareBySOQL("Approval Processes", q, "DeveloperName", "Name", "TableEnumOrId");
    }

    private void compareNamedCredentials() throws Exception {
        String q = "SELECT DeveloperName, Endpoint, PrincipalType FROM NamedCredential";
        compareByToolingSOQL("Named Credentials", q, "DeveloperName", "Endpoint", "PrincipalType");
    }

    // -----------------------------------------------------------------------
    // Helpers de comparacao por arquivo XML (contagem de linhas)
    // -----------------------------------------------------------------------

    /**
     * Adiciona entradas de missing para nomes presentes em apenas uma das orgs
     * e retorna o conjunto de nomes presentes em AMBAS as orgs.
     */
    private Set<String> comparePresenceAbsence(String category,
                                                Set<String> names1, Set<String> names2) {
        for (String n : names1) {
            if (!names2.contains(n)) {
                differences.add(new Difference(Difference.DifferenceType.METADATA_MISSING_IN_ORG2,
                    category, n, "Present", "Absent"));
            }
        }
        for (String n : names2) {
            if (!names1.contains(n)) {
                differences.add(new Difference(Difference.DifferenceType.METADATA_MISSING_IN_ORG1,
                    category, n, "Absent", "Present"));
            }
        }
        Set<String> common = new HashSet<>(names1);
        common.retainAll(names2);
        return common;
    }

    private void compareFileLineCount(String category, String name,
                                       Path file1, Path file2) throws Exception {
        if (file1 == null || file2 == null) return; // presenca/ausencia ja tratada
        long size1 = Files.size(file1);
        long size2 = Files.size(file2);

        // Normaliza o conteúdo antes de comparar e armazenar:
        //   1. CRLF/CR → LF
        //   2. Remove trailing whitespace de cada linha
        // Evita falsos positivos por CRLF, espaços finais invisíveis ou newline final divergente.
        String content1Str = normalizeContent(Files.readString(file1, StandardCharsets.UTF_8));
        String content2Str = normalizeContent(Files.readString(file2, StandardCharsets.UTF_8));

        if (content1Str.stripTrailing().equals(content2Str.stripTrailing())) return;

        long lines1 = countLinesInString(content1Str);
        long lines2 = countLinesInString(content2Str);
        if (lines1 != lines2) {
            Difference diff = new Difference(Difference.DifferenceType.METADATA_STRUCTURE_MISMATCH,
                category, name, "Divergent", "Divergent");
            diff.setDetails("Org1: " + lines1 + " lines | Org2: " + lines2 + " lines");
            // Armazena conteúdo normalizado para o diff viewer
            if (size1 <= MAX_DIFF_CONTENT_BYTES && size2 <= MAX_DIFF_CONTENT_BYTES) {
                diff.setContent1(content1Str);
                diff.setContent2(content2Str);
            }
            differences.add(diff);
        }
    }

    /** Conta linhas em uma string já com terminadores normalizados para LF. */
    private static long countLinesInString(String s) {
        if (s.isEmpty()) return 0;
        long n = s.chars().filter(c -> c == '\n').count();
        // Se a string não termina com '\n', a última linha não possui newline
        return s.charAt(s.length() - 1) == '\n' ? n : n + 1;
    }

    /**
     * Normaliza o conteúdo de um arquivo texto para comparação e armazenamento:
     * converte CRLF/CR para LF e remove espaços/tabs no final de cada linha.
     */
    private static String normalizeContent(String raw) {
        String lf = raw.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = lf.split("\n", -1);
        StringBuilder sb = new StringBuilder(lf.length());
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines[i].stripTrailing());
        }
        return sb.toString();
    }

    private Map<String, Path> findMetadataFiles(Path root, String extension) {
        Map<String, Path> map = new HashMap<>();
        if (root == null || !Files.exists(root)) return map;
        try {
            Files.walk(root)
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.getFileName().toString().endsWith(extension))
                .forEach(p -> {
                    String fn = p.getFileName().toString();
                    map.put(fn.substring(0, fn.length() - extension.length()), p);
                });
        } catch (IOException e) { /* ignorar */ }
        return map;
    }

    /** Filtra um mapa de arquivos mantendo apenas entradas cujo nome contenha algum dos termos. */
    private Map<String, Path> filterByName(Map<String, Path> files, String[] terms) {
        Map<String, Path> filtered = new HashMap<>();
        for (Map.Entry<String, Path> entry : files.entrySet()) {
            String nameLower = entry.getKey().toLowerCase();
            for (String term : terms) {
                if (!term.isBlank() && nameLower.contains(term.trim().toLowerCase())) {
                    filtered.put(entry.getKey(), entry.getValue());
                    break;
                }
            }
        }
        return filtered;
    }

    private Set<String> extractStringSet(JSONArray records, String fieldName) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject r = records.getJSONObject(i);
            if (r.has(fieldName) && !r.isNull(fieldName)) set.add(r.getString(fieldName));
        }
        return set;
    }

    // -----------------------------------------------------------------------
    // Helpers de comparacao genericos (SOQL)
    // -----------------------------------------------------------------------

    private void compareByToolingSOQL(String category, String soql, String nameField,
                                      String... detailFields) throws Exception {
        JSONArray[] res = cli.queryBothOrgsToolingSOQL(soql);
        compareRecordLists(category, res[0], res[1], nameField, detailFields);
    }

    private void compareBySOQL(String category, String soql, String nameField,
                                String... detailFields) throws Exception {
        JSONArray[] res = cli.queryBothOrgsSOQL(soql);
        compareRecordLists(category, res[0], res[1], nameField, detailFields);
    }

    private void compareRecordLists(String category, JSONArray org1Records, JSONArray org2Records,
                                    String nameField, String... detailFields) {
        Map<String, String> org1Map = extractNameDetails(org1Records, nameField, detailFields);
        Map<String, String> org2Map = extractNameDetails(org2Records, nameField, detailFields);
        compareMaps(category, org1Map, org2Map);
    }

    private void compareMaps(String category,
                              Map<String, String> org1Map, Map<String, String> org2Map) {
        for (Map.Entry<String, String> entry : org1Map.entrySet()) {
            String name = entry.getKey();
            if (!org2Map.containsKey(name)) {
                differences.add(new Difference(Difference.DifferenceType.METADATA_MISSING_IN_ORG2,
                    category, name, "Present", "Absent"));
            } else if (!entry.getValue().isEmpty() && !entry.getValue().equals(org2Map.get(name))) {
                Difference diff = new Difference(Difference.DifferenceType.METADATA_STRUCTURE_MISMATCH,
                    category, name, "Divergent", "Divergent");
                diff.setDetails("Org1: " + entry.getValue() + "\nOrg2: " + org2Map.get(name));
                differences.add(diff);
            }
        }
        for (String name : org2Map.keySet()) {
            if (!org1Map.containsKey(name)) {
                differences.add(new Difference(Difference.DifferenceType.METADATA_MISSING_IN_ORG1,
                    category, name, "Absent", "Present"));
            }
        }
    }

    private Map<String, String> extractNameDetails(JSONArray records, String nameField,
                                                   String... detailFields) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject r = records.getJSONObject(i);
            if (!r.has(nameField) || r.isNull(nameField)) continue;
            String name = r.getString(nameField);
            if (detailFields.length == 0) { map.put(name, ""); continue; }
            StringBuilder sb = new StringBuilder();
            for (String df : detailFields) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(df).append(": ").append(field(r, df));
            }
            map.put(name, sb.toString());
        }
        return map;
    }

    // -----------------------------------------------------------------------
    // Utilitarios
    // -----------------------------------------------------------------------

    private String field(JSONObject r, String key) {
        return r.has(key) && !r.isNull(key) ? r.get(key).toString() : "";
    }

    private Map<String, JSONObject> indexByField(JSONArray records, String nameField) {
        Map<String, JSONObject> map = new HashMap<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject r = records.getJSONObject(i);
            if (r.has(nameField) && !r.isNull(nameField)) {
                map.put(r.getString(nameField), r);
            }
        }
        return map;
    }
}