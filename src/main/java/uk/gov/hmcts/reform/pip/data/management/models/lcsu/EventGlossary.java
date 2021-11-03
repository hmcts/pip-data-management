package uk.gov.hmcts.reform.pip.data.management.models.lcsu;

import lombok.Data;

@Data
public class EventGlossary {
    private int eventId;
    private String eventStatus;
    private String eventName;
}
