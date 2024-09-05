package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist.CaseInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist.Defendant;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapubliclist.Offence;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.OpaPublicListHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpaPublicListHelperTest {
    private static final String DEFENDANT_NAME_MESSAGE = "Defendant name does not match";
    private static final String URN_MESSAGE = "URN does not match";
    private static final String OFFENCE_INFO_MESSAGE = "Offence info does not match";
    private static final String PROSECUTOR_MESSAGE = "Prosecutor does not match";
    private static final String HEARING_MESSAGE = "Scheduled hearing date does not match";
    private static final String RESTRICTION_MESSAGE = "Case reporting restriction does not match";

    private JsonNode rawListJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(
            "/mocks/opaPublicList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            rawListJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testCaseInfo() {
        CaseInfo caseInfo = OpaPublicListHelper.formatOpaPublicList(rawListJson)
            .stream()
            .toList()
            .get(0)
            .getCaseInfo();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(caseInfo.getUrn())
            .as(URN_MESSAGE)
            .isEqualTo("URN1234");

        softly.assertThat(caseInfo.getScheduledHearingDate())
            .as(HEARING_MESSAGE)
            .isEqualTo("14/09/16");

        softly.assertThat(caseInfo.getCaseReportingRestriction())
            .as(RESTRICTION_MESSAGE)
            .isEqualTo("Case Reporting Restriction detail line 1, Case Reporting restriction detail line 2");

        softly.assertAll();
    }

    @Test
    void testProsecutorUsingInformant() {
        Defendant defendant = OpaPublicListHelper.formatOpaPublicList(rawListJson)
            .stream()
            .toList()
            .get(0)
            .getDefendant();

        assertThat(defendant.getProsecutor())
            .as(PROSECUTOR_MESSAGE)
            .isEqualTo("Prosecution Authority ref 1");
    }

    @Test
    void testDefendantIndividual() {
        Defendant defendant = OpaPublicListHelper.formatOpaPublicList(rawListJson)
            .stream()
            .toList()
            .get(0)
            .getDefendant();

        assertThat(defendant.getName())
            .as(DEFENDANT_NAME_MESSAGE)
            .isEqualTo("individualFirstName individualMiddleName IndividualSurname");
    }

    @Test
    void testDefendantOrganisation() {
        Defendant defendant = OpaPublicListHelper.formatOpaPublicList(rawListJson)
            .stream()
            .toList()
            .get(6)
            .getDefendant();

        assertThat(defendant.getName())
            .as(DEFENDANT_NAME_MESSAGE)
            .isEqualTo("defendantOrganisationName");
    }

    @Test
    void testOffence() {
        Offence offence = OpaPublicListHelper.formatOpaPublicList(rawListJson)
            .stream()
            .toList()
            .get(0)
            .getDefendant()
            .getOffences()
            .get(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(offence.getOffenceTitle())
            .as(OFFENCE_INFO_MESSAGE)
            .isEqualTo("Offence title");

        softly.assertThat(offence.getOffenceSection())
            .as(OFFENCE_INFO_MESSAGE)
            .isEqualTo("Offence section");

        softly.assertThat(offence.getOffenceReportingRestriction())
            .as(OFFENCE_INFO_MESSAGE)
            .isEqualTo("Offence Reporting Restriction detail");

        softly.assertAll();
    }
}
