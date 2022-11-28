package uk.gov.hmcts.reform.pip.data.management.models.admin;

public enum ActionResult {
    ATTEMPTED("Attempted"),
    SUCCEEDED("Succeeded"),
    FAILED("Failed");

    public final String label;

    ActionResult(String label) {
        this.label = label;
    }
}
