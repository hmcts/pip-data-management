package uk.gov.hmcts.reform.pip.data.management.utils;

public enum CaseSearchTerm {

    CASE_URN("case-urn"),
    CASE_ID("case-id");

    public final String dbValue;

    /**
     * Enum for case search terms to database values.
     * @param dbValue
     */
    CaseSearchTerm(String dbValue) {
        this.dbValue = dbValue;
    }
}
