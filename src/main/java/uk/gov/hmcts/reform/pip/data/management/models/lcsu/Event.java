package uk.gov.hmcts.reform.pip.data.management.models.lcsu;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.Data;

import java.time.LocalTime;

@Data
public class Event {

    private int eventId;
    private String eventStatus;
    private String eventName;

    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime eventTime;
}
