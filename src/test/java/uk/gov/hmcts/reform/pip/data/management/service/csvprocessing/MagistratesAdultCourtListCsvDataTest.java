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
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAGISTRATES_ADULT_COURT_LIST_DAILY;

@ActiveProfiles("test")
class MagistratesAdultCourtListCsvDataTest {
    MagistratesAdultCourtListCsvData csvData = new MagistratesAdultCourtListCsvData();

    @Test
    void testGetHeaders() throws IOException {
        Map<String, Object> languageResources = TestUtils.getLanguageResources(
            MAGISTRATES_ADULT_COURT_LIST_DAILY, "en"
        );
        List<String> headers = csvData.getHeaders(languageResources);

        assertThat(headers)
            .as("Incorrect headers")
            .containsExactly(
                "Court House",
                "Sitting at",
                "LJA",
                "Session start",
                "Block Start",
                "Defendant Name",
                "Date of Birth",
                "Address",
                "Age",
                "Informant",
                "Case Number",
                "Offence Code",
                "Offence Title",
                "Offence Summary"
            );
    }

    @Test
    void testGetRows() throws IOException {
        JsonNode json;
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/magistratesAdultCourtList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            json = new ObjectMapper().readTree(inputRaw);
        }
        Map<String, Object> languageResources = TestUtils.getLanguageResources(
            MAGISTRATES_ADULT_COURT_LIST_DAILY, "en"
        );
        List<List<String>> rows = csvData.getRows(json, Map.of("language", "ENGLISH"), languageResources);

        assertThat(rows)
            .as("Incorrect number of rows")
            .hasSize(6);

        assertThat(rows.get(0))
            .as("Incorrect first row")
            .containsExactly(
                "North Shields Magistrates' Court",
                "Courtroom 1",
                "North Northumbria Magistrates' Court",
                "9am",
                "9am",
                "Mr Test User",
                "06/11/1975",
                "1 High Street, London, SW1A 1AA",
                "50",
                "POL01",
                "1000000000",
                "TH68001",
                "Offence title 1",
                "Offence summary 1"
            );
    }
}
