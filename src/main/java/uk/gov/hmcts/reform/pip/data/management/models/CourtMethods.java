package uk.gov.hmcts.reform.pip.data.management.models;

public enum CourtMethods {

    NAME("getName"),
    JURISDICTION("getJurisdiction"),
    LOCATION("getLocation");

    public final String methodName;

    CourtMethods(String methodName) {
        this.methodName = methodName;
    }
}
