package uk.gov.hmcts.reform.pip.data.management.utils;

public enum CaseSearchTerm {

    CASE_URN("case-urn"),
    CASE_ID("case-id"),
    CASE_NAME("case-name");

    public final String dbValue;

    /**
     * Enum for case search terms to database values.
     * @param dbValue the value of the enum as it is stored in the database
     */
    CaseSearchTerm(String dbValue) {
        this.dbValue = dbValue;
    }
}
