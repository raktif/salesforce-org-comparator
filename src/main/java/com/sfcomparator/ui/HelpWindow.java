package com.sfcomparator.ui;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Janela de ajuda (não-modal) com conteúdo bilíngue PT-BR / EN.
 * Atualiza automaticamente quando o idioma é alterado via LanguageManager.
 */
public class HelpWindow extends JDialog {

    private JEditorPane contentPane;
    private JButton     closeButton;
    private JButton[]   navButtons;

    // Anchors embeds no HTML para scrollToReference()
    private static final String[] ANCHORS = {
        "overview", "org-setup", "comp-config", "running", "results", "toolbar", "save-load", "errors", "repo-compare"
    };

    private static final String[][] NAV_LABELS = {
        { // PT-BR
            "1. Vis\u00e3o Geral",
            "2. Configura\u00e7\u00e3o das Orgs",
            "3. Configura\u00e7\u00e3o da Compara\u00e7\u00e3o",
            "4. Executando a Compara\u00e7\u00e3o",
            "5. Tabela de Resultados",
            "6. Barra de Ferramentas",
            "7. Salvar / Carregar Config.",
            "8. Limita\u00e7\u00f5es e Erros",
            "9. Comparar Reposit\u00f3rio"
        },
        { // EN
            "1. Overview",
            "2. Org Setup",
            "3. Comparison Configuration",
            "4. Running the Comparison",
            "5. Results Table",
            "6. Toolbar",
            "7. Save / Load Settings",
            "8. Limitations & Errors",
            "9. Compare with Repository"
        }
    };

    // ---- Constructor ---------------------------------------------------

    public HelpWindow(Frame owner) {
        super(owner, LanguageManager.get("help.title"), false);
        buildUI();
        LanguageManager.addChangeListener(this::applyLanguage);
        setSize(970, 720);
        setMinimumSize(new Dimension(720, 500));
        setLocationRelativeTo(owner);
    }

    // ---- UI Construction -----------------------------------------------

    private void buildUI() {
        JPanel navPanel = buildNavPanel();

        contentPane = new JEditorPane();
        contentPane.setContentType("text/html");
        contentPane.setText(buildHtml());
        contentPane.setEditable(false);
        contentPane.setCaretPosition(0);
        contentPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String ref = e.getDescription();
                if (ref != null && ref.startsWith("#")) {
                    contentPane.scrollToReference(ref.substring(1));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(contentPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navPanel, scrollPane);
        split.setDividerLocation(215);
        split.setDividerSize(4);
        split.setResizeWeight(0.0);

        closeButton = new JButton(LanguageManager.get("btn.close"));
        closeButton.addActionListener(e -> dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        south.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 210)));
        south.add(closeButton);

