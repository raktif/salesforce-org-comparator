package com.sfcomparator.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.sfcomparator.model.Difference;

public class ResultsPanel extends JPanel {

    private static final String[] COL_KEYS = {"col.category", "col.name", "col.org1", "col.org2", "col.details"};

    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel statusLabel;
    private JButton exportButton;
    private JButton clearButton;
    @SuppressWarnings("unchecked")
    private final JComboBox<String>[] filterCombos = new JComboBox[COL_KEYS.length];
    private JButton clearFiltersButton;

    // Elementos de texto atualizados a cada troca de idioma
    private TitledBorder resultsBorder;
    private JLabel filterTitleLabel;
    private final JLabel[] filterColLabels = new JLabel[COL_KEYS.length];
    /** -2 = pronto, -1 = sem diferenças, N>=0 = N diferenças encontradas */
    private int lastDiffCount = -2;
    /** Lista original das diferenças (com strings em inglês); null quando tabela vazia. */
    private List<Difference> lastDifferences;

    public ResultsPanel() {
        setupUI();
        LanguageManager.addChangeListener(this::applyLanguage);
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        resultsBorder = BorderFactory.createTitledBorder(LanguageManager.get("results.border"));
        setBorder(resultsBorder);
        setBackground(new Color(245, 245, 250));

        // Status label
        statusLabel = new JLabel(LanguageManager.get("status.ready"));
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Table setup
        String[] initialCols = new String[COL_KEYS.length];
        for (int i = 0; i < COL_KEYS.length; i++) initialCols[i] = LanguageManager.get(COL_KEYS[i]);
        tableModel = new DefaultTableModel(initialCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setFont(new Font("Arial", Font.PLAIN, 10));
        resultsTable.setRowHeight(25);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Column widths
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(260);

        // Sorting: clicar no cabeçalho ordena asc → desc → sem ordenação
        sorter = new TableRowSorter<>(tableModel);
        resultsTable.setRowSorter(sorter);

        // Renderer para coluna Details: exibe tooltip com texto completo
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (c instanceof JLabel && value != null) {
                    String full = value.toString();
                    ((JLabel) c).setToolTipText(
                        "<html><body style='width:450px'>" + full.replace("&", "&amp;").replace("<", "&lt;")
                        .replace("\n", "<br>") + "</body></html>");
                }
                return c;
            }
        });

        // Duplo clique: se a diferença é Divergente e tem conteúdo armazenado, abre o
        // DiffViewerWindow; caso contrário exibe o texto da coluna Details.
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                int viewRow = resultsTable.getSelectedRow();
                if (viewRow < 0) return;
                int modelRow = sorter.convertRowIndexToModel(viewRow);

                // Tenta abrir o visualizador de diff quando há conteúdo disponível
                if (lastDifferences != null && modelRow < lastDifferences.size()) {
                    Difference diff = lastDifferences.get(modelRow);
                    if (diff.getContent1() != null && diff.getContent2() != null) {
                        Window owner = SwingUtilities.getWindowAncestor(ResultsPanel.this);
                        Frame frame  = (owner instanceof Frame) ? (Frame) owner : null;
                        new DiffViewerWindow(frame, diff);
                        return;
                    }
                }

                // Fallback: exibe o texto da coluna Details em um dialog simples
                Object val = tableModel.getValueAt(modelRow, 4);
                if (val == null || val.toString().isBlank()) return;
                JTextArea text = new JTextArea(val.toString());
                text.setEditable(false);
                text.setWrapStyleWord(true);
                text.setLineWrap(true);
                text.setFont(new Font("Monospaced", Font.PLAIN, 12));
                text.setCaretPosition(0);
                JScrollPane sp = new JScrollPane(text);
                sp.setPreferredSize(new Dimension(650, 320));
                Object nome = tableModel.getValueAt(modelRow, 1);
                JOptionPane.showMessageDialog(ResultsPanel.this, sp,
                    LanguageManager.get("details.title") + (nome != null ? nome : ""),
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        // ---- Painel de filtros ------------------------------------------
        int[] cbWidths = {150, 180, 110, 110, 170};
        for (int i = 0; i < COL_KEYS.length; i++) {
            filterCombos[i] = new JComboBox<>();
            filterCombos[i].addItem(LanguageManager.get("filter.all"));
            filterCombos[i].setFont(new Font("Arial", Font.PLAIN, 11));
            filterCombos[i].setPreferredSize(new Dimension(cbWidths[i], 24));
            filterCombos[i].addActionListener(e -> applyFilters());
        }

        clearFiltersButton = new JButton(LanguageManager.get("btn.clearFilters"));
        clearFiltersButton.setFont(new Font("Arial", Font.PLAIN, 11));
        clearFiltersButton.addActionListener(e -> clearFilters());

        JPanel filterTitleRow = new JPanel(new BorderLayout());
        filterTitleRow.setOpaque(false);
        filterTitleLabel = new JLabel(LanguageManager.get("filter.title"));
        filterTitleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        filterTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        filterTitleRow.add(filterTitleLabel, BorderLayout.WEST);
        filterTitleRow.add(clearFiltersButton, BorderLayout.EAST);

        JPanel combosRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        combosRow.setOpaque(false);
        for (int i = 0; i < COL_KEYS.length; i++) {
            JPanel colFilter = new JPanel(new BorderLayout(0, 2));
            colFilter.setOpaque(false);
            filterColLabels[i] = new JLabel(LanguageManager.get(COL_KEYS[i]));
            filterColLabels[i].setFont(new Font("Arial", Font.PLAIN, 10));
            colFilter.add(filterColLabels[i], BorderLayout.NORTH);
            colFilter.add(filterCombos[i], BorderLayout.CENTER);
            combosRow.add(colFilter);
        }

        JPanel filterPanel = new JPanel(new BorderLayout(0, 2));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(4, 6, 4, 6),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 215)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8))));
        filterPanel.add(filterTitleRow, BorderLayout.NORTH);
        filterPanel.add(combosRow, BorderLayout.CENTER);

        // Área norte: status + filtros
        JPanel northArea = new JPanel(new BorderLayout(0, 2));
        northArea.setOpaque(false);
        northArea.add(statusLabel, BorderLayout.NORTH);
        northArea.add(filterPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(245, 245, 250));

        exportButton = new JButton(LanguageManager.get("btn.export"));
        exportButton.setFont(new Font("Arial", Font.PLAIN, 11));
        exportButton.setEnabled(false);
        buttonPanel.add(exportButton);

        clearButton = new JButton(LanguageManager.get("btn.clear"));
        clearButton.setFont(new Font("Arial", Font.PLAIN, 11));
        buttonPanel.add(clearButton);

        add(northArea, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // ---- Lógica de filtros ----------------------------------------------

    private void applyFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        for (int i = 0; i < filterCombos.length; i++) {
            // índice 0 = opção "Todos / All" — ignora o filtro para este combo
            if (filterCombos[i].getSelectedIndex() > 0) {
                Object sel = filterCombos[i].getSelectedItem();
                if (sel != null) {
                    final int col = i;
                    final String quoted = Pattern.quote(sel.toString());
                    filters.add(RowFilter.regexFilter("(?i)^" + quoted + "$", col));
                }
            }
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void clearFilters() {
        for (JComboBox<String> cb : filterCombos) cb.setSelectedIndex(0);
        sorter.setRowFilter(null);
    }

    /** Repopula cada combo com os valores únicos da sua coluna. */
    private void refreshFilterCombos() {
        for (int col = 0; col < filterCombos.length; col++) {
            Set<String> values = new LinkedHashSet<>();
            values.add(LanguageManager.get("filter.all"));
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                Object v = tableModel.getValueAt(row, col);
                if (v != null && !v.toString().isBlank()) values.add(v.toString());
            }
            JComboBox<String> cb = filterCombos[col];
            cb.removeAllItems();
            for (String v : values) cb.addItem(v);
        }
    }
    // ---- API pública ---------------------------------------------------

    public void displayResults(List<Difference> differences) {
        this.lastDifferences = differences;
        tableModel.setRowCount(0);
        sorter.setRowFilter(null);

        if (differences == null || differences.isEmpty()) {
            lastDiffCount = -1;
            statusLabel.setText(LanguageManager.get("status.noDiff"));
            statusLabel.setForeground(new Color(0, 128, 0));
            exportButton.setEnabled(false);
            refreshFilterCombos();
            return;
        }

        lastDiffCount = differences.size();
        statusLabel.setText(LanguageManager.get("status.found", lastDiffCount));
        statusLabel.setForeground(new Color(220, 20, 60));

        for (Difference diff : differences) {
            tableModel.addRow(new Object[]{
                translateCategory(diff.getCategory()),
                diff.getName(),
                translateStatus(diff.getOrg1Value()),
                translateStatus(diff.getOrg2Value()),
                diff.getDetails() != null ? diff.getDetails() : diff.getType().toString()
            });
        }

        refreshFilterCombos();
        exportButton.setEnabled(true);
    }

    public void clearResults() {
        lastDiffCount = -2;
        lastDifferences = null;
        tableModel.setRowCount(0);
        statusLabel.setText(LanguageManager.get("status.ready"));
        statusLabel.setForeground(Color.BLACK);
        exportButton.setEnabled(false);
        clearFilters();
        refreshFilterCombos();
    }

    /** Atualiza todos os textos do painel para o idioma atual. */
    public void applyLanguage() {
        resultsBorder.setTitle(LanguageManager.get("results.border"));
        repaint();
        // Cabeçalhos das colunas (não afeta dados nem larguras)
        for (int i = 0; i < COL_KEYS.length; i++) {
            resultsTable.getColumnModel().getColumn(i).setHeaderValue(LanguageManager.get(COL_KEYS[i]));
            filterColLabels[i].setText(LanguageManager.get(COL_KEYS[i]));
        }
        resultsTable.getTableHeader().repaint();
        filterTitleLabel.setText(LanguageManager.get("filter.title"));
        clearFiltersButton.setText(LanguageManager.get("btn.clearFilters"));
        exportButton.setText(LanguageManager.get("btn.export"));
        clearButton.setText(LanguageManager.get("btn.clear"));

        if (lastDifferences != null) {
            // Re-renderiza a tabela com as novas traduções, preservando as seleções dos filtros por índice
            int[] selIndices = new int[filterCombos.length];
            for (int i = 0; i < filterCombos.length; i++)
                selIndices[i] = Math.max(0, filterCombos[i].getSelectedIndex());

            tableModel.setRowCount(0);
            for (Difference diff : lastDifferences) {
                tableModel.addRow(new Object[]{
                    translateCategory(diff.getCategory()),
                    diff.getName(),
                    translateStatus(diff.getOrg1Value()),
                    translateStatus(diff.getOrg2Value()),
                    diff.getDetails() != null ? diff.getDetails() : diff.getType().toString()
                });
            }
            refreshFilterCombos();
            for (int i = 0; i < filterCombos.length; i++) {
                if (selIndices[i] < filterCombos[i].getItemCount())
                    filterCombos[i].setSelectedIndex(selIndices[i]);
            }
            applyFilters();
            statusLabel.setText(LanguageManager.get("status.found", lastDiffCount));
            statusLabel.setForeground(new Color(220, 20, 60));
        } else {
            // Sem dados: apenas atualiza a opção "Todos" nos combos e a label de status
            for (JComboBox<String> cb : filterCombos) {
                if (cb.getItemCount() > 0) {
                    int selIdx = cb.getSelectedIndex();
                    cb.removeItemAt(0);
                    cb.insertItemAt(LanguageManager.get("filter.all"), 0);
                    cb.setSelectedIndex(selIdx < 0 ? 0 : selIdx);
                }
            }
            if (lastDiffCount == -2) {
                statusLabel.setText(LanguageManager.get("status.ready"));
                statusLabel.setForeground(Color.BLACK);
            } else if (lastDiffCount == -1) {
                statusLabel.setText(LanguageManager.get("status.noDiff"));
                statusLabel.setForeground(new Color(0, 128, 0));
            }
        }
    }

    public JButton getExportButton()         { return exportButton; }
    public JButton getClearButton()          { return clearButton; }
    public JTable  getResultsTable()         { return resultsTable; }
    public DefaultTableModel getTableModel() { return tableModel; }

    // ---- Helpers de tradução -------------------------------------------

    private static String translateCategory(String category) {
        if (category == null) return "";
        int parenIdx = category.indexOf(" (");
        if (parenIdx > 0) {
            String base   = category.substring(0, parenIdx);
            String suffix = category.substring(parenIdx);
            return translateBaseCategory(base) + suffix;
        }
        return translateBaseCategory(category);
    }

    private static String translateBaseCategory(String base) {
        switch (base) {
            case "Object Fields":         return LanguageManager.get("cat.objectFields");
            case "Page Layouts":          return LanguageManager.get("cat.pageLayouts");
            case "Validation Rules":      return LanguageManager.get("cat.validationRules");
            case "Flows":                 return LanguageManager.get("cat.flows");
            case "Apex Triggers":         return LanguageManager.get("cat.apexTriggers");
            case "Record Types":          return LanguageManager.get("cat.recordTypes");
            case "Custom Metadata Types": return LanguageManager.get("cat.customMetadata");
            case "Custom Settings":       return LanguageManager.get("cat.customSettings");
            case "Permission Sets":       return LanguageManager.get("cat.permissionSets");
            case "Profiles":              return LanguageManager.get("cat.profiles");
            case "Public Groups":         return LanguageManager.get("cat.publicGroups");
            case "Approval Processes":    return LanguageManager.get("cat.approvalProcesses");
            case "Named Credentials":     return LanguageManager.get("cat.namedCredentials");
            default:                      return base;
        }
    }

    private static String translateStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "Present":   return LanguageManager.get("val.present");
            case "Absent":    return LanguageManager.get("val.absent");
            case "Divergent": return LanguageManager.get("val.divergent");
            default:          return status;  // e.g. "string", "boolean", "v1", "picklist"
        }
    }
}
