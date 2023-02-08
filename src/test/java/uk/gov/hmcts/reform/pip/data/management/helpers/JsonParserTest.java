package uk.gov.hmcts.reform.pip.data.management.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
class JsonParserTest {

    @Test
    void testReadAttribute() throws JsonProcessingException {
        assertThat(JsonParser.readAttribute("{\"displayName\": \"ReqName\"}","displayName"))
            .as("Helper method doesn't seem to be working correctly")
            .isEqualTo("ReqName");
    }

    @Test
    void testReadAttributeNotFound() throws JsonProcessingException {
        assertThat(JsonParser.readAttribute("{\"displayName\": \"ReqName\"}","test"))
            .as("Helper method doesn't seem to be working correctly")
            .isEqualTo("");
    }

    @Test
    void testReadAttributeException() {
        assertThrows(JsonProcessingException.class, () ->
                         JsonParser.readAttribute("test", "attribute"),
                     "JsonProcessingException not thrown when trying to get errored system admin"
                         + " api response");
    }
}
