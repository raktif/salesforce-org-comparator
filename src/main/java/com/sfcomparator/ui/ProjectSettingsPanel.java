package com.sfcomparator.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Painel de configurações do projeto local (repositório).
 * Exibido na aba "Configuração", abaixo dos formulários de conexão das orgs.
 */
public class ProjectSettingsPanel extends JPanel {

    private TitledBorder titledBorder;
    private JLabel localPathLabel;
    private JTextField localPathField;
    private JLabel pathErrorLabel;
    private final List<Runnable> pathChangeListeners = new ArrayList<>();

    public ProjectSettingsPanel() {
        setupUI();
        LanguageManager.addChangeListener(this::applyLanguage);
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        titledBorder = BorderFactory.createTitledBorder(LanguageManager.get("project.title"));
        setBorder(titledBorder);
        setBackground(new Color(245, 245, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        localPathLabel = new JLabel(LanguageManager.get("lbl.localPath"));
        localPathLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        syncLabelColumnWidth();
        add(localPathLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        localPathField = new JTextField(30);
        localPathField.setFont(new Font("Arial", Font.PLAIN, 11));
        localPathField.setToolTipText(LanguageManager.get("tip.localPath"));
        add(localPathField, gbc);

        localPathField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                pathChangeListeners.forEach(Runnable::run);
            }
        });
        localPathField.addActionListener(e -> pathChangeListeners.forEach(Runnable::run));

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 10, 4, 10);
        pathErrorLabel = new JLabel();
        pathErrorLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        pathErrorLabel.setForeground(Color.RED);
        pathErrorLabel.setVisible(false);
        add(pathErrorLabel, gbc);
    }

    /**
     * Sincroniza a largura da coluna de labels com a do label mais largo da
     * {@link OrgConnectionPanel}, garantindo que todos os campos da aba
     * "Configuração" iniciem na mesma posição horizontal.
     */
    private void syncLabelColumnWidth() {
        JLabel refLabel = new JLabel(LanguageManager.get("lbl.clientSecret"));
        refLabel.setFont(localPathLabel.getFont());
        int refWidth = refLabel.getPreferredSize().width;
        Dimension natural = localPathLabel.getPreferredSize();
        localPathLabel.setPreferredSize(new Dimension(
            Math.max(refWidth, natural.width), natural.height));
    }

    /** Atualiza todos os textos do painel para o idioma atual. */
    public void applyLanguage() {
        titledBorder.setTitle(LanguageManager.get("project.title"));
        repaint();
        localPathLabel.setText(LanguageManager.get("lbl.localPath"));
        localPathField.setToolTipText(LanguageManager.get("tip.localPath"));
        localPathLabel.setPreferredSize(null); // reset para medir o texto novo antes de re-sincronizar
        syncLabelColumnWidth();
        revalidate();
        if (pathErrorLabel.isVisible()) {
            pathErrorLabel.setText(LanguageManager.get("lbl.path.notFound"));
        }
    }

    /** Registra um ouvinte chamado sempre que o usuario sai do campo de caminho. */
    public void addPathChangeListener(Runnable listener) {
        pathChangeListeners.add(listener);
    }

    /** Exibe a mensagem de erro abaixo do campo de caminho. */
    public void showPathError() {
        pathErrorLabel.setText(LanguageManager.get("lbl.path.notFound"));
        pathErrorLabel.setVisible(true);
    }

    /** Oculta a mensagem de erro abaixo do campo de caminho. */
    public void clearPathError() {
        pathErrorLabel.setVisible(false);
    }

    public String getLocalPath() { return localPathField.getText().trim(); }
    public void setLocalPath(String path) { localPathField.setText(path != null ? path : ""); }
}
