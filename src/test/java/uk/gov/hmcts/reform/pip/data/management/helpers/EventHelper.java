package uk.gov.hmcts.reform.pip.data.management.helpers;

import uk.gov.hmcts.reform.pip.data.management.models.lcsu.Event;

import java.util.ArrayList;
import java.util.List;

public final class EventHelper {
    public EventHelper(){
    }

    public static Event createMockEvent(int eventId, String eventStatus, String eventName) {
        Event event = new Event();
        event.setEventId(eventId);
        event.setEventName(eventName);
        event.setEventStatus(eventStatus);

        return event;
    }

    public static List<Event> createMockEventList() {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Event event = createMockEvent(i,
                                          String.format("mock event status %o", i + 1),
                                          String.format("mock event name %o", i + 1));
            events.add(event);
        }
        return events;
    }
}
