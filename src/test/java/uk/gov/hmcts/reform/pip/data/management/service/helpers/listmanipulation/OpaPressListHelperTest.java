package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist.Offence;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist.OpaCaseInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist.OpaDefendantInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.opapresslist.OpaPressList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD.TooManyMethods")
class OpaPressListHelperTest {
    private static final String PLEA_DATE_MESSAGE = "Plea dates do not match";
    private static final String DEFENDANT_NAME_MESSAGE = "Defendant name does not match";
    private static final String DEFENDANT_INFO_MESSAGE = "Defendant info does not match";
    private static final String OFFENCE_INFO_MESSAGE = "Offence info does not match";
    private static final String CASE_INFO_MESSAGE = "Case info does not match";
    private static final String PROSECUTOR_MESSAGE = "Prosecutor does not match";
    private static final String TEST_ADDRESS_LINE = "Address Line 1, Address Line 2, Town, County";

    private JsonNode rawListJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(
            "/mocks/opaPressList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            rawListJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testOrderingOfPleaDates() {
        Set<String> expectedPleaDates = Set.of("22/09/2023", "21/09/2023", "20/09/2023");

        assertThat(OpaPressListHelper.processRawListData(rawListJson))
            .as(PLEA_DATE_MESSAGE)
            .hasSize(3)
            .extracting(r -> r.keySet())
            .isEqualTo(expectedPleaDates);
    }

    @Test
    void testDefendantForEachPleaDate() {
        List<List<OpaPressList>> values = OpaPressListHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(values.get(0))
            .as(DEFENDANT_NAME_MESSAGE)
            .hasSize(6)
            .extracting(d -> d.getDefendantInfo().getName())
            .containsExactly(
                Collections.nCopies(6, "Surname2, Forename2 MiddleName2").toArray(new String[0])
            );

        softly.assertThat(values.get(1))
            .as(DEFENDANT_NAME_MESSAGE)
            .hasSize(3)
            .extracting(d -> d.getDefendantInfo().getName())
            .containsExactly(Collections.nCopies(3, "Organisation name").toArray(new String[0]));

        softly.assertThat(values.get(2))
            .as(DEFENDANT_NAME_MESSAGE)
            .hasSize(2)
            .extracting(d -> d.getDefendantInfo().getName())
            .containsExactly(
                "Surname, Forename MiddleName",
                "Surname, Forename MiddleName"
            );

        softly.assertAll();
    }

    @Test
    void testCaseInfo() {
        OpaCaseInfo caseInfo = OpaPressListHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList()
            .get(0)
            .get(0)
            .getCaseInfo();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(caseInfo.getUrn())
            .as(CASE_INFO_MESSAGE)
            .isEqualTo("URN8888");

        softly.assertThat(caseInfo.getScheduledHearingDate())
            .as(CASE_INFO_MESSAGE)
            .isEqualTo("01/10/2023");

        softly.assertThat(caseInfo.getCaseReportingRestriction())
            .as(CASE_INFO_MESSAGE)
            .isEqualTo("Case reporting Restriction detail line 1, Case reporting restriction detail line 2");

        softly.assertAll();
    }

