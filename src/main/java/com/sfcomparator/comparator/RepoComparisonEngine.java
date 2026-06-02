package com.sfcomparator.comparator;

import com.sfcomparator.cli.CLIExecutor;
import com.sfcomparator.model.Difference;
import com.sfcomparator.model.MetadataRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Motor de comparação entre repositório local SFDX e uma org Salesforce.
 *
 * Para cada tipo de metadado selecionado:
 *   1. Lista os arquivos *-meta.xml no caminho local correspondente.
 *   2. Recupera o mesmo tipo da org via Salesforce CLI.
 *   3. Compara: Ausente na org / Presente apenas na org / Divergente.
 *
 * Os resultados são expressos como {@link Difference} compatível com {@link com.sfcomparator.ui.ResultsPanel}:
 *   - orgNumber == 1 → status na coluna Org 1; coluna Org 2 = "-"
 *   - orgNumber == 2 → status na coluna Org 2; coluna Org 1 = "-"
 */
public class RepoComparisonEngine {

    private final CLIExecutor cli;
    private final String basePath;
    private final List<String> selectedApiNames;
    private final int orgNumber;

    private final List<Difference> differences = new ArrayList<>();
    private Consumer<String> progressCallback = msg -> {};
    private AtomicBoolean cancelFlag = null;

    /** Tamanho máximo (bytes) de cada arquivo para armazenar conteúdo no diff viewer. */
    private static final long MAX_DIFF_CONTENT_BYTES = 300_000L;

    public RepoComparisonEngine(CLIExecutor cli, String basePath,
                                List<String> selectedApiNames, int orgNumber) {
        this.cli              = cli;
        this.basePath         = basePath;
        this.selectedApiNames = List.copyOf(selectedApiNames);
        this.orgNumber        = orgNumber;
    }

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    public void setCancelFlag(AtomicBoolean flag) {
        this.cancelFlag = flag;
    }

    // -----------------------------------------------------------------------

    public List<Difference> executeComparison() throws Exception {
        for (String apiName : selectedApiNames) {
            checkCancelled();
            MetadataRegistry.MetadataEntry entry = MetadataRegistry.ENTRIES.stream()
                .filter(e -> e.apiName().equals(apiName))
                .findFirst().orElse(null);
            if (entry == null) continue;
            compareMetadataType(entry);
        }
        return Collections.unmodifiableList(differences);
    }

    // -----------------------------------------------------------------------
    // Comparação por tipo de metadado
    // -----------------------------------------------------------------------

    private void compareMetadataType(MetadataRegistry.MetadataEntry entry) throws Exception {
        // PASSO 1: recuperar TODOS os metadados do tipo diretamente da org
        progress("Recuperando " + entry.apiName() + " da Org " + orgNumber + "...");

        Map<String, Path> orgFiles = new LinkedHashMap<>();
        try {
            Path orgDir = (orgNumber == 1)
                ? cli.retrieveOrg1Metadata(entry.apiName())
                : cli.retrieveOrg2Metadata(entry.apiName());

            // Percorre toda a árvore do diretório recuperado — sem assumir
            // estrutura de subpasta específica (ex.: retrieved/ vs
            // retrieved/force-app/main/default/<pasta>/), usando apenas o
            // nome do arquivo como chave para garantir a correspondência.
            orgFiles = findMetaXmlFilesByName(orgDir);
        } catch (Exception ex) {
            // Falha na recuperação: lista arquivos locais e os marca como ausentes na org
            String details = "Falha ao recuperar " + entry.apiName()
                + " da Org " + orgNumber + ": " + shortMsg(ex);
            Map<String, Path> localFilesOnError =
                findMetaXmlFilesByName(Paths.get(basePath).resolve(entry.folderPath()));
            for (String name : localFilesOnError.keySet()) {
                differences.add(buildDiff(
                    orgNumber == 1
                        ? Difference.DifferenceType.METADATA_MISSING_IN_ORG1
                        : Difference.DifferenceType.METADATA_MISSING_IN_ORG2,
                    entry.apiName(), name, "Absent", details));
            }
            return;
        }

        // PASSO 2: listar arquivos do repositório local para o tipo
        progress("Comparando " + entry.apiName() + " com o repositório local...");
        Path localFolder = Paths.get(basePath).resolve(entry.folderPath());
        Map<String, Path> localFiles = findMetaXmlFilesByName(localFolder);

        // Nada a comparar se ambos os lados estão vazios
        if (orgFiles.isEmpty() && localFiles.isEmpty()) return;

        // PASSO 3: Presentes na org mas ausentes no repositório local
        for (String name : orgFiles.keySet()) {
            if (!localFiles.containsKey(name)) {
                differences.add(buildDiff(
                    orgNumber == 1
                        ? Difference.DifferenceType.METADATA_MISSING_IN_ORG2
                        : Difference.DifferenceType.METADATA_MISSING_IN_ORG1,
                    entry.apiName(), name, "Present",
                    "Presente na Org " + orgNumber + ", ausente no repositório local."));
            }
        }

        // PASSO 4: Presentes no repositório local mas ausentes na org
        for (String name : localFiles.keySet()) {
            if (!orgFiles.containsKey(name)) {
                differences.add(buildDiff(
                    orgNumber == 1
                        ? Difference.DifferenceType.METADATA_MISSING_IN_ORG1
                        : Difference.DifferenceType.METADATA_MISSING_IN_ORG2,
                    entry.apiName(), name, "Absent",
                    "Presente no repositório local, ausente na Org " + orgNumber + "."));
            }
        }

        // PASSO 5: Presentes em ambos — comparar conteúdo
        for (Map.Entry<String, Path> orgEntry : orgFiles.entrySet()) {
            checkCancelled();
            String name = orgEntry.getKey();
            if (!localFiles.containsKey(name)) continue;
            compareFileContent(entry.apiName(), name, localFiles.get(name), orgEntry.getValue());
        }
    }

