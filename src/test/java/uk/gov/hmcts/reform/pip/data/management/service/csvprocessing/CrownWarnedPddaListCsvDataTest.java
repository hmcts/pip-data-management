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
import static uk.gov.hmcts.reform.pip.model.publication.ListType.CROWN_WARNED_PDDA_LIST;

@ActiveProfiles("test")
class CrownWarnedPddaListCsvDataTest {
    CrownWarnedPddaListCsvData csvData = new CrownWarnedPddaListCsvData();

    @Test
    void testGetHeaders() throws IOException {
        Map<String, Object> languageResources = TestUtils.getLanguageResources(CROWN_WARNED_PDDA_LIST, "en");
        List<String> headers = csvData.getHeaders(languageResources);

        assertThat(headers)
            .as("Incorrect headers")
            .containsExactly(
                "Hearing Description",
                "Fixed For",
                "Case Reference",
                "Defendant Name(s)",
                "Prosecuting Authority",
                "Linked Cases",
                "Listing Notes"
            );
    }

    @Test
    void testGetRows() throws IOException {
        JsonNode json;
        try (InputStream inputStream = getClass().getResourceAsStream("/mocks/crownWarnedPddaList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            json = new ObjectMapper().readTree(inputRaw);
        }
        Map<String, Object> languageResources = TestUtils.getLanguageResources(CROWN_WARNED_PDDA_LIST, "en");
        List<List<String>> rows = csvData.getRows(json, Map.of(), languageResources);

        assertThat(rows)
            .as("Incorrect number of rows")
            .hasSize(2);

        assertThat(rows.get(0))
            .as("Incorrect first row")
            .containsExactly(
                "TestHearingDescription",
                "01/01/2024",
                "T00112233",
                "TestDefendantRequestedName",
                "Crown Prosecution Service",
                "TestLinkedCaseNumber",
                "TestListNote"
            );

        assertThat(rows.get(1))
            .as("Incorrect first row")
            .containsExactly(
                "To be allocated",
                "03/01/2024",
                "T00112244",
                "TestMaskedName2",
                "Other Prosecutor",
                "TestLinkedCaseNumber2",
                "TestListNote2"
            );
    }
}
