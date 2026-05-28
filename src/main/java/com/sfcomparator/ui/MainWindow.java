package com.sfcomparator.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.sfcomparator.api.SalesforceAPIClient;
import com.sfcomparator.cli.CLIExecutor;
import com.sfcomparator.comparator.ComparisonEngine;
import com.sfcomparator.model.ComparisonConfig;
import com.sfcomparator.model.Difference;
import com.sfcomparator.model.OrgConnection;
import com.sfcomparator.util.SettingsManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.json.JSONObject;

public class MainWindow extends JFrame {
    private OrgConnectionPanel org1Panel;
    private OrgConnectionPanel org2Panel;
    private ComparisonConfigPanel configPanel;
    private ResultsPanel resultsPanel;
    private JButton compareButton;
    private JButton loadSettingsButton;
    private JButton saveSettingsButton;
    private JButton languageButton;
    private JButton helpButton;
    private JLabel headerLabel;
    private JTabbedPane tabbedPane;
    private HelpWindow helpWindow;

    public MainWindow() {
        setTitle(LanguageManager.get("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);
        setLocationRelativeTo(null);
        setResizable(true);
        setupUI();
        setupEventListeners();
        LanguageManager.addChangeListener(this::applyLanguage);
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 245));

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(70, 130, 180));
        headerLabel = new JLabel(LanguageManager.get("header.title"));
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Org connections
        JPanel connectionsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        connectionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        connectionsPanel.setBackground(new Color(240, 240, 245));

        org1Panel = new OrgConnectionPanel("org1.title");
        org2Panel = new OrgConnectionPanel("org2.title");
        connectionsPanel.add(org1Panel);
        connectionsPanel.add(org2Panel);

        // Config panel
        configPanel = new ComparisonConfigPanel();
        configPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Results panel
        resultsPanel = new ResultsPanel();
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Botão Start Comparison (permanece inalterado)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlPanel.setBackground(new Color(240, 240, 245));

        compareButton = new JButton(LanguageManager.get("btn.compare"));
        compareButton.setFont(new Font("Arial", Font.BOLD, 12));
        compareButton.setBackground(new Color(34, 139, 34));
        compareButton.setForeground(Color.WHITE);
        compareButton.setFocusPainted(false);
        compareButton.setPreferredSize(new Dimension(180, 35));

        controlPanel.add(compareButton);

