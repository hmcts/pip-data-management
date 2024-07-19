package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.oparesults.Offence;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.oparesults.OpaResults;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpaResultsHelperTest {
    private static final String DECISION_DATES_MESSAGE = "Decision dates do not match";
    private static final String DEFENDANT_NAME_MESSAGE = "Defendant name does not match";
    private static final String CASE_URN_MESSAGE = "Case URN does not match";
    private static final String OFFENCE_MESSAGE = "Offence does not match";

    private JsonNode rawListJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(
            "/mocks/opaResults.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            rawListJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testOrderingOfDecisionDates() {
        assertThat(OpaResultsHelper.processRawListData(rawListJson))
            .as(DECISION_DATES_MESSAGE)
            .hasSize(3)
            .extracting(r -> r.keySet())
            .isEqualTo(Set.of("07 January 2024", "06 January 2024", "05 January 2024"));
    }

    @Test
    void testDefendantForEachDecisionDate() {
        List<List<OpaResults>> values = OpaResultsHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(values.get(0))
            .as(DEFENDANT_NAME_MESSAGE)
            .hasSize(1)
            .extracting(OpaResults::getDefendant)
            .containsExactly("Organisation name");

        softly.assertThat(values.get(1))
            .as(DEFENDANT_NAME_MESSAGE)
            .hasSize(1)
            .extracting(OpaResults::getDefendant)
            .containsExactly("Surname 2, Forename 2 MiddleName 2");

        softly.assertThat(values.get(2))
            .as(DEFENDANT_NAME_MESSAGE)
            .hasSize(2)
            .extracting(OpaResults::getDefendant)
            .containsExactly("Surname, Forename MiddleName", "Surname 3, Forename 3 MiddleName 3");

        softly.assertAll();
    }

    @Test
    void testCaseUrnForEachDecisionDate() {
        List<List<OpaResults>> values = OpaResultsHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(values.get(0))
            .as(CASE_URN_MESSAGE)
            .hasSize(1)
            .extracting(OpaResults::getCaseUrn)
            .containsExactly("URN456");

        softly.assertThat(values.get(1))
            .as(CASE_URN_MESSAGE)
            .hasSize(1)
            .extracting(OpaResults::getCaseUrn)
            .containsExactly("URN456");

        softly.assertThat(values.get(2))
            .as(CASE_URN_MESSAGE)
            .hasSize(2)
            .extracting(OpaResults::getCaseUrn)
            .containsExactly("URN123", "URN789");

        softly.assertAll();
    }

    @Test
    void testCaseWithSingleOffence() {
        List<List<OpaResults>> values = OpaResultsHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList();

        List<Offence> offences = values.get(1).get(0)
            .getOffences();
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(offences)
            .as(OFFENCE_MESSAGE)
            .hasSize(1);

        softly.assertThat(offences.get(0).getOffenceTitle())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Offence title 3");

        softly.assertThat(offences.get(0).getOffenceSection())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Offence section 3");

        softly.assertThat(offences.get(0).getDecisionDate())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("06 January 2024");

        softly.assertThat(offences.get(0).getDecisionDetail())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Decision detail 3");

        softly.assertThat(offences.get(0).getBailStatus())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Unconditional bail");

        softly.assertThat(offences.get(0).getNextHearingDate())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("11 February 2024");

        softly.assertThat(offences.get(0).getNextHearingLocation())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Hearing location 3");

        softly.assertThat(offences.get(0).getReportingRestrictions())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Reporting restriction detail 4, Reporting restriction detail 5");

        softly.assertAll();
    }

    @Test
    void testCaseWithMultipleOffences() {
        List<List<OpaResults>> values = OpaResultsHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList();

        List<Offence> offences = values.get(0).get(0)
            .getOffences();
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(offences)
            .as(OFFENCE_MESSAGE)
            .hasSize(2);

        softly.assertThat(offences.get(0).getOffenceTitle())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Offence title 2A");

        softly.assertThat(offences.get(0).getOffenceSection())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Offence section 2A");

        softly.assertThat(offences.get(0).getDecisionDate())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("07 January 2024");

        softly.assertThat(offences.get(0).getDecisionDetail())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Decision detail 2A");

        softly.assertThat(offences.get(0).getBailStatus())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Unconditional bail");

        softly.assertThat(offences.get(0).getNextHearingDate())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("10 February 2024");

        softly.assertThat(offences.get(0).getNextHearingLocation())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Hearing location 2");

        softly.assertThat(offences.get(0).getReportingRestrictions())
            .as(OFFENCE_MESSAGE)
            .isEqualTo("Reporting restriction detail 2, Reporting restriction detail 3");

        softly.assertAll();
    }
}
