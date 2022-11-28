package uk.gov.hmcts.reform.pip.data.management.models.admin;

import lombok.Data;

@Data
public class Action {
    private ChangeType changeType;
    private ActionResult actionResult;
    private String additionalDetails;

    public Action(ChangeType changeType, ActionResult actionResult, String additionalDetails) {
        this.changeType = changeType;
        this.actionResult = actionResult;
        this.additionalDetails = additionalDetails;
    }
}
