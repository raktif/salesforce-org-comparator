package com.sfcomparator.model;

public class OrgConnection {
    private String label;
    private String instanceUrl;
    private String clientId;
    private String clientSecret;
    private String securityToken;
    private String accessToken;

    public OrgConnection(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
    public String getInstanceUrl() { return instanceUrl; }
    public void setInstanceUrl(String instanceUrl) {
        // Normalize URL
        if (instanceUrl != null) {
            instanceUrl = instanceUrl.trim();
            if (instanceUrl.endsWith("/")) {
                instanceUrl = instanceUrl.substring(0, instanceUrl.length() - 1);
            }
        }
        this.instanceUrl = instanceUrl;
    }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getSecurityToken() { return securityToken; }
    public void setSecurityToken(String securityToken) { this.securityToken = securityToken; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}
