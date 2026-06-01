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
import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAGISTRATES_STANDARD_LIST;

@ActiveProfiles("test")
class MagistratesStandardListCsvDataTest {
    private final MagistratesStandardListCsvData csvData = new MagistratesStandardListCsvData();

    @Test
    void testGetHeaders() throws IOException {
        Map<String, Object> languageResources = TestUtils.getLanguageResources(MAGISTRATES_STANDARD_LIST, "en");
        List<String> headers = csvData.getHeaders(languageResources);

        assertThat(headers)
            .as("Incorrect headers")
            .containsExactly(
                "Court House",
                "LJA",
                "Court Room",
                "Sitting at",
                "Name",
                "Application Particulars",
                "DOB",
                "Age",
                "Address",
                "Prosecuting Authority Name",
                "Attendance Method",
                "Reference",
                "Application Type",
                "ASN",
                "Hearing Type",
                "Panel",
                "Reporting Restrictions",
                "Offence Code",
                "Offence Title",
                "Offence Details",
                "Legislation",
                "Max Penalty",
                "Plea",
                "Date of Plea",
                "Convicted on",
                "Adjourned from"
            );
    }

    @Test
    void testGetRows() throws IOException {
        JsonNode json;
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/magistratesStandardList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            json = new ObjectMapper().readTree(inputRaw);
        }
        Map<String, Object> languageResources = TestUtils.getLanguageResources(MAGISTRATES_STANDARD_LIST, "en");
        List<List<String>> rows = csvData.getRows(json, languageResources);

        assertThat(rows)
            .as("Incorrect number of rows")
            .hasSize(14);

        assertThat(rows.get(0))
            .as("Incorrect first row")
            .containsExactly(
                "PRESTON",
                "Local Justice Area A",
                "Courtroom 1: Test Name, Test Name",
                "1:30pm [2 of 3]",
                "Surname A, Forename A MiddleName A (male)",
                "",
                "01/01/1950",
                "20",
                "Address Line 1A, Address Line 2A, Town A, County A, AA1 AA1",
                "Prosecuting Authority Name",
                "VIDEO HEARING A",
                "45684548",
                "",
                "ABC1234",
                "Hearing Type A",
                "ADULT",
                "This is a case level reporting restriction details example",
                "dd01-01",
                "drink driving",
                "driving whilst under the influence of alcohol",
                "This is a legislation",
                "100yrs",
                "NOT_GUILTY",
                "27/06/2026",
                "01/05/2026",
                "02/05/2026 - For the trial"
            );
    }
}
