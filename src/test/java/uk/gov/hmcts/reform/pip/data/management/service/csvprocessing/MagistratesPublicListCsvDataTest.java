package uk.gov.hmcts.reform.pip.data.management.service.csvprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.TestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAGISTRATES_PUBLIC_LIST;

@ActiveProfiles("test")
class MagistratesPublicListCsvDataTest {
    private final MagistratesPublicListCsvData csvData = new MagistratesPublicListCsvData();

    @Test
    void testGetHeaders() throws IOException {
        Map<String, Object> languageResources = TestUtils.getLanguageResources(MAGISTRATES_PUBLIC_LIST, "en");
        List<String> headers = csvData.getHeaders(languageResources);

        assertThat(headers)
            .as("Incorrect headers")
            .containsExactly(
                "Court House",
                "Court Room",
                "Sitting at",
                "URN",
                "Name",
                "Hearing Type",
                "Prosecuting Authority",
                "Offence Details",
                "Reporting Restrictions"
            );
    }

    @Test
    void testGetRows() throws IOException {
        JsonNode json;
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/magistratesPublicList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            json = new ObjectMapper().readTree(inputRaw);
        }
        Map<String, Object> languageResources = TestUtils.getLanguageResources(MAGISTRATES_PUBLIC_LIST, "en");
        List<List<String>> rows = csvData.getRows(json, languageResources);

        assertThat(rows)
            .as("Incorrect number of rows")
            .hasSize(14);

        assertThat(rows.get(0))
            .as("Incorrect first row")
            .containsExactly(
                "",
                "CourtRoom 1: Judge KnownAs, Judge KnownAs 2",
                "10:40am",
                "12345678",
                "Surname 2, Forename 2",
                "FHDRA1 (First Hearing and Dispute Resolution Appointment)",
                "Pro_Auth",
                "Test offence 1",
                "Press/Publication restrictions apply to this case"
            );
    }
}
