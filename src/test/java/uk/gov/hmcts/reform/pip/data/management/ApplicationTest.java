package uk.gov.hmcts.reform.pip.data.management;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationTest {

    @Test
    void testSetTimezone() {
        new Application().setupTimezone();
        assertEquals(TimeZone.getDefault(), TimeZone.getTimeZone(ZoneId.of("Europe/London")),
                     "Timezone does match expected timezone");
    }

}
