package com.sfcomparator.cli;

import org.json.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Executa comandos do Salesforce CLI (sf) como subprocesso.
 * Gerencia o ciclo de vida de duas orgs temporárias registradas via access token OAuth.
 */
public class CLIExecutor implements AutoCloseable {

    private final String org1Alias;
    private final String org2Alias;
    private final List<Path> tempDirs = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> orgInstanceUrls = new HashMap<>();
    private final Map<String, String> orgAccessTokens  = new HashMap<>();

    public CLIExecutor() {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        this.org1Alias = "sfcomp-org1-" + uid;
        this.org2Alias = "sfcomp-org2-" + uid;
    }

    // -----------------------------------------------------------------------
    // Verificação de disponibilidade do CLI
    // -----------------------------------------------------------------------

    public static void checkAvailable() throws Exception {
        try {
            ProcessBuilder pb = makePB("--version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.getInputStream().readAllBytes();
            int code = p.waitFor();
            if (code != 0) {
                throw new Exception("sf CLI retornou código de erro " + code + " ao executar --version.");
            }
        } catch (IOException e) {
            throw new Exception(
                "Salesforce CLI (sf) não encontrado no PATH do sistema.\n\n" +
                "Por favor, instale o Salesforce CLI:\n" +
                "  https://developer.salesforce.com/tools/salesforcecli\n\n" +
                "Após instalar, reinicie o software.");
        }
    }

    // -----------------------------------------------------------------------
    // Registro de orgs via access token
    // -----------------------------------------------------------------------

    public void registerOrg1(String instanceUrl, String accessToken) throws Exception {
        registerOrg(org1Alias, instanceUrl, accessToken);
    }

    public void registerOrg2(String instanceUrl, String accessToken) throws Exception {
        registerOrg(org2Alias, instanceUrl, accessToken);
    }

    private void registerOrg(String alias, String instanceUrl, String accessToken) throws Exception {
        ProcessBuilder pb = makePB(
            "org", "login", "access-token",
            "--instance-url", instanceUrl,
            "--alias", alias,
            "--no-prompt", "--json");
        pb.redirectErrorStream(true);
        pb.environment().put("SF_ACCESS_TOKEN", accessToken);
        CLIResult res = run(pb);
        if (res.status != 0) {
            throw new Exception(
                "Falha ao registrar org '" + alias + "' no Salesforce CLI.\n" +
                "Verifique se o CLI está instalado e no PATH.\n\n" +
                "Detalhe: " + res.message);
        }
        // Armazenar credenciais para paginação REST direta
        orgInstanceUrls.put(alias, instanceUrl);
        orgAccessTokens.put(alias, accessToken);
    }

    // -----------------------------------------------------------------------
    // Consultas SOQL / Tooling
    // -----------------------------------------------------------------------

    public JSONArray queryOrg1SOQL(String soql)        throws Exception { return query(org1Alias, soql, false); }
    public JSONArray queryOrg2SOQL(String soql)        throws Exception { return query(org2Alias, soql, false); }
    public JSONArray queryOrg1ToolingSOQL(String soql) throws Exception { return query(org1Alias, soql, true);  }
    public JSONArray queryOrg2ToolingSOQL(String soql) throws Exception { return query(org2Alias, soql, true);  }

    /** Executa o mesmo SOQL em ambas as orgs em paralelo. Retorna [records1, records2]. */
    public JSONArray[] queryBothOrgsSOQL(String soql) throws Exception {
        return queryBothOrgs(soql, false);
    }

    /** Executa o mesmo SOQL Tooling em ambas as orgs em paralelo. Retorna [records1, records2]. */
    public JSONArray[] queryBothOrgsToolingSOQL(String soql) throws Exception {
        return queryBothOrgs(soql, true);
    }

    private JSONArray[] queryBothOrgs(String soql, boolean tooling) throws Exception {
        CompletableFuture<JSONArray> f1 = CompletableFuture.supplyAsync(() -> {
            try { return query(org1Alias, soql, tooling); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        CompletableFuture<JSONArray> f2 = CompletableFuture.supplyAsync(() -> {
            try { return query(org2Alias, soql, tooling); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        try {
            return new JSONArray[] { f1.get(), f2.get() };
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            throw new Exception(cause.getMessage(), cause);
        }
    }

    private JSONArray query(String alias, String soql, boolean tooling) throws Exception {
        List<String> args = new ArrayList<>(Arrays.asList(
            "data", "query",
            "--query", soql,
            "--target-org", alias,
            "--json"));
        if (tooling) args.add("--use-tooling-api");
        ProcessBuilder pb = makePB(args.toArray(new String[0]));
        pb.redirectErrorStream(true);
        CLIResult res = run(pb);
        if (res.status != 0) {
            throw new Exception("Erro na consulta" + (tooling ? " Tooling" : "") +
                " SOQL:\n" + res.message);
        }
        if (res.json == null || !res.json.has("result")) return new JSONArray();

        JSONObject r       = res.json.getJSONObject("result");
        if (!r.has("records")) return new JSONArray();

        JSONArray all      = new JSONArray();
        JSONArray firstBatch = r.getJSONArray("records");
        for (int i = 0; i < firstBatch.length(); i++) all.put(firstBatch.get(i));

        // Paginação: seguir nextRecordsUrl via REST direta se o CLI não paginou
        boolean done = r.optBoolean("done", true);
        String  next = r.has("nextRecordsUrl") ? r.getString("nextRecordsUrl") : null;

        while (!done && next != null && !next.isBlank()) {
            String instanceUrl  = orgInstanceUrls.get(alias);
            String accessToken  = orgAccessTokens.get(alias);
            if (instanceUrl == null || accessToken == null) break;

            JSONObject page     = fetchNextPage(instanceUrl, accessToken, next);
            JSONArray  pageRecs = page.getJSONArray("records");
            for (int i = 0; i < pageRecs.length(); i++) all.put(pageRecs.get(i));

            done = page.optBoolean("done", true);
            next = page.has("nextRecordsUrl") ? page.getString("nextRecordsUrl") : null;
        }
        return all;
    }

    /** Realiza uma chamada REST GET direta para seguir a paginação SOQL (nextRecordsUrl). */
    private JSONObject fetchNextPage(String instanceUrl, String accessToken,
                                     String nextRecordsUrl) throws Exception {
        String fullUrl = nextRecordsUrl.startsWith("http")
            ? nextRecordsUrl : instanceUrl + nextRecordsUrl;
        URL url = URI.create(fullUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(60_000);
        int httpStatus = conn.getResponseCode();
        InputStream stream = httpStatus >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String body = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        if (httpStatus >= 400) {
            throw new Exception("Erro ao buscar próxima página SOQL (HTTP " + httpStatus + "):\n" + body);
        }
        return new JSONObject(body);
    }

    // -----------------------------------------------------------------------
    // Describe SObject
    // -----------------------------------------------------------------------

    public JSONObject describeOrg1SObject(String sobject) throws Exception { return describe(org1Alias, sobject); }
    public JSONObject describeOrg2SObject(String sobject) throws Exception { return describe(org2Alias, sobject); }

    private JSONObject describe(String alias, String sobject) throws Exception {
        ProcessBuilder pb = makePB(
            "sobject", "describe",
            "--sobject", sobject,
            "--target-org", alias,
            "--json");
        pb.redirectErrorStream(true);
        CLIResult res = run(pb);
        if (res.status != 0) {
            throw new Exception("Erro ao descrever SObject '" + sobject + "':\n" + res.message);
        }
        if (res.json != null && res.json.has("result")) {
            return res.json.getJSONObject("result");
        }
        throw new Exception("Resposta inesperada ao descrever '" + sobject + "': " +
            res.rawOutput.substring(0, Math.min(300, res.rawOutput.length())));
    }

    // -----------------------------------------------------------------------
    // Recuperação de metadados (source format via 'sf project retrieve start')
    // Resultado: tmpDir/retrieved/**/<type>/<name>.<ext>-meta.xml
    // -----------------------------------------------------------------------

    public Path retrieveOrg1Metadata(String... specs) throws Exception { return retrieve(org1Alias, specs); }
    public Path retrieveOrg2Metadata(String... specs) throws Exception { return retrieve(org2Alias, specs); }

    /** Recupera metadados de ambas as orgs em paralelo. Retorna [dir1, dir2]. */
    public Path[] retrieveBothOrgsMetadata(String... specs) throws Exception {
        CompletableFuture<Path> f1 = CompletableFuture.supplyAsync(() -> {
            try { return retrieve(org1Alias, specs); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        CompletableFuture<Path> f2 = CompletableFuture.supplyAsync(() -> {
            try { return retrieve(org2Alias, specs); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        try {
            return new Path[] { f1.get(), f2.get() };
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            throw new Exception(cause.getMessage(), cause);
        }
    }

    private Path retrieve(String alias, String... specs) throws Exception {
        Path tmpDir = Files.createTempDirectory("sfcomp-meta-");
        tempDirs.add(tmpDir);

        // sfdx-project.json é necessário para comandos 'sf project ...'
        // A pasta referenciada (force-app) também deve existir.
        Files.writeString(tmpDir.resolve("sfdx-project.json"),
            "{\"packageDirectories\":[{\"path\":\"force-app\",\"default\":true}],\"sourceApiVersion\":\"59.0\"}\n",
            StandardCharsets.UTF_8);
        Files.createDirectories(tmpDir.resolve("force-app"));

        List<String> args = new ArrayList<>(Arrays.asList(
            "project", "retrieve", "start",
            "--target-org", alias,
            "--output-dir", "retrieved",
            "--wait", "10",
            "--json"));
        for (String s : specs) { args.add("--metadata"); args.add(s); }

        ProcessBuilder pb = makePB(args.toArray(new String[0]));
        pb.directory(tmpDir.toFile());
        pb.redirectErrorStream(true);
        CLIResult res = run(pb);
        if (res.status != 0) {
            throw new Exception(
                "Erro ao recuperar metadados [" + String.join(", ", specs) + "]:\n" + res.message);
        }
        return tmpDir;
    }

    // -----------------------------------------------------------------------
    // Limpeza
    // -----------------------------------------------------------------------

    @Override
    public void close() {
        logout(org1Alias);
        logout(org2Alias);
        for (Path d : tempDirs) deleteDir(d);
        orgInstanceUrls.clear();
        orgAccessTokens.clear();
    }

    private void logout(String alias) {
        try {
            ProcessBuilder pb = makePB("org", "logout", "--target-org", alias, "--no-prompt");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.getInputStream().readAllBytes();
            p.waitFor();
        } catch (Exception e) { /* ignorar erros de limpeza */ }
    }

    // -----------------------------------------------------------------------
    // Utilitários internos
    // -----------------------------------------------------------------------

    private static ProcessBuilder makePB(String... args) {
        boolean win = System.getProperty("os.name", "").toLowerCase().contains("win");
        List<String> cmd = new ArrayList<>();
        cmd.add(win ? "sf.cmd" : "sf");
        cmd.addAll(Arrays.asList(args));
        return new ProcessBuilder(cmd);
    }

    static CLIResult run(ProcessBuilder pb) throws Exception {
        Process p = pb.start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        p.waitFor();
        CLIResult r = new CLIResult();
        r.rawOutput = out;
        try {
            // O CLI pode emitir warnings de Node.js antes do JSON.
            // Localiza o primeiro '{' para extrair apenas o bloco JSON.
            int jsonStart = out.indexOf('{');
            String jsonPart = jsonStart >= 0 ? out.substring(jsonStart) : out;
            r.json    = new JSONObject(jsonPart);
            r.status  = r.json.optInt("status", 0);
            r.message = r.json.optString("message", "");
        } catch (Exception e) {
            r.status  = 1;
            r.message = out.length() > 500 ? out.substring(0, 500) : out;
        }
        return r;
    }

    private static void deleteDir(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException {
                    Files.delete(f);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
                    Files.delete(d);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) { /* ignorar */ }
    }

    static class CLIResult {
        int status   = 0;
        String message   = "";
        String rawOutput = "";
        JSONObject json;
    }
}
