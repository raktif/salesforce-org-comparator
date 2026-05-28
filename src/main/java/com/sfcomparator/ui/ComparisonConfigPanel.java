package com.sfcomparator.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ComparisonConfigPanel extends JPanel {
    private JTextField sobjectField;
    private JCheckBox objectFieldsCheckBox;
    private JCheckBox pageLayoutsCheckBox;
    private JCheckBox validationRulesCheckBox;
    private JCheckBox flowsCheckBox;
    private JCheckBox apexTriggersCheckBox;
    private JCheckBox recordTypesCheckBox;
    private JCheckBox customMetadataCheckBox;
    private JCheckBox customSettingsCheckBox;
    private JCheckBox permissionSetsCheckBox;
    private JTextField permSetsFilterField;
    private JCheckBox profilesCheckBox;
    private JTextField profilesFilterField;
    private JCheckBox groupsCheckBox;
    private JCheckBox approvalProcessesCheckBox;
    private JCheckBox namedCredentialsCheckBox;

    // Elementos de texto atualizados a cada troca de idioma
    private TitledBorder configBorder;
    private JLabel sobjectLabel;
    private JLabel objBoundLabel;
    private JLabel orgLevelLabel;

    public ComparisonConfigPanel() {
        setupUI();
        LanguageManager.addChangeListener(this::applyLanguage);
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        configBorder = BorderFactory.createTitledBorder(LanguageManager.get("config.title"));
        setBorder(configBorder);
        setBackground(new Color(245, 245, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        int row = 0;

        // SObject Name
        gbc.gridx = 0; gbc.gridy = row;
        sobjectLabel = new JLabel(LanguageManager.get("lbl.sobject"));
        sobjectLabel.setFont(new Font("Arial", Font.BOLD, 11));
        add(sobjectLabel, gbc);

        gbc.gridx = 1;
        sobjectField = new JTextField(20);
        sobjectField.setFont(new Font("Arial", Font.PLAIN, 11));
        sobjectField.setToolTipText(LanguageManager.get("tip.sobject"));
        add(sobjectField, gbc);

        // --- Grupo 1: Metadados vinculados ao Objeto ---
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 10, 2, 10);
        JSeparator sep1 = new JSeparator();
        add(sep1, gbc);

        row++;
        gbc.gridy = row;
        gbc.insets = new Insets(2, 10, 4, 10);
        objBoundLabel = new JLabel(LanguageManager.get("config.section.object"));
        objBoundLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        objBoundLabel.setForeground(new Color(80, 80, 160));
        add(objBoundLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(3, 10, 3, 10);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        apexTriggersCheckBox = createCheckBox(LanguageManager.get("chk.apexTriggers"));
        add(apexTriggersCheckBox, gbc);

        gbc.gridx = 1;
        objectFieldsCheckBox = createCheckBox(LanguageManager.get("chk.objectFields"));
        add(objectFieldsCheckBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        flowsCheckBox = createCheckBox(LanguageManager.get("chk.flows"));
        add(flowsCheckBox, gbc);

        gbc.gridx = 1;
        pageLayoutsCheckBox = createCheckBox(LanguageManager.get("chk.pageLayouts"));
        add(pageLayoutsCheckBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        recordTypesCheckBox = createCheckBox(LanguageManager.get("chk.recordTypes"));
        add(recordTypesCheckBox, gbc);

        gbc.gridx = 1;
        validationRulesCheckBox = createCheckBox(LanguageManager.get("chk.validationRules"));
        add(validationRulesCheckBox, gbc);

        // --- Grupo 2: Metadados da Org ---
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 10, 2, 10);
        add(new JSeparator(), gbc);

        row++;
        gbc.gridy = row;
        gbc.insets = new Insets(2, 10, 4, 10);
        orgLevelLabel = new JLabel(LanguageManager.get("config.section.org"));
        orgLevelLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        orgLevelLabel.setForeground(new Color(80, 80, 160));
        add(orgLevelLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(3, 10, 3, 10);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        approvalProcessesCheckBox = createCheckBox(LanguageManager.get("chk.approvalProcesses"));
        add(approvalProcessesCheckBox, gbc);

        gbc.gridx = 1;
        customMetadataCheckBox = createCheckBox(LanguageManager.get("chk.customMetadata"));
        add(customMetadataCheckBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        customSettingsCheckBox = createCheckBox(LanguageManager.get("chk.customSettings"));
        add(customSettingsCheckBox, gbc);

        gbc.gridx = 1;
        namedCredentialsCheckBox = createCheckBox(LanguageManager.get("chk.namedCredentials"));
        add(namedCredentialsCheckBox, gbc);

        // Permission Sets com campo de filtro inline
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel permSetsRow = new JPanel(new BorderLayout(8, 0));
        permSetsRow.setBackground(new Color(245, 245, 250));
        permissionSetsCheckBox = createCheckBox(LanguageManager.get("chk.permissionSets"));
        permSetsFilterField = new JTextField();
        permSetsFilterField.setFont(new Font("Arial", Font.PLAIN, 11));
        permSetsFilterField.setToolTipText(LanguageManager.get("tip.permSetsFilter"));
        permSetsFilterField.setVisible(false);
        permSetsRow.add(permissionSetsCheckBox, BorderLayout.WEST);
        permSetsRow.add(permSetsFilterField, BorderLayout.CENTER);
        add(permSetsRow, gbc);
        permissionSetsCheckBox.addActionListener(e -> {
            permSetsFilterField.setVisible(permissionSetsCheckBox.isSelected());
            revalidate(); repaint();
        });

        // Profiles com campo de filtro inline
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel profilesRow = new JPanel(new BorderLayout(8, 0));
        profilesRow.setBackground(new Color(245, 245, 250));
        profilesCheckBox = createCheckBox(LanguageManager.get("chk.profiles"));
        profilesFilterField = new JTextField();
        profilesFilterField.setFont(new Font("Arial", Font.PLAIN, 11));
        profilesFilterField.setToolTipText(LanguageManager.get("tip.profilesFilter"));
        profilesFilterField.setVisible(false);
        profilesRow.add(profilesCheckBox, BorderLayout.WEST);
        profilesRow.add(profilesFilterField, BorderLayout.CENTER);
        add(profilesRow, gbc);
        profilesCheckBox.addActionListener(e -> {
            profilesFilterField.setVisible(profilesCheckBox.isSelected());
            revalidate(); repaint();
        });

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        groupsCheckBox = createCheckBox(LanguageManager.get("chk.publicGroups"));
        add(groupsCheckBox, gbc);

        // Filler: absorve espaço vertical restante, mantendo conteúdo alinhado ao topo
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JLabel(), gbc);
    }

    /** Atualiza todos os textos do painel para o idioma atual. */
    public void applyLanguage() {
        configBorder.setTitle(LanguageManager.get("config.title"));
        repaint();
        sobjectLabel.setText(LanguageManager.get("lbl.sobject"));
        sobjectField.setToolTipText(LanguageManager.get("tip.sobject"));
        objBoundLabel.setText(LanguageManager.get("config.section.object"));
        orgLevelLabel.setText(LanguageManager.get("config.section.org"));
        apexTriggersCheckBox.setText(LanguageManager.get("chk.apexTriggers"));
        objectFieldsCheckBox.setText(LanguageManager.get("chk.objectFields"));
        flowsCheckBox.setText(LanguageManager.get("chk.flows"));
        pageLayoutsCheckBox.setText(LanguageManager.get("chk.pageLayouts"));
        recordTypesCheckBox.setText(LanguageManager.get("chk.recordTypes"));
        validationRulesCheckBox.setText(LanguageManager.get("chk.validationRules"));
        approvalProcessesCheckBox.setText(LanguageManager.get("chk.approvalProcesses"));
        customMetadataCheckBox.setText(LanguageManager.get("chk.customMetadata"));
        customSettingsCheckBox.setText(LanguageManager.get("chk.customSettings"));
        namedCredentialsCheckBox.setText(LanguageManager.get("chk.namedCredentials"));
        permissionSetsCheckBox.setText(LanguageManager.get("chk.permissionSets"));
        permSetsFilterField.setToolTipText(LanguageManager.get("tip.permSetsFilter"));
        profilesCheckBox.setText(LanguageManager.get("chk.profiles"));
        profilesFilterField.setToolTipText(LanguageManager.get("tip.profilesFilter"));
        groupsCheckBox.setText(LanguageManager.get("chk.publicGroups"));
    }

    private JCheckBox createCheckBox(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(new Font("Arial", Font.PLAIN, 11));
        cb.setBackground(new Color(245, 245, 250));
        return cb;
    }

    // Getters
    public String getSobjectName()             { return sobjectField.getText().trim(); }
    public boolean isCompareObjectFields()     { return objectFieldsCheckBox.isSelected(); }
    public boolean isComparePageLayouts()      { return pageLayoutsCheckBox.isSelected(); }
    public boolean isCompareValidationRules()  { return validationRulesCheckBox.isSelected(); }
    public boolean isCompareFlows()            { return flowsCheckBox.isSelected(); }
    public boolean isCompareApexTriggers()     { return apexTriggersCheckBox.isSelected(); }
    public boolean isCompareRecordTypes()       { return recordTypesCheckBox.isSelected(); }
    public boolean isCompareCustomMetadata()   { return customMetadataCheckBox.isSelected(); }
    public boolean isCompareCustomSettings()   { return customSettingsCheckBox.isSelected(); }
    public boolean isComparePermissionSets()   { return permissionSetsCheckBox.isSelected(); }
    public boolean isCompareProfiles()         { return profilesCheckBox.isSelected(); }
    public boolean isCompareGroups()           { return groupsCheckBox.isSelected(); }
    public boolean isCompareApprovalProcesses(){ return approvalProcessesCheckBox.isSelected(); }
    public boolean isCompareNamedCredentials() { return namedCredentialsCheckBox.isSelected(); }

    // Setters
    public void setSobjectName(String v)             { sobjectField.setText(v); }
    public void setCompareObjectFields(boolean v)    { objectFieldsCheckBox.setSelected(v); }
    public void setComparePageLayouts(boolean v)     { pageLayoutsCheckBox.setSelected(v); }
    public void setCompareValidationRules(boolean v) { validationRulesCheckBox.setSelected(v); }
    public void setCompareFlows(boolean v)           { flowsCheckBox.setSelected(v); }
    public void setCompareApexTriggers(boolean v)    { apexTriggersCheckBox.setSelected(v); }
    public void setCompareRecordTypes(boolean v)      { recordTypesCheckBox.setSelected(v); }
    public void setCompareCustomMetadata(boolean v)  { customMetadataCheckBox.setSelected(v); }
    public void setCompareCustomSettings(boolean v)  { customSettingsCheckBox.setSelected(v); }
    public void setComparePermissionSets(boolean v)  { permissionSetsCheckBox.setSelected(v); permSetsFilterField.setVisible(v); revalidate(); repaint(); }
    public void setCompareProfiles(boolean v)        { profilesCheckBox.setSelected(v); profilesFilterField.setVisible(v); revalidate(); repaint(); }
    public void setCompareGroups(boolean v)          { groupsCheckBox.setSelected(v); }
    public void setCompareApprovalProcesses(boolean v)  { approvalProcessesCheckBox.setSelected(v); }
    public void setCompareNamedCredentials(boolean v)   { namedCredentialsCheckBox.setSelected(v); }

    public String getPermissionSetsFilter()       { return permSetsFilterField.getText().trim(); }
    public void setPermissionSetsFilter(String v) { permSetsFilterField.setText(v != null ? v : ""); }

    public String getProfilesFilter()             { return profilesFilterField.getText().trim(); }
    public void setProfilesFilter(String v)       { profilesFilterField.setText(v != null ? v : ""); }
}
