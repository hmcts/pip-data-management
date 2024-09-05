package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.CaseInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.CaseSitting;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.DefendantInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.MagistratesStandardList;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.Offence;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.MagistratesStandardListHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class MagistratesStandardListHelperTest {
    private static final String COURT_ROOM1 = "Courtroom 1: Judge Test Name, Magistrate Test Name";
    private static final String COURT_ROOM2 = "Courtroom 2: Judge Test Name 2, Magistrate Test Name 2";

    private static final String COURT_ROOM_MESSAGE = "Court room and judiciary does not match";
    private static final String CASE_SITTING_MESSAGE = "Case sitting does not match";
    private static final String DEFENDANT_INFO_MESSAGE = "Defendant info does not match";
    private static final String CASE_INFO_MESSAGE = "Case info does not match";
    private static final String OFFENCE_MESSAGE = "Offence does not match";

    private static JsonNode inputJson;

    @BeforeAll
    static void setup()  throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(
            "src/test/resources/mocks/magistratesStandardList.json")), writer,
                     Charset.defaultCharset()
        );

        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    void testCourtRoomAndJudiciary() {
        Map<String, List<MagistratesStandardList>> result = MagistratesStandardListHelper.processRawListData(
            inputJson, Language.ENGLISH
        );

        assertThat(result)
            .as(COURT_ROOM_MESSAGE)
            .hasSize(2)
            .extracting(r -> r.keySet())
            .isEqualTo(Set.of(COURT_ROOM1, COURT_ROOM2));
    }

    @Test
    void testDefendantHeading() {
        Map<String, List<MagistratesStandardList>> result = MagistratesStandardListHelper.processRawListData(
            inputJson, Language.ENGLISH
        );

        assertThat(result.get(COURT_ROOM1))
            .hasSize(3)
            .extracting(MagistratesStandardList::getDefendantHeading)
            .containsExactly("Surname1, Forename1 (male)",
                             "Surname2, Forename2 (male)*",
                             "Surname3, Forename3 (male)*");

        assertThat(result.get(COURT_ROOM2))
            .hasSize(4)
            .extracting(MagistratesStandardList::getDefendantHeading)
            .containsExactly("Surname4, Forename4 (male)*",
                             "Surname5, Forename5 (male)*",
                             "Surname6, Forename6 (male)*",
                             "Surname5, Forename5");
    }

    @Test
    void testCaseSittings() {
        Map<String, List<MagistratesStandardList>> result = MagistratesStandardListHelper.processRawListData(
            inputJson, Language.ENGLISH
        );

        List<CaseSitting> caseSittings1 = result
            .get(COURT_ROOM1).get(0)
            .getCaseSittings();

        assertThat(caseSittings1)
            .as(CASE_SITTING_MESSAGE)
            .hasSize(1)
            .first()
            .extracting(CaseSitting::getSittingStartTime,
                        CaseSitting::getSittingDuration)
            .containsExactly("1:30pm",
                             "2 hours 30 mins");

        List<CaseSitting> caseSittings2 = result
            .get(COURT_ROOM2).get(0)
            .getCaseSittings();

        assertThat(caseSittings2)
            .as(CASE_SITTING_MESSAGE)
            .hasSize(2);
    }

    @Test
    void testDefendantInfo() {
        Map<String, List<MagistratesStandardList>> result = MagistratesStandardListHelper.processRawListData(
            inputJson, Language.ENGLISH
        );

        DefendantInfo defendantInfo = result
            .get(COURT_ROOM1).get(0)
            .getCaseSittings().get(0)
            .getDefendantInfo();

        assertThat(defendantInfo)
            .as(DEFENDANT_INFO_MESSAGE)
            .extracting(DefendantInfo::getDob,
                        DefendantInfo::getAge,
                        DefendantInfo::getAddress,
                        DefendantInfo::getPlea,
                        DefendantInfo::getPleaDate)
            .containsExactly("01/01/1983",
                             "39",
                             "Address Line 1, Address Line 2, Month A, County A, AA1 AA1",
                             "NOT_GUILTY",
                             "Need to confirm");
    }

    @Test
    void testCaseInfo() {
        Map<String, List<MagistratesStandardList>> result = MagistratesStandardListHelper.processRawListData(
            inputJson, Language.ENGLISH
        );

        CaseInfo caseInfo = result
            .get(COURT_ROOM1).get(0)
            .getCaseSittings().get(0)
            .getCaseInfo();

        assertThat(caseInfo)
            .as(CASE_INFO_MESSAGE)
            .extracting(CaseInfo::getProsecutingAuthorityCode,
                        CaseInfo::getHearingNumber,
                        CaseInfo::getAttendanceMethod,
                        CaseInfo::getCaseNumber,
                        CaseInfo::getCaseSequenceIndicator,
                        CaseInfo::getAsn,
                        CaseInfo::getHearingType,
                        CaseInfo::getPanel,
                        CaseInfo::getConvictionDate,
                        CaseInfo::getAdjournedDate)
            .containsExactly("Test1234",
                             "12",
                             "VIDEO HEARING",
                             "45684548",
                             "2 of 3",
                             "Need to confirm",
                             "mda",
                             "ADULT",
                             "13/12/2023",
                             "13/12/2023");
    }

    @Test
    void testOffences() {
        Map<String, List<MagistratesStandardList>> result = MagistratesStandardListHelper.processRawListData(
            inputJson, Language.ENGLISH
        );

        List<Offence> offences = result
            .get(COURT_ROOM1).get(0)
            .getCaseSittings().get(0)
            .getOffences();

        assertThat(offences)
            .as(OFFENCE_MESSAGE)
            .hasSize(2);

        assertThat(offences.get(0))
            .as(OFFENCE_MESSAGE)
            .extracting(Offence::getOffenceTitle,
                        Offence::getOffenceWording)
            .containsExactly("drink driving",
                             "driving whilst under the influence of alcohol");

        assertThat(offences.get(1))
            .as(OFFENCE_MESSAGE)
            .extracting(Offence::getOffenceTitle,
                        Offence::getOffenceWording)
            .containsExactly("Assault by beating",
                             "Assault by beating");
    }
}
