package com.sfcomparator.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class OrgConnectionPanel extends JPanel {
    /** Chave de idioma usada para o título do painel (ex.: "org1.title"). */
    private final String orgTitleKey;
    private TitledBorder titledBorder;
    private JLabel instanceLabel;
    private JLabel tokenLabel;
    private JLabel clientIdLabel;
    private JLabel clientSecretLabel;
    private JTextField instanceUrlField;
    private JPasswordField securityTokenField;
    private JPasswordField clientIdField;
    private JPasswordField clientSecretField;

    /**
     * @param orgTitleKey chave de idioma para o título do painel,
     *                    ex.: "org1.title" ou "org2.title"
     */
    public OrgConnectionPanel(String orgTitleKey) {
        this.orgTitleKey = orgTitleKey;
        setupUI();
        LanguageManager.addChangeListener(this::applyLanguage);
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        titledBorder = BorderFactory.createTitledBorder(LanguageManager.get(orgTitleKey));
        setBorder(titledBorder);
        setBackground(new Color(245, 245, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Instance URL
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        instanceLabel = new JLabel(LanguageManager.get("lbl.instanceUrl"));
        instanceLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        add(instanceLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        instanceUrlField = new JTextField(20);
        instanceUrlField.setFont(new Font("Arial", Font.PLAIN, 11));
        instanceUrlField.setToolTipText(LanguageManager.get("tip.instanceUrl"));
        add(instanceUrlField, gbc);

        // Security Token
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        tokenLabel = new JLabel(LanguageManager.get("lbl.securityToken"));
        tokenLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        add(tokenLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        securityTokenField = new JPasswordField(20);
        securityTokenField.setFont(new Font("Arial", Font.PLAIN, 11));
        securityTokenField.setToolTipText(LanguageManager.get("tip.securityToken"));
        add(securityTokenField, gbc);

        // Client ID
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        clientIdLabel = new JLabel(LanguageManager.get("lbl.clientId"));
        clientIdLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        add(clientIdLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        clientIdField = new JPasswordField(20);
        clientIdField.setFont(new Font("Arial", Font.PLAIN, 11));
        clientIdField.setToolTipText(LanguageManager.get("tip.clientId"));
        add(clientIdField, gbc);

        // Client Secret
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        clientSecretLabel = new JLabel(LanguageManager.get("lbl.clientSecret"));
        clientSecretLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        add(clientSecretLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        clientSecretField = new JPasswordField(20);
        clientSecretField.setFont(new Font("Arial", Font.PLAIN, 11));
        clientSecretField.setToolTipText(LanguageManager.get("tip.clientSecret"));
        add(clientSecretField, gbc);
    }

    /** Atualiza todos os textos do painel para o idioma atual. */
    public void applyLanguage() {
        titledBorder.setTitle(LanguageManager.get(orgTitleKey));
        repaint();
        instanceLabel.setText(LanguageManager.get("lbl.instanceUrl"));
        tokenLabel.setText(LanguageManager.get("lbl.securityToken"));
        clientIdLabel.setText(LanguageManager.get("lbl.clientId"));
        clientSecretLabel.setText(LanguageManager.get("lbl.clientSecret"));
        instanceUrlField.setToolTipText(LanguageManager.get("tip.instanceUrl"));
        securityTokenField.setToolTipText(LanguageManager.get("tip.securityToken"));
        clientIdField.setToolTipText(LanguageManager.get("tip.clientId"));
        clientSecretField.setToolTipText(LanguageManager.get("tip.clientSecret"));
    }

    public String getInstanceUrl() { return instanceUrlField.getText(); }
    public String getSecurityToken() { return new String(securityTokenField.getPassword()); }
    public String getClientId() { return new String(clientIdField.getPassword()); }
    public String getClientSecret() { return new String(clientSecretField.getPassword()); }

    public void setInstanceUrl(String url) { instanceUrlField.setText(url); }
    public void setSecurityToken(String token) { securityTokenField.setText(token); }
    public void setClientId(String clientId) { clientIdField.setText(clientId); }
    public void setClientSecret(String clientSecret) { clientSecretField.setText(clientSecret); }
}
