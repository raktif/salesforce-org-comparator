package com.sfcomparator.model;

public class ComparisonConfig {
    private String sobjectName;
    private boolean compareObjectFields;
    private boolean comparePageLayouts;
    private boolean compareValidationRules;
    private boolean compareFlows;
    private boolean compareApexTriggers;
    private boolean compareRecordTypes;
    private boolean compareCustomMetadata;
    private boolean compareCustomSettings;
    private boolean comparePermissionSets;
    private boolean compareProfiles;
    private boolean compareGroups;
    private boolean compareApprovalProcesses;
    private boolean compareNamedCredentials;
    private String permissionSetsFilter = "";
    private String profilesFilter = "";

    public String getSobjectName() { return sobjectName; }
    public void setSobjectName(String sobjectName) { this.sobjectName = sobjectName; }

    public boolean isCompareObjectFields() { return compareObjectFields; }
    public void setCompareObjectFields(boolean compareObjectFields) { this.compareObjectFields = compareObjectFields; }

    public boolean isComparePageLayouts() { return comparePageLayouts; }
    public void setComparePageLayouts(boolean comparePageLayouts) { this.comparePageLayouts = comparePageLayouts; }

    public boolean isCompareValidationRules() { return compareValidationRules; }
    public void setCompareValidationRules(boolean compareValidationRules) { this.compareValidationRules = compareValidationRules; }

    public boolean isCompareFlows() { return compareFlows; }
    public void setCompareFlows(boolean compareFlows) { this.compareFlows = compareFlows; }

    public boolean isCompareApexTriggers() { return compareApexTriggers; }
    public void setCompareApexTriggers(boolean compareApexTriggers) { this.compareApexTriggers = compareApexTriggers; }

    public boolean isCompareRecordTypes() { return compareRecordTypes; }
    public void setCompareRecordTypes(boolean compareRecordTypes) { this.compareRecordTypes = compareRecordTypes; }

    public boolean isCompareCustomMetadata() { return compareCustomMetadata; }
    public void setCompareCustomMetadata(boolean compareCustomMetadata) { this.compareCustomMetadata = compareCustomMetadata; }

    public boolean isCompareCustomSettings() { return compareCustomSettings; }
    public void setCompareCustomSettings(boolean compareCustomSettings) { this.compareCustomSettings = compareCustomSettings; }

    public boolean isComparePermissionSets() { return comparePermissionSets; }
    public void setComparePermissionSets(boolean comparePermissionSets) { this.comparePermissionSets = comparePermissionSets; }

    public boolean isCompareProfiles() { return compareProfiles; }
    public void setCompareProfiles(boolean compareProfiles) { this.compareProfiles = compareProfiles; }

    public boolean isCompareGroups() { return compareGroups; }
    public void setCompareGroups(boolean compareGroups) { this.compareGroups = compareGroups; }

    public boolean isCompareApprovalProcesses() { return compareApprovalProcesses; }
    public void setCompareApprovalProcesses(boolean compareApprovalProcesses) { this.compareApprovalProcesses = compareApprovalProcesses; }

    public boolean isCompareNamedCredentials() { return compareNamedCredentials; }
    public void setCompareNamedCredentials(boolean compareNamedCredentials) { this.compareNamedCredentials = compareNamedCredentials; }

    public String getPermissionSetsFilter() { return permissionSetsFilter != null ? permissionSetsFilter : ""; }
    public void setPermissionSetsFilter(String v) { this.permissionSetsFilter = v != null ? v : ""; }

    public String getProfilesFilter() { return profilesFilter != null ? profilesFilter : ""; }
    public void setProfilesFilter(String v) { this.profilesFilter = v != null ? v : ""; }
}