        // Fechar com Escape
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(split, BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);
    }

    private JPanel buildNavPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(32, 52, 74));
        panel.setPreferredSize(new Dimension(215, 400));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JLabel title = new JLabel(isEN() ? "Navigation" : "Navega\u00e7\u00e3o");
        title.setFont(new Font("Arial", Font.BOLD, 11));
        title.setForeground(new Color(148, 190, 230));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        panel.add(title);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(62, 92, 128));
        sep.setMaximumSize(new Dimension(205, 1));
        panel.add(sep);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        int lang = isEN() ? 1 : 0;
        navButtons = new JButton[ANCHORS.length];
        for (int i = 0; i < ANCHORS.length; i++) {
            final String anchor = ANCHORS[i];
            navButtons[i] = createNavButton(NAV_LABELS[lang][i]);
            navButtons[i].addActionListener(e -> contentPane.scrollToReference(anchor));
            panel.add(navButtons[i]);
            panel.add(Box.createRigidArea(new Dimension(0, 1)));
        }
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 11));
        btn.setForeground(new Color(198, 220, 242));
        btn.setBackground(new Color(32, 52, 74));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setMaximumSize(new Dimension(215, 32));
        btn.setPreferredSize(new Dimension(215, 32));
        btn.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 8));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(50, 76, 108));
                btn.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(32, 52, 74));
                btn.setForeground(new Color(198, 220, 242));
            }
        });
        return btn;
    }

    // ---- Language Handling ---------------------------------------------

    private boolean isEN() {
        return LanguageManager.getLanguage() == LanguageManager.Language.EN;
    }

    public void applyLanguage() {
        setTitle(LanguageManager.get("help.title"));
        closeButton.setText(LanguageManager.get("btn.close"));
        int lang = isEN() ? 1 : 0;
        for (int i = 0; i < navButtons.length; i++) {
            navButtons[i].setText(NAV_LABELS[lang][i]);
        }
        contentPane.setText(buildHtml());
        contentPane.setCaretPosition(0);
    }

    private String buildHtml() {
        return isEN() ? htmlEN() : htmlPT();
    }

    // ---- Shared CSS ----------------------------------------------------

    private static final String CSS =
        "<style type='text/css'>" +
        "body{font-family:Arial,sans-serif;font-size:11pt;margin:16px 22px;color:#1e1e1e;}" +
        "h1{color:#1a4878;font-size:16pt;font-weight:bold;margin-top:4px;margin-bottom:8px;}" +
        "h2{color:#1a4878;font-size:13pt;font-weight:bold;margin-top:22px;margin-bottom:4px;}" +
        "h3{color:#2c3e50;font-size:11pt;font-weight:bold;margin-top:14px;margin-bottom:3px;}" +
        "p{margin-top:5px;margin-bottom:5px;line-height:1.5;}" +
        "ul{margin-left:22px;margin-top:4px;margin-bottom:4px;}" +
        "ol{margin-left:22px;margin-top:4px;margin-bottom:4px;}" +
        "li{margin-top:3px;line-height:1.5;}" +
        "code{font-family:'Courier New',monospace;font-size:10pt;background-color:#f0f0f0;}" +
        "table{border-collapse:collapse;width:100%;margin-top:7px;margin-bottom:7px;}" +
        "th{background-color:#2c5f8a;color:#ffffff;padding:7px 10px;text-align:left;font-weight:bold;}" +
        "td{border:1px solid #c8c8c8;padding:5px 9px;vertical-align:top;}" +
        "hr{border:0;border-top:1px solid #d8d8d8;margin:20px 0;}" +
        "</style>";

    // ====================================================================
    //  HTML  –  Português (PT-BR)
    // ====================================================================

    private static String htmlPT() {
        return "<html><head>" + CSS + "</head><body>"

            + "<a name='overview'></a>"
            + "<h1>Manual do Usu&aacute;rio &mdash; Salesforce Org Comparator</h1>"
            + "<p>Bem-vindo ao <b>Salesforce Org Comparator</b>! Este manual explica como utilizar todas as funcionalidades do software.</p>"
            + "<hr/>"

            // ── 1. Visão Geral ───────────────────────────────────────────
            + "<h2>1. Vis&atilde;o Geral</h2>"
            + "<p>O <b>Salesforce Org Comparator</b> &eacute; uma ferramenta desktop para comparar metadados entre dois ambientes Salesforce (<b>orgs</b>). Use-o para:</p>"
            + "<ul>"
            + "<li>Identificar diferen&ccedil;as de configura&ccedil;&atilde;o entre produ&ccedil;&atilde;o e sandbox</li>"
            + "<li>Auditar e validar deployments e releases</li>"
            + "<li>Verificar paridade entre ambientes de desenvolvimento e QA</li>"
            + "<li>Detectar campos, perfis ou permiss&otilde;es presentes em uma org mas ausentes em outra</li>"
            + "</ul>"
            + "<p>O software usa o <b>Salesforce CLI</b> e a <b>API REST do Salesforce</b> para recuperar metadados. Os resultados s&atilde;o exibidos em uma tabela interativa que pode ser filtrada, ordenada e exportada para CSV.</p>"
            + "<table>"
            + "<tr><th>Tipo de Compara&ccedil;&atilde;o</th><th>Descri&ccedil;&atilde;o</th></tr>"
            + "<tr><td><b>Por objeto (SObject)</b></td><td>Campos, Layouts de P&aacute;gina, Regras de Valida&ccedil;&atilde;o, Fluxos, Apex Triggers, Tipos de Registro</td></tr>"
            + "<tr><td><b>A n&iacute;vel de org</b></td><td>Custom Metadata, Custom Settings, Permission Sets, Profiles, Grupos P&uacute;blicos, Processos de Aprova&ccedil;&atilde;o, Named Credentials</td></tr>"
            + "</table>"
            + "<hr/>"

            // ── 2. Configuração das Orgs ─────────────────────────────────
            + "<a name='org-setup'></a>"
            + "<h2>2. Configura&ccedil;&atilde;o das Orgs</h2>"
            + "<p>Para conectar ao Salesforce, cada org precisa de uma <b>Connected App</b> com OAuth 2.0 habilitado. Preencha os campos na aba <i>Configura&ccedil;&atilde;o</i>.</p>"
            + "<h3>Campos do formul&aacute;rio</h3>"
            + "<table>"
            + "<tr><th>Campo</th><th>Descri&ccedil;&atilde;o</th><th>Exemplo</th></tr>"
            + "<tr><td><b>Instance URL</b></td><td>URL base da sua org Salesforce</td><td>https://suaempresa.my.salesforce.com</td></tr>"
            + "<tr><td><b>Token de Seguran&ccedil;a</b></td><td>C&oacute;digo alfanum&eacute;rico enviado por e-mail ao criar conta ou resetar a senha</td><td>xKz8a1B9...</td></tr>"
            + "<tr><td><b>Client ID</b></td><td>Consumer Key da Connected App</td><td>3MVG9...</td></tr>"
            + "<tr><td><b>Client Secret</b></td><td>Consumer Secret da Connected App</td><td>A1B2C3...</td></tr>"
            + "</table>"
            + "<h3>Instance URL</h3>"
            + "<ul>"
            + "<li><b>Produ&ccedil;&atilde;o:</b> <code>https://login.salesforce.com</code> ou <code>https://suaempresa.my.salesforce.com</code></li>"
            + "<li><b>Sandbox:</b> <code>https://test.salesforce.com</code> ou <code>https://suaempresa--sandbox.sandbox.my.salesforce.com</code></li>"
            + "</ul>"
            + "<h3>Token de Seguran&ccedil;a</h3>"
            + "<p>O Token &eacute; exigido pelo Salesforce para autentica&ccedil;&atilde;o de APIs fora de redes confi&aacute;veis. Para obter ou redefinir:</p>"
            + "<ol>"
            + "<li>Na org Salesforce, fa&ccedil;a o login como o usu&aacute;rio selecionado em <b>Fluxo de Credenciais do Cliente</b> e clique no seu nome de usu&aacute;rio (canto superior direito)</li>"
            + "<li>Acesse <i>Settings</i> &rarr; <i>Personal Information</i> &rarr; <i>Reset My Security Token</i></li>"
            + "<li>O novo token ser&aacute; enviado para o e-mail cadastrado</li>"
            + "</ol>"
            + "<table><tr><td><b>Obs.:</b> Se o IP da sua rede est&aacute; na lista de <i>Trusted IP Ranges</i> da org, o token pode ser omitido &mdash; deixe o campo em branco.</td></tr></table>"
            + "<h3>Como criar uma Connected App</h3>"
            + "<ol>"
            + "<li>Acesse o <b>Setup</b> da org desejada</li>"
            + "<li>Em Quick Find, pesquise <i>External</i> &rarr; <i>Settings</i> &rarr; <b>New Connected App</b></li>"
            + "<li>Preencha: Connected App Name, API Name, Contact Email</li>"
            + "<li>Marque <b>Enable OAuth Settings</b></li>"
            + "<li>Em <i>Callback URL</i>, insira <code>https://login.salesforce.com/services/oauth2/success</code> ou o valor espec&iacute;fico para sua org</li>"
            + "<li>Em <i>Selected OAuth Scopes</i>, adicione: <b>Manage user data via APIs (api)</b>, <b>Perform requests at any time (refresh_token, offline_access)</b></li>"
            + "<li>Clique em <b>Save</b> e aguarde 2&ndash;10 minutos para ativa&ccedil;&atilde;o</li>"
            + "<li>Ap&oacute;s salvar, copie o <i>Consumer Key</i> (Client ID) e o <i>Consumer Secret</i></li>"
            + "</ol>"
            + "<table><tr><td><b>Dica:</b> A mesma Connected App pode ser usada em ambas as orgs desde que esteja configurada em cada uma.</td></tr></table>"
            + "<hr/>"

            // ── 3. Configuração da Comparação ────────────────────────────
            + "<a name='comp-config'></a>"
            + "<h2>3. Configura&ccedil;&atilde;o da Compara&ccedil;&atilde;o</h2>"
            + "<h3>Nome do Objeto (SObject API Name)</h3>"
            + "<p>Para compara&ccedil;&otilde;es por objeto, preencha o <b>API Name</b> do SObject. Exemplos:</p>"
            + "<ul>"
            + "<li>Objetos padr&atilde;o: <code>Opportunity</code>, <code>Account</code>, <code>Lead</code>, <code>Contact</code>, <code>Case</code></li>"
            + "<li>Objetos customizados: <code>MinhaLista__c</code>, <code>Contrato__c</code></li>"
            + "</ul>"
            + "<table><tr><td><b>Aten&ccedil;&atilde;o:</b> O API Name &eacute; <b>case-sensitive</b>. Objetos customizados terminam em <code>__c</code>. Erros de grafia resultam em &quot;Objeto n&atilde;o encontrado&quot;.</td></tr></table>"
            + "<h3>Op&ccedil;&otilde;es por Objeto</h3>"
            + "<p>Requerem o campo <i>Nome do Objeto</i> preenchido:</p>"
            + "<table>"
            + "<tr><th>Op&ccedil;&atilde;o</th><th>O que &eacute; comparado</th></tr>"
            + "<tr><td><b>Campos do Objeto</b></td><td>API Name e tipo de dado de cada campo. Detecta campos ausentes em uma das orgs ou campos com tipos diferentes.</td></tr>"
            + "<tr><td><b>Layouts de P&aacute;gina</b></td><td>Exist&ecirc;ncia e nome dos Layouts de P&aacute;gina do objeto.</td></tr>"
            + "<tr><td><b>Regras de Valida&ccedil;&atilde;o</b></td><td>Exist&ecirc;ncia das Regras de Valida&ccedil;&atilde;o do objeto.</td></tr>"
            + "<tr><td><b>Fluxos (Acionados por Registro)</b></td><td>Fluxos Acionados por Registro associados ao objeto. Compara VersionNumber.</td></tr>"
            + "<tr><td><b>Apex Triggers</b></td><td>Triggers do objeto. Compara Status, ApiVersion e tamanho do c&oacute;digo-fonte.</td></tr>"
            + "<tr><td><b>Tipos de Registro</b></td><td>Exist&ecirc;ncia dos Tipos de Registro do objeto.</td></tr>"
            + "</table>"
            + "<h3>Op&ccedil;&otilde;es a N&iacute;vel de Org</h3>"
            + "<p>N&atilde;o requerem nome de objeto &mdash; comparam toda a org:</p>"
            + "<table>"
            + "<tr><th>Op&ccedil;&atilde;o</th><th>O que &eacute; comparado</th></tr>"
            + "<tr><td><b>Tipos de Metadados Personalizados</b></td><td>Defini&ccedil;&otilde;es de Tipos de Metadados Personalizados.</td></tr>"
            + "<tr><td><b>Configura&ccedil;&otilde;es Personalizadas</b></td><td>Configura&ccedil;&otilde;es Personalizadas (Hierarquia e Lista).</td></tr>"
            + "<tr><td><b>Conjuntos de Permiss&otilde;es</b></td><td>Exist&ecirc;ncia e conte&uacute;do dos Conjuntos de Permiss&otilde;es.</td></tr>"
            + "<tr><td><b>Perfis</b></td><td>Exist&ecirc;ncia e conte&uacute;do dos Perfis.</td></tr>"
            + "<tr><td><b>Grupos P&uacute;blicos</b></td><td>Exist&ecirc;ncia dos Grupos P&uacute;blicos.</td></tr>"
            + "<tr><td><b>Processos de Aprova&ccedil;&atilde;o</b></td><td>Exist&ecirc;ncia e status dos Processos de Aprova&ccedil;&atilde;o.</td></tr>"
            + "<tr><td><b>Credenciais Nomeadas</b></td><td>Exist&ecirc;ncia das Credenciais Nomeadas.</td></tr>"
            + "</table>"
            + "<h3>Filtros de Permission Sets e Profiles</h3>"
            + "<p>Os campos de filtro aceitam um termo parcial (case-insensitive) para limitar a compara&ccedil;&atilde;o. Se vazio, <b>todos</b> os itens s&atilde;o comparados.</p>"
            + "<p><b>Exemplo:</b> Digitar <code>Sales</code> retorna apenas Permission Sets ou Profiles que contenham &quot;Sales&quot; no nome.</p>"
            + "<table><tr><td><b>Dica:</b> Use filtros em orgs com centenas de Permission Sets ou Profiles para reduzir significativamente o tempo de compara&ccedil;&atilde;o.</td></tr></table>"
            + "<hr/>"

            // ── 4. Executando a Comparação ───────────────────────────────
            + "<a name='running'></a>"
            + "<h2>4. Executando a Compara&ccedil;&atilde;o</h2>"
            + "<p>Com os campos preenchidos e ao menos uma op&ccedil;&atilde;o selecionada, clique no bot&atilde;o <b>Iniciar Compara&ccedil;&atilde;o</b> (na barra inferior).</p>"
            + "<h3>Sequ&ecirc;ncia de execu&ccedil;&atilde;o</h3>"
            + "<ol>"
            + "<li>O software valida os campos obrigat&oacute;rios (Instance URL e Client ID de ambas as orgs)</li>"
            + "<li>Abre uma janela de progresso mostrando a etapa atual</li>"
            + "<li>Autentica nas duas orgs via OAuth 2.0</li>"
            + "<li>Para cada tipo de metadado selecionado, recupera e compara os dados das duas orgs</li>"
            + "<li>Ao final, exibe os resultados na aba <i>Resultados</i> e fecha a janela de progresso</li>"
            + "</ol>"
            + "<h3>Janela de Progresso</h3>"
            + "<ul>"
            + "<li>Exibe a etapa atual (ex: <i>Verificando Perfis...</i>)</li>"
            + "<li>O bot&atilde;o <b>Cancelar</b> interrompe o processo com seguran&ccedil;a; os recursos tempor&aacute;rios s&atilde;o liberados</li>"
            + "<li>Orgs tempor&aacute;rias registradas no Salesforce CLI s&atilde;o removidas automaticamente ao final ou ao cancelar</li>"
            + "</ul>"
            + "<table><tr><td><b>Aten&ccedil;&atilde;o:</b> N&atilde;o feche a janela principal durante a compara&ccedil;&atilde;o. Aguarde a conclus&atilde;o ou clique em Cancelar.</td></tr></table>"
            + "<hr/>"

            // ── 5. Tabela de Resultados ──────────────────────────────────
            + "<a name='results'></a>"
            + "<h2>5. Tabela de Resultados</h2>"
            + "<p>A aba <i>Resultados</i> exibe todas as diferen&ccedil;as encontradas em uma tabela interativa.</p>"
            + "<h3>Colunas</h3>"
            + "<table>"
            + "<tr><th>Coluna</th><th>Conte&uacute;do</th></tr>"
            + "<tr><td><b>Categoria</b></td><td>Tipo de metadado (ex: Campos do Objeto, Processos de Aprova&ccedil;&atilde;o). Para compara&ccedil;&otilde;es por objeto, inclui o nome do objeto entre par&ecirc;nteses.</td></tr>"
            + "<tr><td><b>Nome</b></td><td>API Name do item com diferen&ccedil;a (nome do campo, layout, perfil, etc.)</td></tr>"
            + "<tr><td><b>Org 1</b></td><td>Status ou valor na Org 1: <i>Presente</i>, <i>Ausente</i>, <i>Divergente</i> ou o tipo de dado t&eacute;cnico do campo.</td></tr>"
            + "<tr><td><b>Org 2</b></td><td>Status ou valor na Org 2 (idem).</td></tr>"
            + "<tr><td><b>Detalhes</b></td><td>Informa&ccedil;&atilde;o t&eacute;cnica adicional sobre a diferen&ccedil;a. <b>Sempre exibida em ingl&ecirc;s</b>, independentemente do idioma selecionado.</td></tr>"
            + "</table>"
            + "<h3>Interpreta&ccedil;&atilde;o dos valores</h3>"
            + "<table>"
            + "<tr><th>Valor</th><th>Significado</th></tr>"
            + "<tr><td><b>Presente</b></td><td>O item existe nesta org</td></tr>"
            + "<tr><td><b>Ausente</b></td><td>O item n&atilde;o existe nesta org</td></tr>"
            + "<tr><td><b>Divergente</b></td><td>O item existe em ambas as orgs mas com valores diferentes</td></tr>"
            + "<tr><td>string, boolean, picklist&hellip;</td><td>Tipo de dado de um campo Salesforce</td></tr>"
            + "<tr><td>v1, v2, v60&hellip;</td><td>VersionNumber de um Flow</td></tr>"
            + "</table>"
            + "<h3>Ordena&ccedil;&atilde;o e Reorganiza&ccedil;&atilde;o</h3>"
            + "<p>Clique no <b>cabe&ccedil;alho de qualquer coluna</b> para ordenar crescente. Clique novamente para decrescente. O terceiro clique remove a ordena&ccedil;&atilde;o.</p>"
            + "<p>Para reorganizar as colunas, arraste o cabe&ccedil;alho de uma coluna para a posi&ccedil;&atilde;o desejada.</p>"
            + "<h3>Filtros</h3>"
            + "<p>Abaixo dos cabe&ccedil;alhos, cada coluna possui um combo de filtro. Selecione um valor para filtrar a tabela. M&uacute;ltiplos filtros s&atilde;o combinados com l&oacute;gica <b>AND</b>.</p>"
            + "<p>O bot&atilde;o <b>Limpar Filtros</b> remove todos os filtros ativos e restaura todas as linhas.</p>"
            + "<h3>Duplo Clique &mdash; Visualizador de Diff</h3>"
            + "<p>Clique duas vezes em uma linha com status <b>Divergente</b> que possua conte&uacute;do armazenado para abrir o <b>Visualizador de Diff</b> &mdash; uma janela de compara&ccedil;&atilde;o lado a lado do conte&uacute;do dos dois arquivos de metadados.</p>"
            + "<h3>Visualizador de Diff</h3>"
            + "<p>O Visualizador de Diff exibe o conte&uacute;do dos dois arquivos em colunas paralelas, com n&uacute;meros de linha e codifica&ccedil;&atilde;o por cores:</p>"
            + "<table>"
            + "<tr><th>Cor</th><th>Significado</th></tr>"
            + "<tr><td style='background-color:#ffffff;border:1px solid #aaa;'>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b>Branco</b> &mdash; Linha id&ecirc;ntica nos dois arquivos</td></tr>"
            + "<tr><td style='background-color:#ffffcc;border:1px solid #aaa;'>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b>Amarelo</b> &mdash; Linha modificada (diferente em ambos os lados)</td></tr>"
            + "<tr><td style='background-color:#dcffdc;border:1px solid #aaa;'>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b>Verde</b> &mdash; Linha presente apenas no lado esquerdo (removida do lado direito)</td></tr>"
            + "<tr><td style='background-color:#ffdcdc;border:1px solid #aaa;'>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b>Vermelho</b> &mdash; Linha presente apenas no lado direito (adicionada &agrave; direita)</td></tr>"
            + "</table>"
            + "<p>O cabe&ccedil;alho identifica cada lado: em compara&ccedil;&otilde;es entre orgs, o lado esquerdo &eacute; <i>Org 1</i> e o direito &eacute; <i>Org 2</i>. Em compara&ccedil;&otilde;es com reposit&oacute;rio, o lado esquerdo &eacute; <i>Reposit&oacute;rio Local</i> e o direito &eacute; a org selecionada.</p>"
            + "<p>Para linhas cujo status <b>n&atilde;o</b> seja Divergente ou que n&atilde;o possuam conte&uacute;do armazenado (ex: apenas Presente ou Ausente), o duplo clique exibe um di&aacute;logo simples com o texto completo da coluna <i>Detalhes</i>.</p>"
            + "<table><tr><td><b>Obs.:</b> O algoritmo de diff usa LCS (<i>Longest Common Subsequence</i>) para calcular a menor quantidade de mudan&ccedil;as entre os arquivos. Para arquivos com mais de 2.000 linhas por lado, o visualizador exibe o conte&uacute;do em alinhamento simples sem c&aacute;lculo de LCS, para preservar o desempenho.</td></tr></table>"
            + "<h3>Exportar para CSV</h3>"
            + "<p>O bot&atilde;o <b>Exportar para CSV</b> salva as linhas <i>atualmente vis&iacute;veis</i> (respeitando filtros ativos) em um arquivo CSV abr&iacute;vel no Excel ou Google Sheets.</p>"
            + "<ul>"
            + "<li>O nome do arquivo inclui a data e hora da exporta&ccedil;&atilde;o</li>"
            + "<li>Se filtros estiverem ativos, apenas as linhas vis&iacute;veis s&atilde;o exportadas</li>"
            + "</ul>"
            + "<h3>Limpar Resultados</h3>"
            + "<p>O bot&atilde;o <b>Limpar</b> apaga todos os resultados da tabela, permitindo iniciar uma nova compara&ccedil;&atilde;o sem reiniciar o software.</p>"
            + "<hr/>"

            // ── 6. Barra de Ferramentas ──────────────────────────────────
            + "<a name='toolbar'></a>"
            + "<h2>6. Barra de Ferramentas</h2>"
            + "<p>Localizada no topo da janela, abaixo do cabe&ccedil;alho azul.</p>"
            + "<table>"
            + "<tr><th>Bot&atilde;o</th><th>Fun&ccedil;&atilde;o</th></tr>"
            + "<tr><td><b>Salvar Configura&ccedil;&otilde;es</b> (ícone de disquete)</td><td>Salva todos os campos preenchidos (dados das orgs e op&ccedil;&otilde;es de compara&ccedil;&atilde;o) em um arquivo <code>.sfcs</code> criptografado. As credenciais s&atilde;o protegidas com AES-256.</td></tr>"
            + "<tr><td><b>Carregar Configura&ccedil;&otilde;es</b> (ícone de pasta)</td><td>Carrega um arquivo <code>.sfcs</code> e preenche automaticamente todos os campos do formul&aacute;rio.</td></tr>"
            + "<tr><td><b>PT / EN</b> (ícone de globo)</td><td>Alterna o idioma da interface entre Portugu&ecirc;s (PT-BR) e Ingl&ecirc;s (EN). Todos os textos s&atilde;o atualizados imediatamente, incluindo a tabela de resultados.</td></tr>"
            + "<tr><td><b>Ajuda</b> (ícone ?)</td><td>Abre esta janela de ajuda.</td></tr>"
            + "</table>"
            + "<hr/>"

            // ── 7. Salvar/Carregar Configurações ─────────────────────────
            + "<a name='save-load'></a>"
            + "<h2>7. Salvar e Carregar Configura&ccedil;&otilde;es</h2>"
            + "<p>Salve as configura&ccedil;&otilde;es para n&atilde;o precisar redigitar os dados a cada sess&atilde;o.</p>"
            + "<h3>Salvar</h3>"
            + "<ol>"
            + "<li>Preencha os campos desejados (orgs e op&ccedil;&otilde;es de compara&ccedil;&atilde;o)</li>"
            + "<li>Clique em <b>Salvar Configura&ccedil;&otilde;es</b> na barra de ferramentas</li>"
            + "<li>Escolha o local e o nome do arquivo (extens&atilde;o <code>.sfcs</code>)</li>"
            + "<li>Clique em <b>Salvar</b></li>"
            + "</ol>"
            + "<h3>Carregar</h3>"
            + "<ol>"
            + "<li>Clique em <b>Carregar Configura&ccedil;&otilde;es</b> na barra de ferramentas</li>"
            + "<li>Navegue at&eacute; o arquivo <code>.sfcs</code> salvo anteriormente</li>"
            + "<li>Clique em <b>Abrir</b> &mdash; todos os campos ser&atilde;o preenchidos automaticamente</li>"
            + "</ol>"
            + "<table><tr><td><b>Seguran&ccedil;a:</b> O arquivo <code>.sfcs</code> &eacute; criptografado com AES-256 e vinculado &agrave; m&aacute;quina onde foi criado. N&atilde;o &eacute; poss&iacute;vel abri-lo em outra m&aacute;quina &mdash; isso protege as credenciais armazenadas. Se precisar usar o software em outra m&aacute;quina, crie um novo arquivo nela.</td></tr></table>"
            + "<hr/>"

            // ── 8. Limitações e Erros ────────────────────────────────────
            + "<a name='errors'></a>"
            + "<h2>8. Limita&ccedil;&otilde;es e Erros Comuns</h2>"
            + "<h3>Pr&eacute;-requisitos</h3>"
            + "<ul>"
            + "<li><b>Java 21</b> ou superior instalado</li>"
            + "<li><b>Salesforce CLI</b> (<code>sf</code>) instalado e acess&iacute;vel no PATH do sistema</li>"
            + "<li>Conex&atilde;o com a internet para acessar as orgs Salesforce</li>"
            + "<li>Connected App configurada em cada org com OAuth habilitado</li>"
            + "</ul>"
            + "<h3>Erros Comuns e Solu&ccedil;&otilde;es</h3>"
            + "<table>"
            + "<tr><th>Mensagem</th><th>Causa Prov&aacute;vel</th><th>Solu&ccedil;&atilde;o</th></tr>"
            + "<tr><td><b>Salesforce CLI n&atilde;o encontrado no PATH</b></td>"
            + "<td>O CLI <code>sf</code> n&atilde;o est&aacute; instalado ou n&atilde;o foi adicionado ao PATH</td>"
            + "<td>Instale em <code>developer.salesforce.com/tools/salesforcecli</code> e <b>reinicie o computador</b> para atualizar o PATH</td></tr>"
            + "<tr><td><b>Falha de autentica&ccedil;&atilde;o / invalid_grant</b></td>"
            + "<td>Credenciais incorretas ou token expirado; Connected App sem os escopos OAuth corretos</td>"
            + "<td>Verifique usu&aacute;rio, senha e token. Redefina o Security Token em: Settings &rarr; Personal Information &rarr; Reset My Security Token</td></tr>"
            + "<tr><td><b>Erro de SSL / certificado inv&aacute;lido</b></td>"
            + "<td>Proxy corporativo interceptando conex&otilde;es HTTPS</td>"
            + "<td>Execute o software via <code>run_debug.bat</code> para obter logs SSL detalhados. Contate o administrador de rede.</td></tr>"
            + "<tr><td><b>Objeto n&atilde;o encontrado</b></td>"
            + "<td>API Name do objeto incorreto (erro de grafia ou ausente o sufixo <code>__c</code>)</td>"
            + "<td>Consulte o API Name exato em Setup &rarr; Object Manager no Salesforce.</td></tr>"
            + "<tr><td><b>Nenhuma diferen&ccedil;a encontrada</b></td>"
            + "<td>As orgs est&atilde;o sincronizadas para as op&ccedil;&otilde;es selecionadas</td>"
            + "<td>Verifique se selecionou as op&ccedil;&otilde;es corretas. Se esperava diferen&ccedil;as, investigue diretamente nas orgs.</td></tr>"
            + "<tr><td><b>Compara&ccedil;&atilde;o muito lenta</b></td>"
            + "<td>Grande volume de metadados (ex: centenas de Profiles ou Permission Sets)</td>"
            + "<td>Use os campos de filtro de Permission Sets e Profiles para limitar o escopo. Desmarque op&ccedil;&otilde;es n&atilde;o necess&aacute;rias.</td></tr>"
            + "<tr><td><b>Arquivo .sfcs n&atilde;o pode ser carregado</b></td>"
            + "<td>O arquivo foi criado em outra m&aacute;quina</td>"
            + "<td>Arquivos de configura&ccedil;&atilde;o s&atilde;o vinculados &agrave; m&aacute;quina de origem. Crie um novo arquivo nesta m&aacute;quina.</td></tr>"
            + "</table>"
            + "<h3>Limita&ccedil;&otilde;es Conhecidas</h3>"
            + "<ul>"
            + "<li><b>Campos de Objeto:</b> compara&ccedil;&atilde;o pelo tipo de dado (<i>field type</i>), n&atilde;o pelo r&oacute;tulo (<i>label</i>). Renomear o r&oacute;tulo sem mudar o tipo n&atilde;o gera diferen&ccedil;a.</li>"
            + "<li><b>Layouts de P&aacute;gina:</b> comparados pela exist&ecirc;ncia e quantidade de linhas &mdash; a estrutura interna do layout n&atilde;o &eacute; analisada.</li>"
            + "<li><b>Apex Triggers:</b> compara&ccedil;&atilde;o por ApiVersion e quantidade de linhas de c&oacute;digo &mdash; mudan&ccedil;as internas com mesmo tamanho podem n&atilde;o ser detectadas.</li>"
            + "<li><b>Flows:</b> compara&ccedil;&atilde;o por VersionNumber &mdash; dois flows com o mesmo n&uacute;mero de vers&atilde;o n&atilde;o s&atilde;o considerados divergentes mesmo que o conte&uacute;do difira.</li>"
            + "<li><b>Profiles e Permission Sets:</b> comparados por quantidade de linhas no XML &mdash; permiss&otilde;es individuais n&atilde;o s&atilde;o detalhadas nesta vers&atilde;o.</li>"
            + "<li>Metadados n&atilde;o listados nas op&ccedil;&otilde;es (ex: Reports, Dashboards, Workflows legados) n&atilde;o s&atilde;o verificados por esta vers&atilde;o do software.</li>"
            + "<li><b>Visualizador de Diff:</b> para arquivos com mais de 2.000 linhas por lado, o algoritmo LCS &eacute; desativado e o conte&uacute;do &eacute; exibido em alinhamento simples. O diff pode ser menos preciso nesses casos.</li>"
            + "</ul>"
            + "<hr/>"

            // ── 9. Comparar com Repositório ──────────────────────────────
            + "<a name='repo-compare'></a>"
            + "<h2>9. Comparar com Reposit&oacute;rio Local</h2>"
            + "<p>A aba <i>Comparar Reposit&oacute;rio</i> permite comparar os metadados presentes em uma org Salesforce com os metadados armazenados em um projeto SFDX local. Ideal para auditar se a org est&aacute; em conformidade com o c&oacute;digo-fonte versionado no reposit&oacute;rio.</p>"
            + "<h3>Pr&eacute;-requisito: Caminho do Projeto Local</h3>"
            + "<p>Antes de usar esta aba, preencha o campo <b>Caminho do Projeto Local</b> na aba <i>Configura&ccedil;&atilde;o</i>. O caminho deve apontar para a pasta raiz do projeto SFDX &mdash; aquela que cont&eacute;m a estrutura <code>force-app/main/default/</code>.</p>"
            + "<ul>"
            + "<li>Se o caminho informado n&atilde;o existir, uma mensagem de erro em vermelho &eacute; exibida abaixo do campo.</li>"
            + "<li>A aba <i>Comparar Reposit&oacute;rio</i> fica desabilitada at&eacute; que um caminho v&aacute;lido seja fornecido.</li>"
            + "</ul>"
            + "<h3>Sele&ccedil;&atilde;o de Tipos de Metadados</h3>"
            + "<p>Ao informar um caminho v&aacute;lido, o software varre as pastas do projeto automaticamente e <b>habilita apenas os checkboxes</b> cujas pastas de metadado existem e possuem conte&uacute;do. Tipos cujas pastas estejam ausentes ou vazias permanecem desabilitados.</p>"
            + "<table><tr><td><b>Aten&ccedil;&atilde;o:</b> Se mais de 3 tipos forem selecionados simultaneamente, um aviso de desempenho ser&aacute; exibido antes de iniciar a compara&ccedil;&atilde;o, pois o processo pode ser lento.</td></tr></table>"
            + "<h3>Bot&otilde;es de Compara&ccedil;&atilde;o</h3>"
            + "<table>"
            + "<tr><th>Bot&atilde;o</th><th>A&ccedil;&atilde;o</th></tr>"
            + "<tr><td><b>Comparar com Org 1</b> (azul)</td><td>Compara os metadados selecionados contra os presentes na Org 1</td></tr>"
            + "<tr><td><b>Comparar com Org 2</b> (verde)</td><td>Compara os metadados selecionados contra os presentes na Org 2</td></tr>"
            + "</table>"
            + "<h3>Fluxo de Execu&ccedil;&atilde;o</h3>"
            + "<ol>"
            + "<li>O software autentica na org selecionada via OAuth 2.0 (usando as credenciais da aba <i>Configura&ccedil;&atilde;o</i>)</li>"
            + "<li>Para cada tipo de metadado selecionado, <b>recupera todos os itens diretamente da org</b> via Salesforce CLI</li>"
            + "<li>Compara os itens recuperados com os arquivos <code>*-meta.xml</code> na pasta correspondente do projeto local</li>"
            + "<li>Exibe os resultados na aba <i>Resultados</i></li>"
            + "</ol>"
            + "<h3>Interpreta&ccedil;&atilde;o dos Resultados</h3>"
            + "<table>"
            + "<tr><th>Situa&ccedil;&atilde;o</th><th>Org X</th><th>Detalhes</th></tr>"
            + "<tr><td>Item existe na org, mas n&atilde;o no reposit&oacute;rio local</td><td><b>Presente</b></td><td>Presente na Org X, ausente no reposit&oacute;rio local.</td></tr>"
            + "<tr><td>Item existe no reposit&oacute;rio local, mas n&atilde;o na org</td><td><b>Ausente</b></td><td>Presente no reposit&oacute;rio local, ausente na Org X.</td></tr>"
            + "<tr><td>Item existe em ambos com conte&uacute;do diferente</td><td><b>Divergente</b></td><td>Local: X bytes | Org X: Y bytes.</td></tr>"
            + "</table>"
            + "<h3>Visualizador de Diff &mdash; Duplo Clique nos Resultados</h3>"
            + "<p>Para itens com status <b>Divergente</b>, clique duas vezes na linha da tabela de resultados para abrir o <b>Visualizador de Diff</b>. O lado esquerdo exibe o conte&uacute;do do arquivo local (<i>Reposit&oacute;rio Local</i>) e o lado direito exibe o conte&uacute;do recuperado da org (<i>Org 1</i> ou <i>Org 2</i>).</p>"
            + "<p>O conte&uacute;do &eacute; normalizado antes da compara&ccedil;&atilde;o: quebras de linha no estilo Windows (CRLF) s&atilde;o convertidas para o padr&atilde;o Unix (LF) e espa&ccedil;os/tabs invis&iacute;veis no final das linhas s&atilde;o removidos, eliminando falsos positivos causados por formata&ccedil;&atilde;o invis&iacute;vel.</p>"
            + "<table><tr><td><b>Nota:</b> A compara&ccedil;&atilde;o &eacute; feita pelo <b>nome do arquivo</b> (independente do caminho relativo interno) e pelo <b>conte&uacute;do</b> dos arquivos <code>*-meta.xml</code>. A estrutura interna dos metadados n&atilde;o &eacute; analisada item a item nesta vers&atilde;o.</td></tr></table>"
            + "<hr/>"
            + "<p style='text-align:center;color:#888;font-size:10pt;margin-top:20px;'><i>Salesforce Org Comparator &mdash; v1.2 &mdash; Criado por Raktif</i></p>"
            + "</body></html>";
    }

    // ====================================================================
    //  HTML  –  English (EN)
    // ====================================================================

    private static String htmlEN() {
        return "<html><head>" + CSS + "</head><body>"

            + "<a name='overview'></a>"
            + "<h1>User Manual &mdash; Salesforce Org Comparator</h1>"
            + "<p>Welcome to <b>Salesforce Org Comparator</b>! This manual explains all features of the software.</p>"
            + "<hr/>"

            // ── 1. Overview ──────────────────────────────────────────────
            + "<h2>1. Overview</h2>"
            + "<p>The <b>Salesforce Org Comparator</b> is a desktop tool for comparing metadata between two Salesforce environments (<b>orgs</b>). Use it to:</p>"
            + "<ul>"
            + "<li>Identify configuration differences between production and sandbox</li>"
            + "<li>Audit and validate deployments and releases</li>"
            + "<li>Verify parity between development and QA environments</li>"
            + "<li>Detect fields, profiles, or permissions present in one org but absent in another</li>"
            + "</ul>"
            + "<p>The software uses the <b>Salesforce CLI</b> and the <b>Salesforce REST API</b> to retrieve metadata. Results are displayed in an interactive table that can be filtered, sorted, and exported to CSV.</p>"
            + "<table>"
            + "<tr><th>Comparison Type</th><th>Description</th></tr>"
            + "<tr><td><b>Per object (SObject)</b></td><td>Fields, Page Layouts, Validation Rules, Flows, Apex Triggers, Record Types</td></tr>"
            + "<tr><td><b>Org-wide</b></td><td>Custom Metadata, Custom Settings, Permission Sets, Profiles, Public Groups, Approval Processes, Named Credentials</td></tr>"
            + "</table>"
            + "<hr/>"

            // ── 2. Org Setup ─────────────────────────────────────────────
            + "<a name='org-setup'></a>"
            + "<h2>2. Org Setup</h2>"
            + "<p>To connect to a Salesforce org, you need a <b>Connected App</b> with OAuth 2.0 enabled. Fill in the fields for both orgs in the <i>Configuration</i> tab.</p>"
            + "<h3>Form Fields</h3>"
            + "<table>"
            + "<tr><th>Field</th><th>Description</th><th>Example</th></tr>"
            + "<tr><td><b>Instance URL</b></td><td>Base URL of your Salesforce org</td><td>https://yourcompany.my.salesforce.com</td></tr>"
            + "<tr><td><b>Security Token</b></td><td>Alphanumeric code emailed when creating an account or resetting a password</td><td>xKz8a1B9...</td></tr>"
            + "<tr><td><b>Client ID</b></td><td>Consumer Key of the Connected App</td><td>3MVG9...</td></tr>"
            + "<tr><td><b>Client Secret</b></td><td>Consumer Secret of the Connected App</td><td>A1B2C3...</td></tr>"
            + "</table>"
            + "<h3>Instance URL</h3>"
            + "<ul>"
            + "<li><b>Production:</b> <code>https://login.salesforce.com</code> or <code>https://yourcompany.my.salesforce.com</code></li>"
            + "<li><b>Sandbox:</b> <code>https://test.salesforce.com</code> or <code>https://yourcompany--sandbox.sandbox.my.salesforce.com</code></li>"
            + "</ul>"
            + "<h3>Security Token</h3>"
            + "<p>An alphanumeric code generated by Salesforce for API authentication outside trusted networks. To obtain or reset it:</p>"
            + "<ol>"
            + "<li>In your Salesforce org, log in as the user selected in <b>Client Credentials Flow</b> and click your username (top right)</li>"
            + "<li>Go to <i>Settings</i> &rarr; <i>Personal Information</i> &rarr; <i>Reset My Security Token</i></li>"
            + "<li>The new token will be sent to your registered email address</li>"
            + "</ol>"
            + "<table><tr><td><b>Note:</b> If your network IP is in the org&apos;s Trusted IP Ranges, the security token may not be required &mdash; leave the field blank in that case.</td></tr></table>"
            + "<h3>How to Create a Connected App</h3>"
            + "<ol>"
            + "<li>Access the <b>Setup</b> of the desired org</li>"
            + "<li>In Quick Find, search for <i>External</i> &rarr; <i>Settings</i> &rarr; <b>New Connected App</b></li>"
            + "<li>Fill in: Connected App Name, API Name, Contact Email</li>"
            + "<li>Check <b>Enable OAuth Settings</b></li>"
            + "<li>For Callback URL, enter <code>https://login.salesforce.com/services/oauth2/success</code> or the specific value for your org</li>"
            + "<li>In Selected OAuth Scopes, add: <b>Manage user data via APIs (api)</b>, <b>Perform requests at any time (refresh_token, offline_access)</b></li>"
            + "<li>Click <b>Save</b> and wait 2&ndash;10 minutes for activation</li>"
            + "<li>After saving, copy the <i>Consumer Key</i> (Client ID) and the <i>Consumer Secret</i></li>"
            + "</ol>"
            + "<table><tr><td><b>Tip:</b> The same Connected App can be configured in both orgs for use with the comparator.</td></tr></table>"
            + "<hr/>"

            // ── 3. Comparison Configuration ──────────────────────────────
            + "<a name='comp-config'></a>"
            + "<h2>3. Comparison Configuration</h2>"
            + "<h3>Object Name (SObject API Name)</h3>"
            + "<p>For object-level comparisons, enter the <b>API Name</b> of the SObject. Examples:</p>"
            + "<ul>"
            + "<li>Standard objects: <code>Opportunity</code>, <code>Account</code>, <code>Lead</code>, <code>Contact</code>, <code>Case</code></li>"
            + "<li>Custom objects: <code>MyList__c</code>, <code>Custom_Order__c</code></li>"
            + "</ul>"
            + "<table><tr><td><b>Note:</b> The API Name is <b>case-sensitive</b>. Custom objects end in <code>__c</code>. Spelling errors result in an &quot;Object not found&quot; message.</td></tr></table>"
            + "<h3>Per-Object Options</h3>"
            + "<p>These require the <i>Object Name</i> field to be filled in:</p>"
            + "<table>"
            + "<tr><th>Option</th><th>What is Compared</th></tr>"
            + "<tr><td><b>Object Fields</b></td><td>API Name and data type of each field. Detects fields absent in one org or fields with different types between orgs.</td></tr>"
            + "<tr><td><b>Page Layouts</b></td><td>Existence and name of Page Layouts for the object.</td></tr>"
            + "<tr><td><b>Validation Rules</b></td><td>Existence of Validation Rules for the object.</td></tr>"
            + "<tr><td><b>Flows (Record-Triggered)</b></td><td>Record-Triggered Flows associated with the object. Compares VersionNumber.</td></tr>"
            + "<tr><td><b>Apex Triggers</b></td><td>Triggers for the object. Compares Status, ApiVersion, and source code size.</td></tr>"
            + "<tr><td><b>Record Types</b></td><td>Existence of Record Types for the object.</td></tr>"
            + "</table>"
            + "<h3>Org-Wide Options</h3>"
            + "<p>These do not require an object name &mdash; they compare the entire org:</p>"
            + "<table>"
            + "<tr><th>Option</th><th>What is Compared</th></tr>"
            + "<tr><td><b>Custom Metadata Types</b></td><td>Custom Metadata Type definitions.</td></tr>"
            + "<tr><td><b>Custom Settings</b></td><td>Custom Settings (Hierarchy and List).</td></tr>"
            + "<tr><td><b>Permission Sets</b></td><td>Existence and content of Permission Sets.</td></tr>"
            + "<tr><td><b>Profiles</b></td><td>Existence and content of Profiles.</td></tr>"
            + "<tr><td><b>Public Groups</b></td><td>Existence of Public Groups.</td></tr>"
            + "<tr><td><b>Approval Processes</b></td><td>Existence and status of Approval Processes.</td></tr>"
            + "<tr><td><b>Named Credentials</b></td><td>Existence of Named Credentials.</td></tr>"
            + "</table>"
            + "<h3>Permission Sets and Profiles Filters</h3>"
            + "<p>The filter fields accept a partial term (case-insensitive) to limit comparisons to matching items. If empty, <b>all</b> items are compared.</p>"
            + "<p><b>Example:</b> Typing <code>Sales</code> returns only Permission Sets or Profiles whose names contain &quot;Sales&quot;.</p>"
            + "<table><tr><td><b>Tip:</b> Use filters in orgs with hundreds of Permission Sets or Profiles to significantly reduce comparison time.</td></tr></table>"
            + "<hr/>"

            // ── 4. Running the Comparison ────────────────────────────────
            + "<a name='running'></a>"
            + "<h2>4. Running the Comparison</h2>"
            + "<p>With the fields filled in and at least one option selected, click the <b>Start Comparison</b> button (in the bottom bar).</p>"
            + "<h3>Execution Flow</h3>"
            + "<ol>"
            + "<li>The software validates required fields (Instance URL and Client ID for both orgs)</li>"
            + "<li>A progress window opens showing the current step</li>"
            + "<li>Authenticates to both orgs via OAuth 2.0</li>"
            + "<li>For each selected metadata type, retrieves and compares data from both orgs</li>"
            + "<li>Displays results in the <i>Results</i> tab and closes the progress window</li>"
            + "</ol>"
            + "<h3>Progress Window</h3>"
            + "<ul>"
            + "<li>Shows the current step (e.g., <i>Verifying Profiles...</i>)</li>"
            + "<li>The <b>Cancel</b> button stops the process safely and releases all temporary resources</li>"
            + "<li>Temporary orgs registered in the Salesforce CLI are automatically removed when done or cancelled</li>"
            + "</ul>"
            + "<table><tr><td><b>Note:</b> Do not close the main window during comparison. Wait for completion or click Cancel.</td></tr></table>"
            + "<hr/>"

            // ── 5. Results Table ─────────────────────────────────────────
            + "<a name='results'></a>"
            + "<h2>5. Results Table</h2>"
            + "<p>The <i>Results</i> tab displays all differences found in an interactive table.</p>"
            + "<h3>Columns</h3>"
            + "<table>"
            + "<tr><th>Column</th><th>Content</th></tr>"
            + "<tr><td><b>Category</b></td><td>Metadata type (e.g., Object Fields, Approval Processes). For object comparisons, includes the object name in parentheses.</td></tr>"
            + "<tr><td><b>Name</b></td><td>API Name of the differing item (field name, layout name, profile name, etc.)</td></tr>"
            + "<tr><td><b>Org 1</b></td><td>Status or value in Org 1: <i>Present</i>, <i>Absent</i>, <i>Divergent</i>, or the field&apos;s technical data type.</td></tr>"
            + "<tr><td><b>Org 2</b></td><td>Status or value in Org 2 (same).</td></tr>"
            + "<tr><td><b>Details</b></td><td>Additional technical information about the difference. <b>Always shown in English</b>, regardless of the selected language.</td></tr>"
            + "</table>"
            + "<h3>Value Interpretation</h3>"
            + "<table>"
            + "<tr><th>Value</th><th>Meaning</th></tr>"
            + "<tr><td><b>Present</b></td><td>The item exists in this org</td></tr>"
            + "<tr><td><b>Absent</b></td><td>The item does not exist in this org</td></tr>"
            + "<tr><td><b>Divergent</b></td><td>The item exists in both orgs but with different values</td></tr>"
            + "<tr><td>string, boolean, picklist&hellip;</td><td>Salesforce field data type</td></tr>"
            + "<tr><td>v1, v2, v60&hellip;</td><td>Flow VersionNumber</td></tr>"
            + "</table>"
            + "<h3>Sorting and Reordering</h3>"
            + "<p>Click any <b>column header</b> to sort ascending. Click again for descending. A third click removes sorting.</p>"
            + "<p>To reorder columns, drag a column header to the desired position.</p>"
            + "<h3>Filters</h3>"
            + "<p>Below the column headers, each column has a filter combo. Select a value to filter the table. Multiple filters are combined with <b>AND</b> logic.</p>"
            + "<p>The <b>Clear Filters</b> button removes all active filters and restores all rows.</p>"
            + "<h3>Double-Click &mdash; Diff Viewer</h3>"
            + "<p>Double-clicking any row with <b>Divergent</b> status that has stored content opens the <b>Diff Viewer</b> &mdash; a side-by-side comparison window showing the content of both metadata files.</p>"
            + "<h3>Diff Viewer</h3>"
            + "<p>The Diff Viewer displays the content of both metadata files in parallel columns, with line numbers and color coding:</p>"
            + "<table>"
            + "<tr><th>Color</th><th>Meaning</th></tr>"
            + "<tr><td style='background-color:#ffffff;border:1px solid #aaa;'>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b>White</b> &mdash; Identical line in both files</td></tr>"
            + "<tr><td style='background-color:#ffffcc;border:1px solid #aaa;'>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b>Yellow</b> &mdash; Modified line (different on both sides)</td></tr>"
            + "<tr><td style='background-color:#dcffdc;border:1px solid #aaa;'>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b>Green</b> &mdash; Line present only on the left side (removed from right)</td></tr>"
            + "<tr><td style='background-color:#ffdcdc;border:1px solid #aaa;'>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b>Red</b> &mdash; Line present only on the right side (added on right)</td></tr>"
            + "</table>"
            + "<p>The header identifies each side: in org-to-org comparisons, the left side is <i>Org 1</i> and the right is <i>Org 2</i>. In repository comparisons, the left side is <i>Local Repository</i> and the right is the selected org.</p>"
            + "<p>For rows whose status is <b>not</b> Divergent or that do not have stored content (e.g., Present-only or Absent-only), double-clicking shows a simple dialog with the full text of the <i>Details</i> column.</p>"
            + "<table><tr><td><b>Note:</b> The diff algorithm uses LCS (<i>Longest Common Subsequence</i>) to compute the minimal set of changes between files. For files with more than 2,000 lines per side, the viewer falls back to simple side-by-side alignment without LCS computation, to preserve performance.</td></tr></table>"
            + "<h3>Export to CSV</h3>"
            + "<p>The <b>Export to CSV</b> button saves the <i>currently visible</i> rows (respecting active filters) to a CSV file openable in Excel or Google Sheets.</p>"
            + "<ul>"
            + "<li>The file name includes the date and time of export</li>"
            + "<li>If filters are active, only visible rows are exported</li>"
            + "</ul>"
            + "<h3>Clear Results</h3>"
            + "<p>The <b>Clear</b> button removes all results from the table, allowing a new comparison without restarting the software.</p>"
            + "<hr/>"

            // ── 6. Toolbar ───────────────────────────────────────────────
            + "<a name='toolbar'></a>"
            + "<h2>6. Toolbar</h2>"
            + "<p>Located at the top of the window, below the blue header.</p>"
            + "<table>"
            + "<tr><th>Button</th><th>Function</th></tr>"
            + "<tr><td><b>Save Settings</b> (floppy icon)</td><td>Saves all filled fields (org data and comparison options) to an encrypted <code>.sfcs</code> file. Credentials are protected with AES-256.</td></tr>"
            + "<tr><td><b>Load Settings</b> (folder icon)</td><td>Loads a <code>.sfcs</code> file and automatically fills in all form fields.</td></tr>"
            + "<tr><td><b>PT / EN</b> (globe icon)</td><td>Toggles the interface language between Portuguese (PT-BR) and English (EN). All texts update immediately, including the results table.</td></tr>"
            + "<tr><td><b>Help</b> (? icon)</td><td>Opens this help window.</td></tr>"
            + "</table>"
            + "<hr/>"

            // ── 7. Save/Load Settings ────────────────────────────────────
            + "<a name='save-load'></a>"
            + "<h2>7. Save and Load Settings</h2>"
            + "<p>Save your settings to avoid re-entering data every session.</p>"
            + "<h3>Save</h3>"
            + "<ol>"
            + "<li>Fill in the desired fields (org credentials and comparison options)</li>"
            + "<li>Click <b>Save Settings</b> in the toolbar</li>"
            + "<li>Choose a location and file name (extension <code>.sfcs</code>)</li>"
            + "<li>Click <b>Save</b></li>"
            + "</ol>"
            + "<h3>Load</h3>"
            + "<ol>"
            + "<li>Click <b>Load Settings</b> in the toolbar</li>"
            + "<li>Browse to a previously saved <code>.sfcs</code> file</li>"
            + "<li>Click <b>Open</b> &mdash; all fields will be filled in automatically</li>"
            + "</ol>"
            + "<table><tr><td><b>Security:</b> The <code>.sfcs</code> file is encrypted with AES-256 and bound to the machine where it was created. It cannot be opened on a different machine, protecting stored credentials. If you need to use the software on another machine, create a new settings file on that machine.</td></tr></table>"
            + "<hr/>"

            // ── 8. Limitations & Errors ──────────────────────────────────
            + "<a name='errors'></a>"
            + "<h2>8. Limitations &amp; Common Errors</h2>"
            + "<h3>Prerequisites</h3>"
            + "<ul>"
            + "<li><b>Java 21</b> or higher installed</li>"
            + "<li><b>Salesforce CLI</b> (<code>sf</code>) installed and accessible in the system PATH</li>"
            + "<li>Internet connection to access Salesforce orgs</li>"
            + "<li>A Connected App configured in each org with OAuth enabled</li>"
            + "</ul>"
            + "<h3>Common Errors and Solutions</h3>"
            + "<table>"
            + "<tr><th>Error Message</th><th>Likely Cause</th><th>Solution</th></tr>"
            + "<tr><td><b>Salesforce CLI not found in PATH</b></td>"
            + "<td>The <code>sf</code> CLI is not installed or not added to PATH</td>"
            + "<td>Install at <code>developer.salesforce.com/tools/salesforcecli</code> and <b>restart your computer</b> to update PATH</td></tr>"
            + "<tr><td><b>Authentication failed / invalid_grant</b></td>"
            + "<td>Incorrect credentials or expired token; Connected App missing required OAuth scopes</td>"
            + "<td>Check credentials. Reset Security Token: Settings &rarr; Personal Information &rarr; Reset My Security Token</td></tr>"
            + "<tr><td><b>SSL error / invalid certificate</b></td>"
            + "<td>Corporate proxy intercepting HTTPS connections</td>"
            + "<td>Run via <code>run_debug.bat</code> to get detailed SSL logs. Contact your network administrator.</td></tr>"
            + "<tr><td><b>Object not found</b></td>"
            + "<td>Incorrect object API Name (typo or missing <code>__c</code> suffix)</td>"
            + "<td>Look up the exact API Name in Setup &rarr; Object Manager in Salesforce.</td></tr>"
            + "<tr><td><b>No differences found</b></td>"
            + "<td>Orgs are in sync for the selected options</td>"
            + "<td>Verify that you selected the right options. If you expected differences, investigate directly in the orgs.</td></tr>"
            + "<tr><td><b>Comparison is slow</b></td>"
            + "<td>Large volume of metadata (e.g., hundreds of Profiles or Permission Sets)</td>"
            + "<td>Use the Permission Sets and Profiles filter fields to limit scope. Uncheck unnecessary options.</td></tr>"
            + "<tr><td><b>.sfcs file cannot be loaded</b></td>"
            + "<td>The file was created on a different machine</td>"
            + "<td>Settings files are bound to the machine of origin. Create a new settings file on this machine.</td></tr>"
            + "</table>"
            + "<h3>Known Limitations</h3>"
            + "<ul>"
            + "<li><b>Object Fields:</b> compared by data type, not by label. Renaming a label without changing the type does not generate a difference.</li>"
            + "<li><b>Page Layouts:</b> compared by existence and line count &mdash; the internal layout structure is not analyzed.</li>"
            + "<li><b>Apex Triggers:</b> compared by ApiVersion and source code line count &mdash; internal changes of the same size may not be detected.</li>"
            + "<li><b>Flows:</b> compared by VersionNumber &mdash; two flows with the same version number are not flagged as divergent even if content differs.</li>"
            + "<li><b>Profiles and Permission Sets:</b> compared by XML line count &mdash; individual permissions are not detailed in this version.</li>"
            + "<li>Metadata not listed in the comparison options (e.g., Reports, Dashboards, legacy Workflows) is not checked in this version.</li>"
            + "<li><b>Diff Viewer:</b> for files with more than 2,000 lines per side, the LCS algorithm is disabled and content is shown in simple side-by-side alignment. Diff accuracy may be reduced in those cases.</li>"
            + "</ul>"
            + "<hr/>"

            // ── 9. Compare with Repository ───────────────────────────────
            + "<a name='repo-compare'></a>"
            + "<h2>9. Compare with Local Repository</h2>"
            + "<p>The <i>Compare Repository</i> tab allows you to compare metadata present in a Salesforce org against metadata stored in a local SFDX project. Ideal for auditing whether the org is in sync with the versioned source code in the repository.</p>"
            + "<h3>Prerequisite: Local Project Path</h3>"
            + "<p>Before using this tab, fill in the <b>Local Project Path</b> field in the <i>Configuration</i> tab. The path must point to the root folder of the SFDX project &mdash; the one containing the <code>force-app/main/default/</code> structure.</p>"
            + "<ul>"
            + "<li>If the provided path does not exist, a red error message appears below the field.</li>"
            + "<li>The <i>Compare Repository</i> tab remains disabled until a valid path is provided.</li>"
            + "</ul>"
            + "<h3>Selecting Metadata Types</h3>"
            + "<p>When a valid path is entered, the software automatically scans the project folders and <b>enables only the checkboxes</b> whose metadata folders exist and contain files. Types with absent or empty folders remain disabled.</p>"
            + "<table><tr><td><b>Note:</b> If more than 3 metadata types are selected at once, a performance warning will appear before starting the comparison, as the process may be slow.</td></tr></table>"
            + "<h3>Comparison Buttons</h3>"
            + "<table>"
            + "<tr><th>Button</th><th>Action</th></tr>"
            + "<tr><td><b>Compare with Org 1</b> (blue)</td><td>Compares the selected metadata against those present in Org 1</td></tr>"
            + "<tr><td><b>Compare with Org 2</b> (green)</td><td>Compares the selected metadata against those present in Org 2</td></tr>"
            + "</table>"
            + "<h3>Execution Flow</h3>"
            + "<ol>"
            + "<li>The software authenticates to the selected org via OAuth 2.0 (using the credentials from the <i>Configuration</i> tab)</li>"
            + "<li>For each selected metadata type, <b>retrieves all items directly from the org</b> via Salesforce CLI</li>"
            + "<li>Compares the retrieved items against the <code>*-meta.xml</code> files in the corresponding folder of the local project</li>"
            + "<li>Displays results in the <i>Results</i> tab</li>"
            + "</ol>"
            + "<h3>Interpreting Results</h3>"
            + "<table>"
            + "<tr><th>Situation</th><th>Org X</th><th>Details</th></tr>"
            + "<tr><td>Item exists in org but not in the local repository</td><td><b>Present</b></td><td>Present in Org X, absent from local repository.</td></tr>"
            + "<tr><td>Item exists in the local repository but not in the org</td><td><b>Absent</b></td><td>Present in local repository, absent from Org X.</td></tr>"
            + "<tr><td>Item exists in both but with different content</td><td><b>Divergent</b></td><td>Local: X bytes | Org X: Y bytes.</td></tr>"
            + "</table>"
            + "<h3>Diff Viewer &mdash; Double-Click on Results</h3>"
            + "<p>For items with <b>Divergent</b> status, double-click the row in the results table to open the <b>Diff Viewer</b>. The left side displays the content of the local file (<i>Local Repository</i>) and the right side shows the content retrieved from the org (<i>Org 1</i> or <i>Org 2</i>).</p>"
            + "<p>Content is normalized before comparison: Windows-style line endings (CRLF) are converted to Unix (LF) and invisible trailing spaces or tabs are removed from each line, eliminating false positives caused by invisible formatting.</p>"
            + "<table><tr><td><b>Note:</b> Comparison is performed by <b>file name</b> (regardless of the internal relative path) and by <b>content</b> of the <code>*-meta.xml</code> files. The internal metadata structure is not analyzed item by item in this version.</td></tr></table>"
            + "<hr/>"
            + "<p style='text-align:center;color:#888;font-size:10pt;margin-top:20px;'><i>Salesforce Org Comparator &mdash; v1.2 &mdash; Created by Raktif</i></p>"
            + "</body></html>";
    }
}
