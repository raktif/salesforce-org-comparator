package com.sfcomparator.ui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.sfcomparator.model.MetadataRegistry;

/**
 * Painel da aba "Comparar Repositorio".
 * Exibe todos os tipos de metadados mapeados no projeto local em 3 colunas,
 * ordenados alfabeticamente, para que o usuario selecione quais comparar com a org.
 */
public class CompareRepoPanel extends JPanel {

    private JLabel titleLabel;
    private final Map<String, JCheckBox> checkboxes = new LinkedHashMap<>();

    public CompareRepoPanel() {
        setupUI();
        LanguageManager.addChangeListener(this::applyLanguage);
    }

    private void setupUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 245, 250));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Rotulo de instrucao — estilo identico aos rotulos de secao do ComparisonConfigPanel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 6));
        headerPanel.setBackground(new Color(245, 245, 250));
        titleLabel = new JLabel(LanguageManager.get("lbl.repo.selectMetadata"));
        titleLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        titleLabel.setForeground(new Color(80, 80, 160));
        headerPanel.add(titleLabel);

        // Grade de checkboxes — 3 colunas, altura dinamica
        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 8, 4));
        gridPanel.setBackground(new Color(245, 245, 250));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(2, 8, 8, 8));

        for (MetadataRegistry.MetadataEntry entry : MetadataRegistry.ENTRIES) {
            JCheckBox cb = new JCheckBox(entry.apiName());
            cb.setFont(new Font("Arial", Font.PLAIN, 11));
            cb.setBackground(new Color(245, 245, 250));
            cb.setToolTipText(LanguageManager.get("tip.repo." + entry.apiName()));
            checkboxes.put(entry.apiName(), cb);
            gridPanel.add(cb);
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /** Atualiza todos os textos do painel para o idioma atual. */
    public void applyLanguage() {
        titleLabel.setText(LanguageManager.get("lbl.repo.selectMetadata"));
        checkboxes.forEach((apiName, cb) ->
            cb.setToolTipText(LanguageManager.get("tip.repo." + apiName)));
    }

    /** Retorna os API names de todos os tipos de metadados selecionados pelo usuario. */
    public List<String> getSelectedMetadata() {
        return checkboxes.entrySet().stream()
            .filter(e -> e.getValue().isSelected())
            .map(Map.Entry::getKey)
            .toList();
    }

    /**
     * Habilita ou desabilita o checkbox do tipo de metadado especificado.
     * Se desabilitado, o checkbox e desmarcado automaticamente.
     */
    public void setCheckboxEnabled(String apiName, boolean enabled) {
        JCheckBox cb = checkboxes.get(apiName);
        if (cb != null) {
            cb.setEnabled(enabled);
            if (!enabled) cb.setSelected(false);
        }
    }

    /**
     * Reabilita todos os checkboxes e desmarca todos, retornando ao estado inicial.
     * Chamado antes de cada varredura de pastas ou quando o caminho e limpo.
     */
    public void resetCheckboxStates() {
        checkboxes.values().forEach(cb -> {
            cb.setEnabled(true);
            cb.setSelected(false);
        });
    }
}
