package com.sfcomparator.model;

public class Difference {
    public enum DifferenceType {
        SOBJECT_FIELD_MISSING_IN_ORG2,
        SOBJECT_FIELD_MISSING_IN_ORG1,
        SOBJECT_FIELD_TYPE_MISMATCH,
        METADATA_MISSING_IN_ORG2,
        METADATA_MISSING_IN_ORG1,
        METADATA_STRUCTURE_MISMATCH
    }

    private DifferenceType type;
    private String category;  // e.g., "SObject", "Apex Class", "Flow"
    private String name;
    private String org1Value;
    private String org2Value;
    private String details;
    /** Conteúdo completo do lado esquerdo (local ou Org 1) para exibição no diff viewer. */
    private String content1;
    /** Conteúdo completo do lado direito (org ou Org 2) para exibição no diff viewer. */
    private String content2;

    public Difference(DifferenceType type, String category, String name, String org1Value, String org2Value) {
        this.type = type;
        this.category = category;
        this.name = name;
        this.org1Value = org1Value;
        this.org2Value = org2Value;
    }

    public DifferenceType getType() { return type; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public String getOrg1Value() { return org1Value; }
    public String getOrg2Value() { return org2Value; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getContent1() { return content1; }
    public String getContent2() { return content2; }
    public void setContent1(String content1) { this.content1 = content1; }
    public void setContent2(String content2) { this.content2 = content2; }

    @Override
    public String toString() {
        return String.format("| %s | %s | %s | %s |", category, name, org1Value != null ? org1Value : "N/A", org2Value != null ? org2Value : "N/A");
    }
}
