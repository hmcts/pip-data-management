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
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_FIRM_PDDA_LIST;

@ActiveProfiles("test")
class CrownFirmPddaListCsvDataTest {
    CrownFirmPddaListCsvData csvData = new CrownFirmPddaListCsvData();

    @Test
    void testGetHeaders() throws IOException {
        Map<String, Object> languageResources = TestUtils.getLanguageResources(CROWN_FIRM_PDDA_LIST, "en");
        List<String> headers = csvData.getHeaders(languageResources);

        assertThat(headers)
            .as("Incorrect headers")
            .containsExactly(
                "Sitting Date",
                "Court House",
                "Court Address",
                "Court Phone Number",
                "Court Room",
                "Sitting at",
                "Hearing Time",
                "Case Number",
                "Defendant Name(s)",
                "Hearing Type",
                "Representative",
                "Prosecuting Authority",
                "Listing Notes"
            );
    }

    @Test
    void testGetRows() throws IOException {
        JsonNode json;
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/crownFirmPddaList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            json = new ObjectMapper().readTree(inputRaw);
        }
        Map<String, Object> languageResources = TestUtils.getLanguageResources(CROWN_FIRM_PDDA_LIST, "en");
        List<List<String>> rows = csvData.getRows(json, Map.of(), languageResources);

        assertThat(rows)
            .as("Incorrect number of rows")
            .hasSize(3);

        assertThat(rows.get(0))
            .as("Incorrect first row")
            .containsExactly(
                "Wednesday 10 September 2025",
                "TestCourtHouseName",
                "1 Main Road, London, A1 1AA",
                "02071234568",
                "Courtroom 1: TestJudgeRequested, Ms TestJusticeForename TestJusticeSurname Sr",
                "10am",
                "TestTimeMarkingNote",
                "T00112233",
                "TestMaskedName, Mr TestDefendantForename TestDefendantSurname TestDefendantSuffix",
                "TestHearingDescription",
                "TestSolicitorRequestedName",
                "Crown Prosecution Service",
                "TestListNote"
            );
    }
}
