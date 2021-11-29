package uk.gov.hmcts.reform.pip.data.management.helpers;

import uk.gov.hmcts.reform.pip.data.management.models.lcsu.CaseEventGlossary;

import java.util.ArrayList;
import java.util.List;

public final class EventGlossaryHelper {

    private EventGlossaryHelper() {
    }

    public static CaseEventGlossary createMockEvent(int id, String name, String description) {
        CaseEventGlossary event = new CaseEventGlossary();
        event.setId(id);
        event.setName(name);
        event.setDescription(description);

        return event;
    }

    public static List<CaseEventGlossary> createMockEventList() {
        List<CaseEventGlossary> caseEventGlossaries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CaseEventGlossary caseEventGlossary = createMockEvent(i,
                                                      String.format("mock case event name %o", i + 1),
                                                      String.format("mock case event description %o", i + 1));
            caseEventGlossaries.add(caseEventGlossary);
        }
        return caseEventGlossaries;
    }
}
