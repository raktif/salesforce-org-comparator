package com.sfcomparator.ui;

import com.sfcomparator.model.Difference;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Janela de visualização lado a lado (diff) do conteúdo de dois arquivos de metadado.
 * Abre ao dar duplo clique em uma linha com status "Divergente" na tabela de resultados.
 *
 * <p>Para cada par de arquivos, computa o diff linha a linha usando o algoritmo LCS
 * (Longest Common Subsequence). Operações DEL+INS consecutivas são mescladas como CHANGED.
 * Para arquivos com mais de {@value #MAX_LCS_LINES} linhas por lado, exibe o conteúdo
 * em alinhamento simples (sem LCS) para preservar o desempenho da EDT.</p>
 */
public class DiffViewerWindow extends JDialog {

    // ── Cores das linhas ────────────────────────────────────────────────────
    private static final Color COLOR_EQUAL_CONTENT             = new Color(255, 255, 255);
    private static final Color COLOR_CHANGED_CONTENT           = new Color(255, 255, 204);
    // Linhas presentes SOMENTE no lado esquerdo (local/org1) → verde
    private static final Color COLOR_LEFT_ONLY_CONTENT         = new Color(220, 255, 220);
    private static final Color COLOR_LEFT_ONLY_EMPTY           = new Color(235, 248, 235);
    // Linhas presentes SOMENTE no lado direito (org/org2) → vermelho
    private static final Color COLOR_RIGHT_ONLY_CONTENT        = new Color(255, 220, 220);
    private static final Color COLOR_RIGHT_ONLY_EMPTY          = new Color(248, 235, 235);

    private static final Color COLOR_EQUAL_GUTTER              = new Color(235, 235, 235);
    private static final Color COLOR_CHANGED_GUTTER            = new Color(240, 240, 174);
    private static final Color COLOR_LEFT_ONLY_GUTTER          = new Color(190, 240, 190);
    private static final Color COLOR_LEFT_ONLY_EMPTY_GUTTER    = new Color(218, 236, 218);
    private static final Color COLOR_RIGHT_ONLY_GUTTER         = new Color(240, 190, 190);
    private static final Color COLOR_RIGHT_ONLY_EMPTY_GUTTER   = new Color(236, 218, 218);

    // ── Limite para o algoritmo LCS (linhas por lado) ───────────────────────
    private static final int MAX_LCS_LINES = 2000;

    // ── Tipos de operação de diff ────────────────────────────────────────────
    private enum DiffType { EQUAL, ADDED, REMOVED, CHANGED }

    // ── Linha do diff (par esquerda / direita) ───────────────────────────────
    private static final class DiffRow {
        final DiffType type;
        final int      leftNum;   // 0 = sem número de linha (ADDED não tem lado esquerdo)
        final String   leftText;
        final int      rightNum;  // 0 = sem número de linha (REMOVED não tem lado direito)
        final String   rightText;

        DiffRow(DiffType type, int leftNum, String leftText, int rightNum, String rightText) {
            this.type      = type;
            this.leftNum   = leftNum;
            this.leftText  = leftText  != null ? leftText  : "";
            this.rightNum  = rightNum;
            this.rightText = rightText != null ? rightText : "";
        }
    }

    // ── Construtor ───────────────────────────────────────────────────────────

    public DiffViewerWindow(Frame owner, Difference diff) {
        super(owner, LanguageManager.get("diff.title") + ": " + diff.getName(), false);

        String c1 = diff.getContent1() != null ? diff.getContent1() : "";
        String c2 = diff.getContent2() != null ? diff.getContent2() : "";

        String leftLabel  = resolveLeftLabel(diff);
        String rightLabel = resolveRightLabel(diff);

        String[] lines1 = trimTrailingEmpty(c1.replace("\r\n", "\n").replace("\r", "\n").split("\n", -1));
        String[] lines2 = trimTrailingEmpty(c2.replace("\r\n", "\n").replace("\r", "\n").split("\n", -1));

        List<DiffRow> rows = computeDiff(lines1, lines2);

        buildUI(leftLabel, rightLabel, rows);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    // ── Construção da interface ──────────────────────────────────────────────

    private void buildUI(String leftLabel, String rightLabel, List<DiffRow> rows) {
        setLayout(new BorderLayout());

        // Cabeçalho com rótulos dos dois lados
        JLabel lblLeft = makeHeaderLabel(" " + leftLabel, new Color(40, 90, 150));
        JLabel lblRight = makeHeaderLabel(" " + rightLabel, new Color(40, 130, 60));
        JPanel header = new JPanel(new GridLayout(1, 2, 1, 0));
        header.setBackground(new Color(30, 30, 30));
        header.add(lblLeft);
        header.add(lblRight);

        // Modelo da tabela: lineNum | content | lineNum | content
        String colContent = LanguageManager.get("diff.col.content");
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"", colContent, "", colContent}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (DiffRow row : rows) {
            model.addRow(new Object[]{
                row.leftNum  > 0 ? row.leftNum  : "",
                row.leftText,
                row.rightNum > 0 ? row.rightNum : "",
                row.rightText
            });
        }

        // Tabela
        JTable table = new JTable(model);
        table.setFont(new Font("Monospaced", Font.PLAIN, 11));
        table.setRowHeight(18);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(210, 210, 210));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Larguras das colunas
        table.getColumnModel().getColumn(0).setPreferredWidth(48);
        table.getColumnModel().getColumn(0).setMaxWidth(64);
        table.getColumnModel().getColumn(1).setPreferredWidth(576);
        table.getColumnModel().getColumn(2).setPreferredWidth(48);
        table.getColumnModel().getColumn(2).setMaxWidth(64);
        table.getColumnModel().getColumn(3).setPreferredWidth(576);

        // Renderer colorido
        DiffCellRenderer renderer = new DiffCellRenderer(rows);
        for (int c = 0; c < 4; c++) {
            table.getColumnModel().getColumn(c).setCellRenderer(renderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Rodapé: legenda + botão fechar
        JPanel south = new JPanel(new BorderLayout(8, 0));
        south.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        south.add(buildLegend(), BorderLayout.WEST);

        JButton closeBtn = new JButton(LanguageManager.get("btn.close"));
        closeBtn.addActionListener(e -> dispose());
        south.add(closeBtn, BorderLayout.EAST);

        add(header,     BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(south,      BorderLayout.SOUTH);
    }

    private static JLabel makeHeaderLabel(String text, Color bg) {
        JLabel lbl = new JLabel(text, SwingConstants.LEFT);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return lbl;
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        addLegendItem(p, COLOR_EQUAL_CONTENT,      LanguageManager.get("diff.legend.equal"));
        addLegendItem(p, COLOR_CHANGED_CONTENT,    LanguageManager.get("diff.legend.changed"));
        addLegendItem(p, COLOR_LEFT_ONLY_CONTENT,  LanguageManager.get("diff.legend.leftonly"));
        addLegendItem(p, COLOR_RIGHT_ONLY_CONTENT, LanguageManager.get("diff.legend.rightonly"));
        return p;
    }

    private static void addLegendItem(JPanel parent, Color color, String label) {
        JLabel swatch = new JLabel("  ");
        swatch.setOpaque(true);
        swatch.setBackground(color);
        swatch.setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160)));
        JLabel text = new JLabel(label);
        text.setFont(new Font("Arial", Font.PLAIN, 10));
        parent.add(swatch);
        parent.add(text);
    }

    // ── Resolução dos rótulos ────────────────────────────────────────────────

    private static String resolveLeftLabel(Difference diff) {
        // org-to-org: ambas as colunas "Divergent" → esquerda = Org 1
        if ("Divergent".equals(diff.getOrg1Value()) && "Divergent".equals(diff.getOrg2Value())) {
            return LanguageManager.get("diff.label.org1");
        }
        // repo vs org: esquerda é sempre o repositório local
        return LanguageManager.get("diff.label.local");
    }

    private static String resolveRightLabel(Difference diff) {
        if ("Divergent".equals(diff.getOrg1Value()) && "Divergent".equals(diff.getOrg2Value())) {
            return LanguageManager.get("diff.label.org2");
        }
        // org1Value = "Divergent" → comparando com Org 1; caso contrário Org 2
        if ("Divergent".equals(diff.getOrg1Value())) return LanguageManager.get("diff.label.org1");
        return LanguageManager.get("diff.label.org2");
    }

    // ── Algoritmo de Diff (LCS) ──────────────────────────────────────────────

    private static String[] trimTrailingEmpty(String[] lines) {
        if (lines.length > 0 && lines[lines.length - 1].isEmpty()) {
            return Arrays.copyOf(lines, lines.length - 1);
        }
        return lines;
    }

    /**
     * Computa o diff entre {@code left} e {@code right} usando LCS quando ambos têm
     * ≤ {@value #MAX_LCS_LINES} linhas. Caso contrário usa alinhamento simples.
     */
    private static List<DiffRow> computeDiff(String[] left, String[] right) {
        int m = left.length, n = right.length;
        if (m == 0 && n == 0) return Collections.emptyList();

        if (m > MAX_LCS_LINES || n > MAX_LCS_LINES) {
            return simpleSideBySide(left, right);
        }

        // Tabela DP para LCS
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                dp[i][j] = left[i - 1].equals(right[j - 1])
                        ? dp[i - 1][j - 1] + 1
                        : Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }

        // Backtracking iterativo: 0=EQUAL, 1=DEL (esquerda), 2=INS (direita)
        List<int[]> ops = new ArrayList<>();
        int i = m, j = n;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && left[i - 1].equals(right[j - 1])) {
                ops.add(new int[]{0, i - 1, j - 1});
                i--; j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                ops.add(new int[]{2, -1, j - 1}); // INS
                j--;
            } else {
                ops.add(new int[]{1, i - 1, -1}); // DEL
                i--;
            }
        }
        Collections.reverse(ops);

        // Monta DiffRows mesclando sequências consecutivas DEL+INS como CHANGED
        List<DiffRow> result = new ArrayList<>(ops.size());
        int leftNum = 0, rightNum = 0;
        int k = 0;
        while (k < ops.size()) {
            if (ops.get(k)[0] == 0) {
                // EQUAL
                int[] op = ops.get(k++);
                leftNum++; rightNum++;
                result.add(new DiffRow(DiffType.EQUAL, leftNum, left[op[1]], rightNum, right[op[2]]));
            } else {
                // Coleta sequência consecutiva de DELs e INSs
                List<int[]> dels = new ArrayList<>();
                List<int[]> inss = new ArrayList<>();
                while (k < ops.size() && ops.get(k)[0] != 0) {
                    if (ops.get(k)[0] == 1) dels.add(ops.get(k));
                    else                     inss.add(ops.get(k));
                    k++;
                }
                // Pares DEL+INS → CHANGED
                int paired = Math.min(dels.size(), inss.size());
                for (int p = 0; p < paired; p++) {
                    leftNum++; rightNum++;
                    result.add(new DiffRow(DiffType.CHANGED,
                            leftNum,  left[dels.get(p)[1]],
                            rightNum, right[inss.get(p)[2]]));
                }
                // DELs restantes → REMOVED (linha ausente no lado direito)
                for (int p = paired; p < dels.size(); p++) {
                    leftNum++;
                    result.add(new DiffRow(DiffType.REMOVED, leftNum, left[dels.get(p)[1]], 0, null));
                }
                // INSs restantes → ADDED (linha ausente no lado esquerdo)
                for (int p = paired; p < inss.size(); p++) {
                    rightNum++;
                    result.add(new DiffRow(DiffType.ADDED, 0, null, rightNum, right[inss.get(p)[2]]));
                }
            }
        }
        return result;
    }

    /** Alinhamento simples (sem LCS): usada para arquivos muito grandes. */
    private static List<DiffRow> simpleSideBySide(String[] left, String[] right) {
        int max = Math.max(left.length, right.length);
        List<DiffRow> result = new ArrayList<>(max);
        int leftNum = 0, rightNum = 0;
        for (int i = 0; i < max; i++) {
            String l = i < left.length  ? left[i]  : null;
            String r = i < right.length ? right[i] : null;
            DiffType type;
            if (l != null && r != null) {
                type = l.equals(r) ? DiffType.EQUAL : DiffType.CHANGED;
            } else if (l != null) {
                type = DiffType.REMOVED;
            } else {
                type = DiffType.ADDED;
            }
            if (l != null) leftNum++;
            if (r != null) rightNum++;
            result.add(new DiffRow(type,
                    l != null ? leftNum  : 0, l,
                    r != null ? rightNum : 0, r));
        }
        return result;
    }

    // ── Renderer das células ─────────────────────────────────────────────────

    private static final class DiffCellRenderer extends DefaultTableCellRenderer {

        private final List<DiffRow> rows;

        DiffCellRenderer(List<DiffRow> rows) {
            this.rows = rows;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);

            if (row >= rows.size() || isSelected) return c;

            DiffRow dr         = rows.get(row);
            boolean isLeftSide = (col == 0 || col == 1);
            boolean isGutter   = (col == 0 || col == 2);

            c.setBackground(isGutter ? gutterColor(dr.type, isLeftSide) : contentColor(dr.type, isLeftSide));

            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                if (isGutter) {
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    lbl.setForeground(new Color(100, 100, 120));
                    lbl.setFont(new Font("Monospaced", Font.PLAIN, 10));
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 4));
                } else {
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    lbl.setForeground(Color.BLACK);
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                }
            }
            return c;
        }

        private static Color contentColor(DiffType t, boolean isLeftSide) {
            switch (t) {
                case REMOVED: return isLeftSide ? COLOR_LEFT_ONLY_CONTENT  : COLOR_LEFT_ONLY_EMPTY;
                case ADDED:   return isLeftSide ? COLOR_RIGHT_ONLY_EMPTY   : COLOR_RIGHT_ONLY_CONTENT;
                case CHANGED: return COLOR_CHANGED_CONTENT;
                default:      return COLOR_EQUAL_CONTENT;
            }
        }

        private static Color gutterColor(DiffType t, boolean isLeftSide) {
            switch (t) {
                case REMOVED: return isLeftSide ? COLOR_LEFT_ONLY_GUTTER       : COLOR_LEFT_ONLY_EMPTY_GUTTER;
                case ADDED:   return isLeftSide ? COLOR_RIGHT_ONLY_EMPTY_GUTTER : COLOR_RIGHT_ONLY_GUTTER;
                case CHANGED: return COLOR_CHANGED_GUTTER;
                default:      return COLOR_EQUAL_GUTTER;
            }
        }
    }
}
