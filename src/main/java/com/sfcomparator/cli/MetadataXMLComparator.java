package com.sfcomparator.cli;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Compara arquivos XML de metadados Salesforce (Profiles, Permission Sets, etc.)
 * recuperados via 'sf metadata retrieve'.
 *
 * Estrutura esperada dos arquivos MDAPI:
 *   tmpDir/unpackaged/profiles/<name>.profile
 *   tmpDir/unpackaged/permissionsets/<name>.permissionset
 */
public class MetadataXMLComparator {

    // Cada entrada: [tagDoElemento, tagFilhaChaverParaIdentificação]
    private static final String[][] SECTIONS = {
        {"fieldPermissions",        "field"},
        {"objectPermissions",       "object"},
        {"userPermissions",         "name"},
        {"recordTypeVisibilities",  "recordType"},
        {"tabVisibilities",         "tab"},
        {"tabSettings",             "tab"},
        {"classAccesses",           "apexClass"},
        {"pageAccesses",            "apexPage"},
        {"applicationVisibilities", "application"},
        {"customPermissions",       "name"},
        {"layoutAssignments",       "layout"},
    };

    /**
     * Compara dois arquivos XML de metadados Salesforce.
     * Retorna lista de strings descrevendo cada divergência encontrada.
     */
    public static List<String> compare(Path file1, Path file2) throws Exception {
        Document doc1 = parseXML(file1);
        Document doc2 = parseXML(file2);
        List<String> diffs = new ArrayList<>();
        for (String[] s : SECTIONS) {
            compareSection(doc1, doc2, s[0], s[1], diffs);
        }
        return diffs;
    }

    /**
     * Lista arquivos em um diretório com a extensão fornecida.
     * Retorna mapa: nomeBase (sem extensão) → Path completo.
     */
    public static Map<String, Path> listFiles(Path dir, String extension) throws IOException {
        Map<String, Path> map = new LinkedHashMap<>();
        if (!Files.exists(dir)) return map;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                String name = file.getFileName().toString();
                if (name.endsWith(extension)) {
                    map.put(name.substring(0, name.length() - extension.length()), file);
                }
            }
        }
        return map;
    }

    // -----------------------------------------------------------------------
    // Comparação por seção
    // -----------------------------------------------------------------------

    private static void compareSection(Document doc1, Document doc2,
                                       String sectionTag, String keyTag,
                                       List<String> diffs) {
        Map<String, String> map1 = extractSection(doc1, sectionTag, keyTag);
        Map<String, String> map2 = extractSection(doc2, sectionTag, keyTag);
        if (map1.isEmpty() && map2.isEmpty()) return;

        for (Map.Entry<String, String> e : map1.entrySet()) {
            String key = e.getKey();
            if (!map2.containsKey(key)) {
                diffs.add("[" + sectionTag + "] " + key + ": presente na Org1, ausente na Org2");
            } else if (!e.getValue().equals(map2.get(key))) {
                diffs.add("[" + sectionTag + "] " + key + ":\n" +
                          "  Org1: " + e.getValue() + "\n" +
                          "  Org2: " + map2.get(key));
            }
        }
        for (String key : map2.keySet()) {
            if (!map1.containsKey(key)) {
                diffs.add("[" + sectionTag + "] " + key + ": ausente na Org1, presente na Org2");
            }
        }
    }

    /**
     * Extrai uma seção do documento XML como mapa: chave → propriedades (ordenadas, canônicas).
     */
    private static Map<String, String> extractSection(Document doc,
                                                       String sectionTag, String keyTag) {
        Map<String, String> map = new LinkedHashMap<>();
        NodeList nodes = doc.getElementsByTagName(sectionTag);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (!(n instanceof Element el)) continue;
            String key = getDirectChildText(el, keyTag);
            if (key == null || key.isEmpty()) continue;

            // Coleta propriedades dos filhos diretos (exceto o campo chave)
            TreeMap<String, String> props = new TreeMap<>();
            NodeList children = el.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) continue;
                if (child.getNodeName().equals(keyTag)) continue;
                props.put(child.getNodeName(), child.getTextContent().trim());
            }

            StringBuilder sb = new StringBuilder();
            props.forEach((k, v) -> {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(k).append("=").append(v);
            });
            map.put(key, sb.toString());
        }
        return map;
    }

    /** Busca o texto de um filho direto pelo nome da tag. */
    private static String getDirectChildText(Element parent, String childTag) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                child.getNodeName().equals(childTag)) {
                return child.getTextContent().trim();
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Parse XML com proteção contra XXE
    // -----------------------------------------------------------------------

    private static Document parseXML(Path file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        // Proteção contra ataques XXE (OWASP A05)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file.toFile());
    }
}
