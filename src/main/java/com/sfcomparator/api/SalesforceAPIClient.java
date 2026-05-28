package com.sfcomparator.api;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONObject;
import com.sfcomparator.model.OrgConnection;

public class SalesforceAPIClient {
    private final OrgConnection connection;
    /** Usado exclusivamente para o POST de autenticaÃ§Ã£o OAuth. NÃƒO segue redirects automaticamente,
     *  para preservar o mÃ©todo POST e o body nos redirecionamentos do My Domain. */
    private final HttpClient authClient;

    public SalesforceAPIClient(OrgConnection connection) {
        this.connection = connection;
        SSLContext ctx = buildTrustingSSLContext();
        this.authClient = HttpClient.newBuilder()
                .sslContext(ctx)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * Cria um SSLContext que aceita qualquer certificado SSL.
     * NecessÃ¡rio para orgs Salesforce com domÃ­nios customizados (My Domain)
     * cujos certificados intermediÃ¡rios podem nÃ£o estar no cacerts do Java.
     */
    private SSLContext buildTrustingSSLContext() {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] c, String a) {}
                    public void checkServerTrusted(X509Certificate[] c, String a) {}
                }
            };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAll, new SecureRandom());
            return ctx;
        } catch (Exception e) {
            try { return SSLContext.getDefault(); } catch (Exception ex) { throw new RuntimeException(ex); }
        }
    }

    /**
     * Autentica na org Salesforce via grant_type=client_credentials.
     *
     * Redirects 301/302/307/308 sÃ£o seguidos manualmente preservando
     * o mÃ©todo POST e o body (o HttpClient do Java converte POST em GET
     * ao seguir 302 automaticamente, o que quebra o fluxo OAuth).
     */
    public boolean authenticate() throws Exception {
        String body = buildAuthBody();
        String url  = connection.getInstanceUrl() + "/services/oauth2/token";

        for (int attempt = 0; attempt < 5; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    authClient.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status == 200) {
                JSONObject json = new JSONObject(response.body());
                connection.setAccessToken(json.getString("access_token"));
                // Atualiza instance_url com o valor retornado pelo OAuth
                // (pode diferir da URL informada, especialmente em sandboxes com My Domain)
                if (json.has("instance_url")) {
                    connection.setInstanceUrl(json.getString("instance_url"));
                }
                return true;
            }

            // Seguir redirects manualmente para preservar o POST
            if (status == 301 || status == 302 || status == 307 || status == 308) {
                String location = response.headers()
                        .firstValue("Location")
                        .or(() -> response.headers().firstValue("location"))
                        .orElseThrow(() -> new Exception(
                            "Redirecionamento sem header Location [HTTP " + status + "]."));
                url = location;
                continue;
            }

            throw new Exception("AutenticaÃ§Ã£o falhou [HTTP " + status + "]:\n" + response.body());
        }

        throw new Exception(
            "AutenticaÃ§Ã£o falhou: muitos redirecionamentos.\n"
            + "Verifique se a Instance URL estÃ¡ correta (ex: https://login.salesforce.com "
            + "ou https://test.salesforce.com).");
    }

    /**
     * Monta o body do request de autenticaÃ§Ã£o conforme as credenciais disponÃ­veis.
     */
    private String buildAuthBody() throws Exception {
        String clientId = notEmpty(connection.getClientId());
        String secret   = notEmpty(connection.getClientSecret());

        if (clientId == null || secret == null) {
            throw new Exception(
                "Credenciais insuficientes.\n"
                + "Preencha a Chave do Consumidor (Client ID) e o Segredo do Consumidor (Client Secret).");
        }

        return "grant_type=client_credentials"
             + "&client_id="     + enc(clientId)
             + "&client_secret=" + enc(secret);
    }

    /** Retorna null se a string for nula ou em branco. */
    private String notEmpty(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static String enc(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
