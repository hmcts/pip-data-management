package uk.gov.hmcts.reform.pip.data.management.models.admin;

public enum ChangeType {
    DELETE_COURT("Delete Court");

    public final String label;

    ChangeType(String label) {
        this.label = label;
    }
}