        // Tabbed pane for organization layout
        tabbedPane = new JTabbedPane();

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(240, 240, 245));
        inputPanel.add(connectionsPanel, BorderLayout.NORTH);
        inputPanel.add(configPanel, BorderLayout.CENTER);

        tabbedPane.addTab(LanguageManager.get("tab.config"), inputPanel);
        tabbedPane.addTab(LanguageManager.get("tab.results"), resultsPanel);

        // Toolbar: Salvar, Carregar, Idioma, Ajuda
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBackground(new Color(248, 248, 252));
        toolBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 210)),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));

        saveSettingsButton = createToolbarButton(LanguageManager.get("toolbar.save"), createSaveIcon());
        toolBar.add(saveSettingsButton);
        toolBar.addSeparator(new Dimension(6, 24));

        loadSettingsButton = createToolbarButton(LanguageManager.get("toolbar.load"), createLoadIcon());
        toolBar.add(loadSettingsButton);
        toolBar.addSeparator(new Dimension(6, 24));

        languageButton = createToolbarButton(LanguageManager.get("toolbar.lang"), createLanguageIcon());
        languageButton.addActionListener(e -> {
            LanguageManager.Language newLang =
                LanguageManager.getLanguage() == LanguageManager.Language.PT_BR
                    ? LanguageManager.Language.EN
                    : LanguageManager.Language.PT_BR;
            LanguageManager.setLanguage(newLang);
        });
        toolBar.add(languageButton);
        toolBar.addSeparator(new Dimension(6, 24));

        helpButton = createToolbarButton(LanguageManager.get("toolbar.help"), createHelpIcon());
        helpButton.addActionListener(e -> {
            if (helpWindow == null || !helpWindow.isDisplayable()) {
                helpWindow = new HelpWindow(this);
            }
            helpWindow.setVisible(true);
            helpWindow.toFront();
        });
        toolBar.add(helpButton);

        JPanel northArea = new JPanel(new BorderLayout());
        northArea.add(headerPanel, BorderLayout.NORTH);
        northArea.add(toolBar, BorderLayout.SOUTH);

        // Main layout
        mainPanel.add(northArea, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void setupEventListeners() {
        compareButton.addActionListener(e -> executeComparison());
        resultsPanel.getExportButton().addActionListener(e -> exportResults());
        resultsPanel.getClearButton().addActionListener(e -> resultsPanel.clearResults());
        saveSettingsButton.addActionListener(e -> saveSettings());
        loadSettingsButton.addActionListener(e -> loadSettings());
    }

    private void executeComparison() {
        // Validações antes de iniciar a thread
        boolean hasSobject       = !configPanel.getSobjectName().isEmpty();
        boolean hasObjectBound   = configPanel.isCompareObjectFields()
                                || configPanel.isComparePageLayouts()
                                || configPanel.isCompareValidationRules()
                                || configPanel.isCompareFlows()
                                || configPanel.isCompareApexTriggers()
                                || configPanel.isCompareRecordTypes();
        boolean hasOrgWide       = configPanel.isCompareCustomMetadata()
                                || configPanel.isCompareCustomSettings()
                                || configPanel.isComparePermissionSets()
                                || configPanel.isCompareProfiles()
                                || configPanel.isCompareGroups()
                                || configPanel.isCompareApprovalProcesses()
                                || configPanel.isCompareNamedCredentials();

        if (!hasSobject && !hasOrgWide && !hasObjectBound) {
            JOptionPane.showMessageDialog(this,
                LanguageManager.get("val.nothingSelected"),
                LanguageManager.get("val.nothingTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!hasSobject && hasObjectBound) {
            int opt = JOptionPane.showConfirmDialog(this,
                LanguageManager.get("val.noSobjectMsg"),
                LanguageManager.get("val.noSobjectTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opt != JOptionPane.YES_OPTION) return;
        }
        if (org1Panel.getInstanceUrl().isBlank() || org2Panel.getInstanceUrl().isBlank()) {
            JOptionPane.showMessageDialog(this,
                LanguageManager.get("val.instanceRequired"),
                LanguageManager.get("val.requiredField"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (org1Panel.getClientId().isBlank() || org2Panel.getClientId().isBlank()) {
            JOptionPane.showMessageDialog(this,
                LanguageManager.get("val.clientIdRequired"),
                LanguageManager.get("val.requiredField"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Desabilitar botão e exibir janela de progresso (na EDT)
        compareButton.setEnabled(false);
        compareButton.setText(LanguageManager.get("btn.comparing"));

        AtomicBoolean cancelled = new AtomicBoolean(false);

        JDialog progressDialog = new JDialog(this, LanguageManager.get("dlg.progress.title"), false);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setResizable(false);
        JPanel progressContent = new JPanel(new BorderLayout(10, 14));
        progressContent.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        // Subclasse anônima: qualquer setText é envolvido em HTML para evitar truncamento
        JLabel statusLabel = new JLabel() {
            @Override public void setText(String t) {
                if (t != null && !t.startsWith("<html>"))
                    t = "<html><div style='text-align:center'>" + t + "</div></html>";
                super.setText(t);
            }
        };
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statusLabel.setPreferredSize(new Dimension(500, 46));
        statusLabel.setText(LanguageManager.get("dlg.status.startAuth"));
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JButton cancelButton = new JButton(LanguageManager.get("dlg.cancel"));
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton.setFocusPainted(false);
        cancelButton.setOpaque(true);
        cancelButton.setBorderPainted(false);
        JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        cancelPanel.setOpaque(false);
        cancelPanel.add(cancelButton);
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 6));
        bottomPanel.setOpaque(false);
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        bottomPanel.add(cancelPanel, BorderLayout.SOUTH);
        progressContent.add(statusLabel, BorderLayout.CENTER);
        progressContent.add(bottomPanel, BorderLayout.SOUTH);
        progressDialog.add(progressContent);
        progressDialog.pack();
        progressDialog.setMinimumSize(new Dimension(560, 165));
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setVisible(true);

        Thread[] taskThread = {null};
        cancelButton.addActionListener(e -> {
            cancelled.set(true);
            cancelButton.setEnabled(false);
            statusLabel.setText(LanguageManager.get("dlg.status.cancelling"));
        });

        taskThread[0] = new Thread(() -> {
            CLIExecutor cli = null;
            try {
                OrgConnection org1Connection = new OrgConnection("Org 1");
                org1Connection.setInstanceUrl(org1Panel.getInstanceUrl());
                org1Connection.setSecurityToken(org1Panel.getSecurityToken());
                org1Connection.setClientId(org1Panel.getClientId());
                org1Connection.setClientSecret(org1Panel.getClientSecret());

                OrgConnection org2Connection = new OrgConnection("Org 2");
                org2Connection.setInstanceUrl(org2Panel.getInstanceUrl());
                org2Connection.setSecurityToken(org2Panel.getSecurityToken());
                org2Connection.setClientId(org2Panel.getClientId());
                org2Connection.setClientSecret(org2Panel.getClientSecret());

                SwingUtilities.invokeLater(() -> statusLabel.setText(LanguageManager.get("dlg.status.authOrg1")));
                SalesforceAPIClient org1Client = new SalesforceAPIClient(org1Connection);
                org1Client.authenticate();

                SwingUtilities.invokeLater(() -> statusLabel.setText(LanguageManager.get("dlg.status.authOrg2")));
                SalesforceAPIClient org2Client = new SalesforceAPIClient(org2Connection);
                org2Client.authenticate();

                // Registrar orgs no Salesforce CLI usando o access token obtido via OAuth
                SwingUtilities.invokeLater(() -> statusLabel.setText(LanguageManager.get("dlg.status.cli")));
                CLIExecutor.checkAvailable();
                cli = new CLIExecutor();
                cli.registerOrg1(org1Connection.getInstanceUrl(), org1Connection.getAccessToken());
                cli.registerOrg2(org2Connection.getInstanceUrl(), org2Connection.getAccessToken());

                ComparisonConfig config = new ComparisonConfig();
                config.setSobjectName(configPanel.getSobjectName());
                config.setCompareObjectFields(configPanel.isCompareObjectFields());
                config.setComparePageLayouts(configPanel.isComparePageLayouts());
                config.setCompareValidationRules(configPanel.isCompareValidationRules());
                config.setCompareFlows(configPanel.isCompareFlows());
                config.setCompareApexTriggers(configPanel.isCompareApexTriggers());
                config.setCompareRecordTypes(configPanel.isCompareRecordTypes());
                config.setCompareCustomMetadata(configPanel.isCompareCustomMetadata());
                config.setCompareCustomSettings(configPanel.isCompareCustomSettings());
                config.setComparePermissionSets(configPanel.isComparePermissionSets());
                config.setCompareProfiles(configPanel.isCompareProfiles());
                config.setCompareGroups(configPanel.isCompareGroups());
                config.setCompareApprovalProcesses(configPanel.isCompareApprovalProcesses());
                config.setCompareNamedCredentials(configPanel.isCompareNamedCredentials());
                config.setPermissionSetsFilter(configPanel.getPermissionSetsFilter());
                config.setProfilesFilter(configPanel.getProfilesFilter());

                ComparisonEngine engine = new ComparisonEngine(cli, config);
                engine.setProgressCallback(msg ->
                    SwingUtilities.invokeLater(() -> statusLabel.setText(msg)));
                engine.setCancelFlag(cancelled);
                List<Difference> differences = engine.executeComparison();

                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    resultsPanel.displayResults(differences);
                    tabbedPane.setSelectedIndex(1);
                    compareButton.setEnabled(true);
                    compareButton.setText(LanguageManager.get("btn.compare"));
                    int total = differences.size();
                    String msg = total == 0
                        ? LanguageManager.get("msg.noDiff")
                        : LanguageManager.get("msg.foundDiff", total);
                    JOptionPane.showMessageDialog(this, msg,
                        LanguageManager.get("dlg.done.title"),
                        total == 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                });

            } catch (InterruptedException ie) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    compareButton.setEnabled(true);
                    compareButton.setText(LanguageManager.get("btn.compare"));
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    compareButton.setEnabled(true);
                    compareButton.setText(LanguageManager.get("btn.compare"));
                    showScrollableError(LanguageManager.get("err.comparison"), getDetailedErrorMessage(ex));
                    ex.printStackTrace();
                });
            } finally {
                if (cli != null) cli.close();
            }
        }); taskThread[0].start();
    }

    private void showScrollableError(String title, String message) {
        JDialog errorDialog = new JDialog(this, title, true);
        errorDialog.setLayout(new BorderLayout(0, 0));
        errorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 340));
        JButton okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(80, 28));
        okButton.addActionListener(e -> errorDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        buttonPanel.add(okButton);
        errorDialog.add(scrollPane, BorderLayout.CENTER);
        errorDialog.add(buttonPanel, BorderLayout.SOUTH);
        errorDialog.pack();
        errorDialog.setMinimumSize(new Dimension(480, 280));
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setResizable(true);
        errorDialog.setVisible(true);
    }

    private String getDetailedErrorMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null) message = ex.getClass().getSimpleName();

        // SSL/Certificate errors
        if (message.contains("PKIX") || message.contains("CertPath") || message.contains("SSLHandshake")) {
            return "Erro de Certificado SSL/TLS:\n\n" + message +
                   "\n\nSolução:\n" +
                   "1. Verifique se a URL da instância está correta\n" +
                   "2. Tente usar: https://login.salesforce.com (produção)\n" +
                   "3. Ou: https://test.salesforce.com (sandbox)\n" +
                   "4. Verifique a conexão de internet";
        }

        if (message.contains("invalid_app_access") || message.contains("not admin approved")) {
            return "Erro: Usuário não autorizado no Connected App.\n\n"
                 + "O perfil do usuário não está na lista de perfis autorizados\n"
                 + "do Connected App (Permitted Users = Admin approved).\n\n"
                 + "Opção 1 \u2014 Liberar para todos os usuários (mais rápido):\n"
                 + "   Setup \u2192 App Manager \u2192 [Connected App] \u2192 Manage \u2192 Edit Policies\n"
                 + "   \u2192 Permitted Users: 'All users may self-authorize'\n\n"
                 + "Opção 2 \u2014 Autorizar apenas o perfil do usuário (mais seguro):\n"
                 + "   Setup \u2192 App Manager \u2192 [Connected App] \u2192 Manage\n"
                 + "   \u2192 seção 'Profiles' \u2192 Edit \u2192 adicionar o perfil do usuário\n"
                 + "   \u2192 faça isso em AMBAS as orgs";
        }

        // invalid_grant — erro mais comum de autenticação Salesforce
        if (message.contains("invalid_grant")) {

            if (message.contains("no client credentials user enabled")) {
                return "Erro: Client Credentials Flow não habilitado corretamente.\n\n"
                     + "O usuário 'Run As' está definido, mas o perfil desse\n"
                     + "usuário não está na lista de perfis autorizados do Connected App.\n\n"
                     + "Passos para corrigir:\n"
                     + "1. Setup \u2192 App Manager \u2192 [Connected App] \u2192 Manage\n"
                     + "2. Role até a seção 'Profiles' (ou 'Permission Sets')\n"
                     + "3. Clique em 'Edit' e adicione o perfil do usuário 'Run As'\n"
                     + "4. Save e tente novamente";
            }

            return "Erro de Autenticação Salesforce (invalid_grant):\n\n"
                 + "Causas mais comuns para o Client Credentials Flow:\n\n"
                 + "1. [MAIS PROVÁVEL] Perfil do usuário 'Run As' não autorizado no Connected App:\n"
                 + "   Setup \u2192 App Manager \u2192 [Connected App] \u2192 Manage\n"
                 + "   \u2192 seção 'Profiles' \u2192 Edit \u2192 adicionar o perfil do usuário 'Run As'\n"
                 + "   \u2192 repita em AMBAS as orgs\n\n"
                 + "2. Client Credentials Flow não habilitado no Connected App:\n"
                 + "   Setup \u2192 App Manager \u2192 [Connected App] \u2192 Edit\n"
                 + "   \u2192 OAuth Settings \u2192 marcar 'Enable Client Credentials Flow'\n\n"
                 + "3. Usuário 'Run As' não configurado:\n"
                 + "   Setup \u2192 App Manager \u2192 [Connected App] \u2192 Manage \u2192 Edit Policies\n"
                 + "   \u2192 campo 'Client Credentials Flow: Run As' \u2192 selecionar o usuário de integração";
        }

        // unauthorized_client
        if (message.contains("unauthorized_client")) {
            return "Erro: Client não autorizado para este fluxo OAuth.\n\n"
                 + "Solução:\n"
                 + "1. Verifique se o Connected App permite o Client Credentials Flow\n"
                 + "2. Setup \u2192 App Manager \u2192 [Connected App] \u2192 Edit\n"
                 + "   \u2192 OAuth Settings \u2192 marcar 'Enable Client Credentials Flow'";
        }

        // Connection errors
        if (message.contains("ConnectException") || message.contains("UnknownHostException") ||
            message.contains("timeout") || message.contains("refused")) {
            return "Erro de Conexão:\n\n" + message +
                   "\n\nSolução:\n" +
                   "1. Verifique a conectividade de internet\n" +
                   "2. Verifique se a URL está correta\n" +
                   "3. Verifique firewall/proxy";
        }

        // Too many redirects
        if (message.contains("muitos redirecionamentos")) {
            return message + "\n\nDica: use https://test.salesforce.com (sandbox) ou\n"
                           + "https://login.salesforce.com (produção) como Instance URL.";
        }

        return "Erro: " + message;
    }

    private void exportResults() {
        DefaultTableModel model = resultsPanel.getTableModel();
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                LanguageManager.get("export.noResults"),
                LanguageManager.get("export.info"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("comparison_results_" + System.currentTimeMillis() + ".csv"));
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write header
                for (int i = 0; i < model.getColumnCount(); i++) {
                    writer.write(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) writer.write(",");
                }
                writer.newLine();

                // Write data
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object value = model.getValueAt(i, j);
                        String strValue = value != null ? value.toString().replace("\"", "\"\"") : "";
                        writer.write("\"" + strValue + "\"");
                        if (j < model.getColumnCount() - 1) writer.write(",");
                    }
                    writer.newLine();
                }
                writer.flush();
                JOptionPane.showMessageDialog(this,
                    LanguageManager.get("export.success"),
                    LanguageManager.get("export.info"), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    LanguageManager.get("export.error", ex.getMessage()),
                    LanguageManager.get("err.title"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveSettings() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(LanguageManager.get("dlg.save.title"));
        fc.setFileFilter(new FileNameExtensionFilter(LanguageManager.get("dlg.sfcs.filter"), "sfcs"));
        fc.setSelectedFile(new File("settings.sfcs"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".sfcs")) {
            file = new File(file.getAbsolutePath() + ".sfcs");
        }

        try {
            // Montar JSON com todos os campos do formulário
            JSONObject org1 = new JSONObject();
            org1.put("instanceUrl",   org1Panel.getInstanceUrl());
            org1.put("securityToken", org1Panel.getSecurityToken());
            org1.put("clientId",      org1Panel.getClientId());
            org1.put("clientSecret",  org1Panel.getClientSecret());

            JSONObject org2 = new JSONObject();
            org2.put("instanceUrl",   org2Panel.getInstanceUrl());
            org2.put("securityToken", org2Panel.getSecurityToken());
            org2.put("clientId",      org2Panel.getClientId());
            org2.put("clientSecret",  org2Panel.getClientSecret());

            JSONObject cfg = new JSONObject();
            cfg.put("sobjectName",           configPanel.getSobjectName());
            cfg.put("compareObjectFields",   configPanel.isCompareObjectFields());
            cfg.put("comparePageLayouts",    configPanel.isComparePageLayouts());
            cfg.put("compareValidationRules",configPanel.isCompareValidationRules());
            cfg.put("compareFlows",          configPanel.isCompareFlows());
            cfg.put("compareApexTriggers",   configPanel.isCompareApexTriggers());
            cfg.put("compareRecordTypes",     configPanel.isCompareRecordTypes());
            cfg.put("compareCustomMetadata", configPanel.isCompareCustomMetadata());
            cfg.put("compareCustomSettings", configPanel.isCompareCustomSettings());
            cfg.put("comparePermissionSets", configPanel.isComparePermissionSets());
            cfg.put("compareProfiles",       configPanel.isCompareProfiles());
            cfg.put("compareGroups",         configPanel.isCompareGroups());
            cfg.put("compareApprovalProcesses", configPanel.isCompareApprovalProcesses());
            cfg.put("compareNamedCredentials",  configPanel.isCompareNamedCredentials());
            cfg.put("permissionSetsFilter",      configPanel.getPermissionSetsFilter());
            cfg.put("profilesFilter",            configPanel.getProfilesFilter());

            JSONObject root = new JSONObject();
            root.put("org1",   org1);
            root.put("org2",   org2);
            root.put("config", cfg);

            new SettingsManager().saveToFile(root.toString(), file);

            JOptionPane.showMessageDialog(this,
                LanguageManager.get("msg.saved", file.getAbsolutePath()),
                LanguageManager.get("msg.savedTitle"), JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                LanguageManager.get("msg.saveError", ex.getMessage()),
                LanguageManager.get("err.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSettings() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(LanguageManager.get("dlg.load.title"));
        fc.setFileFilter(new FileNameExtensionFilter(LanguageManager.get("dlg.sfcs.filter"), "sfcs"));

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            String json = new SettingsManager().loadFromFile(fc.getSelectedFile());
            JSONObject root = new JSONObject(json);

            // Org 1
            JSONObject org1 = root.getJSONObject("org1");
            org1Panel.setInstanceUrl(org1.optString("instanceUrl", ""));
            org1Panel.setSecurityToken(org1.optString("securityToken", ""));
            org1Panel.setClientId(org1.optString("clientId", ""));
            org1Panel.setClientSecret(org1.optString("clientSecret", ""));

            // Org 2
            JSONObject org2 = root.getJSONObject("org2");
            org2Panel.setInstanceUrl(org2.optString("instanceUrl", ""));
            org2Panel.setSecurityToken(org2.optString("securityToken", ""));
            org2Panel.setClientId(org2.optString("clientId", ""));
            org2Panel.setClientSecret(org2.optString("clientSecret", ""));

            // Config
            JSONObject cfg = root.getJSONObject("config");
            configPanel.setSobjectName(cfg.optString("sobjectName", ""));
            configPanel.setCompareObjectFields(cfg.optBoolean("compareObjectFields", false));
            configPanel.setComparePageLayouts(cfg.optBoolean("comparePageLayouts", false));
            configPanel.setCompareValidationRules(cfg.optBoolean("compareValidationRules", false));
            configPanel.setCompareFlows(cfg.optBoolean("compareFlows", false));
            configPanel.setCompareApexTriggers(cfg.optBoolean("compareApexTriggers", false));
            configPanel.setCompareRecordTypes(cfg.optBoolean("compareRecordTypes", false));
            configPanel.setCompareCustomMetadata(cfg.optBoolean("compareCustomMetadata", false));
            configPanel.setCompareCustomSettings(cfg.optBoolean("compareCustomSettings", false));
            configPanel.setComparePermissionSets(cfg.optBoolean("comparePermissionSets", false));
            configPanel.setCompareProfiles(cfg.optBoolean("compareProfiles", false));
            configPanel.setCompareGroups(cfg.optBoolean("compareGroups", false));
            configPanel.setCompareApprovalProcesses(cfg.optBoolean("compareApprovalProcesses", false));
            configPanel.setCompareNamedCredentials(cfg.optBoolean("compareNamedCredentials", false));
            configPanel.setPermissionSetsFilter(cfg.optString("permissionSetsFilter", ""));
            configPanel.setProfilesFilter(cfg.optString("profilesFilter", ""));

            JOptionPane.showMessageDialog(this,
                LanguageManager.get("msg.loaded"),
                LanguageManager.get("msg.loadedTitle"), JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                LanguageManager.get("msg.loadErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }

    // ---- Toolbar helpers ------------------------------------------------

    /** Atualiza todos os textos da janela para o idioma atual. */
    private void applyLanguage() {
        setTitle(LanguageManager.get("app.title"));
        headerLabel.setText(LanguageManager.get("header.title"));
        tabbedPane.setTitleAt(0, LanguageManager.get("tab.config"));
        tabbedPane.setTitleAt(1, LanguageManager.get("tab.results"));
        compareButton.setText(LanguageManager.get("btn.compare"));
        saveSettingsButton.setToolTipText(LanguageManager.get("toolbar.save"));
        loadSettingsButton.setToolTipText(LanguageManager.get("toolbar.load"));
        languageButton.setToolTipText(LanguageManager.get("toolbar.lang"));
        helpButton.setToolTipText(LanguageManager.get("toolbar.help"));
    }

    private JButton createToolbarButton(String tooltip, Icon icon) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(34, 34));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) { btn.setContentAreaFilled(true); btn.setOpaque(true); }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setContentAreaFilled(false); btn.setOpaque(false);
            }
        });
        return btn;
    }

    private static Icon createSaveIcon() {
        return iconOf(20, 20, (c, g, x, y) -> {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(60, 100, 160));
            g2.fillRoundRect(x, y, 20, 20, 3, 3);
            g2.setColor(Color.WHITE);
            g2.fillRect(x+2, y+1, 12, 8);
            g2.setColor(new Color(60, 100, 160));
            g2.fillPolygon(new int[]{x+14, x+19, x+19}, new int[]{y+1, y+1, y+6}, 3);
            g2.setColor(new Color(180, 210, 245));
            g2.fillRect(x+4, y+2, 7, 6);
            g2.setColor(new Color(30, 60, 110));
            g2.fillRoundRect(x+4, y+11, 12, 8, 2, 2);
            g2.setColor(new Color(150, 190, 230));
            g2.fillRect(x+6, y+13, 8, 4);
            g2.dispose();
        });
    }

    private static Icon createLoadIcon() {
        return iconOf(20, 20, (c, g, x, y) -> {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(230, 165, 30));
            g2.fillRoundRect(x+1, y+6, 18, 13, 3, 3);
            g2.fillRoundRect(x+1, y+3, 9, 6, 3, 3);
            g2.setColor(new Color(190, 120, 15));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x+1, y+6, 18, 13, 3, 3);
            g2.setColor(new Color(255, 205, 90));
            g2.drawLine(x+4, y+11, x+16, y+11);
            g2.drawLine(x+4, y+14, x+14, y+14);
            g2.dispose();
        });
    }

    private static Icon createLanguageIcon() {
        return iconOf(20, 20, (c, g, x, y) -> {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(55, 125, 200));
            g2.fillOval(x+1, y+1, 18, 18);
            g2.setColor(new Color(180, 220, 255, 200));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(x+1, y+10, x+19, y+10);
            g2.drawLine(x+2, y+6,  x+18, y+6);
            g2.drawLine(x+2, y+14, x+18, y+14);
            g2.drawOval(x+4, y+1, 12, 18);
            g2.setColor(new Color(35, 80, 155));
            g2.drawOval(x+1, y+1, 18, 18);
            g2.dispose();
        });
    }

    private static Icon createHelpIcon() {
        return iconOf(20, 20, (c, g, x, y) -> {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(85, 145, 215));
            g2.fillOval(x+1, y+1, 18, 18);
            g2.setColor(new Color(45, 95, 175));
            g2.setStroke(new BasicStroke(1f));
            g2.drawOval(x+1, y+1, 18, 18);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            String s = "?";
            g2.drawString(s, x + (20 - fm.stringWidth(s)) / 2, y + 14);
            g2.dispose();
        });
    }

    // getIconWidth/getIconHeight fornecidos via iconOf():
    @FunctionalInterface
    private interface IconPainter {
        void paint(Component c, Graphics g, int x, int y);
    }

    private static Icon iconOf(int w, int h, IconPainter p) {
        return new Icon() {
            public int getIconWidth()  { return w; }
            public int getIconHeight() { return h; }
            public void paintIcon(Component c, Graphics g, int x, int y) { p.paint(c, g, x, y); }
        };
    }
}
