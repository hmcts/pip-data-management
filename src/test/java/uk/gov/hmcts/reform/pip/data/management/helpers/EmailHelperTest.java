package uk.gov.hmcts.reform.pip.data.management.helpers;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
class EmailHelperTest {
    @Test
    void testMaskEmail() {
        assertEquals("t*******@email.com",
                     EmailHelper.maskEmail("testUser@email.com"),
                     "Email was not masked correctly");
    }

    @Test
    void testMaskEmailNotValidEmail() {
        assertEquals("a****",
                     EmailHelper.maskEmail("abcde"),
                     "Email was not masked correctly");
    }

    @Test
    void testMaskEmailEmptyString() {
        assertEquals("",
                     EmailHelper.maskEmail(""),
                     "Email was not masked correctly");
    }
}
