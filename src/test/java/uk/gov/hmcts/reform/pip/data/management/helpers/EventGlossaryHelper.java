package uk.gov.hmcts.reform.pip.data.management.helpers;

import uk.gov.hmcts.reform.pip.data.management.models.lcsu.EventGlossary;

import java.util.ArrayList;
import java.util.List;

public final class EventGlossaryHelper {

    private EventGlossaryHelper() {
    }

    public static EventGlossary createMockEvent(int eventId, String eventStatus, String eventName) {
        EventGlossary event = new EventGlossary();
        event.setEventId(eventId);
        event.setEventName(eventName);
        event.setEventStatus(eventStatus);

        return event;
    }

    public static List<EventGlossary> createMockEventList() {
        List<EventGlossary> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            EventGlossary event = createMockEvent(i,
                                          String.format("mock event status %o", i + 1),
                                          String.format("mock event name %o", i + 1));
            events.add(event);
        }
        return events;
    }
}