    // -----------------------------------------------------------------------
    // Comparação de conteúdo de arquivo
    // -----------------------------------------------------------------------

    private void compareFileContent(String category, String name,
                                     Path local, Path org) throws IOException {
        long localSize = Files.size(local);
        long orgSize   = Files.size(org);

        // Para arquivos muito grandes: heurística de tamanho de bytes (sem normalização nem diff viewer)
        if (localSize > 5_000_000L || orgSize > 5_000_000L) {
            if (localSize != orgSize) {
                String details = String.format("Local: %,d bytes | Org %d: %,d bytes",
                    localSize, orgNumber, orgSize);
                Difference d = new Difference(Difference.DifferenceType.METADATA_STRUCTURE_MISMATCH,
                    category, name,
                    orgNumber == 1 ? "Divergent" : "-",
                    orgNumber == 2 ? "Divergent" : "-");
                d.setDetails(details);
                differences.add(d);
            }
            return;
        }

        // Lê e normaliza o conteúdo antes de comparar:
        //   1. CRLF/CR → LF
        //   2. Remove trailing whitespace de cada linha
        // Elimina falsos positivos causados por diferenças de encoding de nova linha ou
        // por espaços/tabs finais invisíveis adicionados pelo Salesforce CLI.
        String localContent = normalizeContent(new String(Files.readAllBytes(local), StandardCharsets.UTF_8));
        String orgContent   = normalizeContent(new String(Files.readAllBytes(org),   StandardCharsets.UTF_8));

        // stripTrailing() ignora também newlines finais divergentes (ex.: arquivo local
        // sem '\n' final vs arquivo da org com '\n' final).
        if (localContent.stripTrailing().equals(orgContent.stripTrailing())) return;

        String details = String.format("Local: %,d bytes | Org %d: %,d bytes",
            localSize, orgNumber, orgSize);
        Difference d = new Difference(Difference.DifferenceType.METADATA_STRUCTURE_MISMATCH,
            category, name,
            orgNumber == 1 ? "Divergent" : "-",
            orgNumber == 2 ? "Divergent" : "-");
        d.setDetails(details);
        // Armazena conteúdo normalizado para o diff viewer
        if (localSize <= MAX_DIFF_CONTENT_BYTES && orgSize <= MAX_DIFF_CONTENT_BYTES) {
            d.setContent1(localContent);
            d.setContent2(orgContent);
        }
        differences.add(d);
    }

    // -----------------------------------------------------------------------
    // Utilitários
    // -----------------------------------------------------------------------

    /**
     * Constrói uma {@link Difference} colocando {@code orgStatus} na coluna correspondente
     * a {@code orgNumber} e "-" na outra coluna.
     */
    private Difference buildDiff(Difference.DifferenceType type,
                                  String category, String name,
                                  String orgStatus, String details) {
        Difference d = new Difference(type, category, name,
            orgNumber == 1 ? orgStatus : "-",
            orgNumber == 2 ? orgStatus : "-");
        if (details != null && !details.isBlank()) d.setDetails(details);
        return d;
    }

    /**
     * Percorre recursivamente {@code dir} e retorna todos os arquivos {@code *-meta.xml}
     * com chave = <strong>nome completo do arquivo</strong>, incluindo o sufixo
     * {@code "-meta.xml"} (ex.: {@code "BRAT_PS_RECORD_ALERT.permissionset-meta.xml"}).
     * <p>
     * Manter o sufixo garante que a chave represente fielmente o arquivo comparado
     * (evita ambiguidade com arquivos de código homônimos, como {@code .trigger} vs
     * {@code .trigger-meta.xml}) e que o nome exibido na tabela de resultados seja preciso.
     * <p>
     * Usar apenas o nome do arquivo (sem caminho relativo) garante que a correspondência
     * funcione independentemente de onde o CLI Salesforce deposita os arquivos dentro do
     * diretório temporário de recuperação.
     */
    private static Map<String, Path> findMetaXmlFilesByName(Path dir) {
        Map<String, Path> map = new LinkedHashMap<>();
        if (!Files.isDirectory(dir)) return map;
        try {
            Files.walk(dir)
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.getFileName().toString().endsWith("-meta.xml"))
                .sorted()
                .forEach(p -> {
                    String fn = p.getFileName().toString();
                    // Usa o nome completo (com -meta.xml) como chave para que o
                    // resultado exibido reflita exatamente o arquivo comparado.
                    // putIfAbsent: em caso de colisão de nome, mantém o primeiro encontrado.
                    map.putIfAbsent(fn, p);
                });
        } catch (IOException e) { /* ignorar */ }
        return map;
    }

    private void progress(String msg) { progressCallback.accept(msg); }

    private void checkCancelled() throws InterruptedException {
        if (cancelFlag != null && cancelFlag.get())
            throw new InterruptedException("Operação cancelada pelo usuário.");
    }

    private static String shortMsg(Exception e) {
        String m = e.getMessage();
        if (m == null) return e.getClass().getSimpleName();
        return m.length() > 250 ? m.substring(0, 250) + "..." : m;
    }

    /**
     * Normaliza o conteúdo de um arquivo texto para comparação e armazenamento:
     * <ol>
     *   <li>Converte terminadores de linha CRLF/CR para LF.</li>
     *   <li>Remove espaços e tabs no final de cada linha (trailing whitespace).</li>
     * </ol>
     * Elimina falsos positivos causados apenas por diferenças de formatação irrelevante
     * inseridas por ferramentas externas (ex.: Salesforce CLI, Git, editores).
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
}