    @Test
    void testDefendantInfoUsingIndividualDetails() {
        OpaDefendantInfo defendantInfo = OpaPressListHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList()
            .get(2)
            .get(0)
            .getDefendantInfo();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(defendantInfo.getName())
            .as(DEFENDANT_NAME_MESSAGE)
            .isEqualTo("Surname, Forename MiddleName");

        softly.assertThat(defendantInfo.getDob())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo("01/01/1980");

        softly.assertThat(defendantInfo.getAge())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo("43");

        softly.assertThat(defendantInfo.getAddress())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo("Address Line 1, Address Line 2, Town, County, BB1 1BB");

        softly.assertThat(defendantInfo.getAddressWithoutPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo(TEST_ADDRESS_LINE);

        softly.assertThat(defendantInfo.getPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo("BB1 1BB");

        softly.assertAll();
    }

    @Test
    void testDefendantInfoUsingIndividualDetailsWhenNoAddress() {
        OpaDefendantInfo defendantInfo = OpaPressListHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList()
            .get(0)
            .get(1)
            .getDefendantInfo();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(defendantInfo.getAddress())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEmpty();

        softly.assertThat(defendantInfo.getAddressWithoutPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEmpty();

        softly.assertThat(defendantInfo.getPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testDefendantInfoUsingIndividualDetailsWhenNoPostcode() {
        OpaDefendantInfo defendantInfo = OpaPressListHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList()
            .get(0)
            .get(2)
            .getDefendantInfo();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(defendantInfo.getAddress())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo(TEST_ADDRESS_LINE);

        softly.assertThat(defendantInfo.getAddressWithoutPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo(TEST_ADDRESS_LINE);

        softly.assertThat(defendantInfo.getPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testDefendantInfoUsingOrganisationDetails() {
        OpaDefendantInfo defendantInfo = OpaPressListHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList()
            .get(1)
            .get(0)
            .getDefendantInfo();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(defendantInfo.getName())
            .as(DEFENDANT_NAME_MESSAGE)
            .isEqualTo("Organisation name");

        softly.assertThat(defendantInfo.getDob())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEmpty();

        softly.assertThat(defendantInfo.getAge())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEmpty();

        softly.assertThat(defendantInfo.getAddress())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo("Address Line 1, Address Line 2, Town, County, CC1 1CC");

        softly.assertThat(defendantInfo.getAddressWithoutPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo(TEST_ADDRESS_LINE);

        softly.assertThat(defendantInfo.getPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo("CC1 1CC");

        softly.assertAll();
    }

    @Test
    void testDefendantInfoUsingOrganisationDetailsWhenNoAddress() {
        OpaDefendantInfo defendantInfo = OpaPressListHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList()
            .get(1)
            .get(2)
            .getDefendantInfo();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(defendantInfo.getAddress())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEmpty();

        softly.assertThat(defendantInfo.getAddressWithoutPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEmpty();

        softly.assertThat(defendantInfo.getPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testDefendantInfoUsingOrganisationDetailsWhenNoPostcode() {
        OpaDefendantInfo defendantInfo = OpaPressListHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList()
            .get(1)
            .get(1)
            .getDefendantInfo();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(defendantInfo.getAddress())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo(TEST_ADDRESS_LINE);

        softly.assertThat(defendantInfo.getAddressWithoutPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEqualTo(TEST_ADDRESS_LINE);

        softly.assertThat(defendantInfo.getPostcode())
            .as(DEFENDANT_INFO_MESSAGE)
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testOffenceForDefendant() {
        Offence offence = OpaPressListHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList()
            .get(0)
            .get(0)
            .getDefendantInfo()
            .getOffences()
            .get(0);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(offence.getOffenceTitle())
            .as(OFFENCE_INFO_MESSAGE)
            .isEqualTo("Offence title 2");

        softly.assertThat(offence.getOffenceSection())
            .as(OFFENCE_INFO_MESSAGE)
            .isEqualTo("Offence section 2");

        softly.assertThat(offence.getOffenceWording())
            .as(OFFENCE_INFO_MESSAGE)
            .isEqualTo("Offence wording 2");

        softly.assertThat(offence.getPlea())
            .as(OFFENCE_INFO_MESSAGE)
            .isEqualTo("NOT_GUILTY");

        softly.assertThat(offence.getPleaDate())
            .as(OFFENCE_INFO_MESSAGE)
            .isEqualTo("22/09/2023");

        softly.assertThat(offence.getOffenceReportingRestriction())
            .as(OFFENCE_INFO_MESSAGE)
            .isEqualTo("Offence reporting restriction detail 1");

        softly.assertAll();
    }

    @Test
    void testProsecutorUsingInformant() {
        OpaDefendantInfo defendantInfo = OpaPressListHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList()
            .get(0)
            .get(0)
            .getDefendantInfo();

        assertThat(defendantInfo.getProsecutor())
            .as(PROSECUTOR_MESSAGE)
            .isEqualTo("Prosecuting authority ref");
    }

    @Test
    void testProsecutorUsingPartyProsecutingAuthorityRole() {
        OpaDefendantInfo defendantInfo = OpaPressListHelper.processRawListData(rawListJson)
            .values()
            .stream()
            .toList()
            .get(1)
            .get(0)
            .getDefendantInfo();

        assertThat(defendantInfo.getProsecutor())
            .as(PROSECUTOR_MESSAGE)
            .isEqualTo("Prosecuting authority name");
    }
}
