package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.CourtRoom;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.Hearing;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.HearingMetadata;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.Offence;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.PartyInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesstandardlist.Sitting;

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
class MagistratesStandardListHelperTest {
    private static final String COURT_ROOM1 = "Courtroom 1: Test Name, Test NamePRESTON";
    private static final String COURT_ROOM2 = "Courtroom 2: PRESTON";

    private static final String COURT_ROOM_MESSAGE = "Court room and judiciary does not match";
    private static final String HEARING_MESSAGE = "Hearing does not match";
    private static final String SUBJECT_PARTY_MESSAGE = "Sitting info does not match";
    private static final String HEARING_INFO_MESSAGE = "Hearing info does not match";
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
    void testCourtRoomAndJudiciaryKey() {
        Map<String, CourtRoom> result = MagistratesStandardListHelper.processRawListData(inputJson);

        assertThat(result)
            .as(COURT_ROOM_MESSAGE)
            .hasSize(2)
            .extracting(r -> r.keySet())
            .isEqualTo(Set.of(COURT_ROOM1, COURT_ROOM2));
    }

    @Test
    void testSittingHeading() {
        Map<String, CourtRoom> result = MagistratesStandardListHelper.processRawListData(inputJson);

        assertThat(result.get(COURT_ROOM1).getSittings())
            .hasSize(2)
            .extracting(Sitting::getSittingHeading)
            .containsExactly("1:30pm [2 of 3]",
                             "4:30pm [2 of 3]");

        assertThat(result.get(COURT_ROOM2).getSittings())
            .hasSize(3)
            .extracting(Sitting::getSittingHeading)
            .containsExactly("1:30pm [2 of 3]",
                             "1:30pm",
                             "2:30pm");
    }

    @Test
    void testCourtRoomObject() {
        Map<String, CourtRoom> result = MagistratesStandardListHelper.processRawListData(inputJson);

        CourtRoom courtRoom = result.get(COURT_ROOM1);

        assertThat(courtRoom)
            .as(COURT_ROOM_MESSAGE)
            .extracting(CourtRoom::getCourtRoomName,
                        CourtRoom::getCourtHouseName,
                        CourtRoom::getLja)
            .containsExactly(
                "Courtroom 1: Test Name, Test Name",
                "PRESTON",
                "Local Justice Area A");

        CourtRoom courtRoom2 = result.get(COURT_ROOM2);
        assertThat(courtRoom2)
            .as(COURT_ROOM_MESSAGE)
            .extracting(CourtRoom::getCourtRoomName,
                        CourtRoom::getCourtHouseName,
                        CourtRoom::getLja)
            .containsExactly(
                "Courtroom 2: ",
                "PRESTON",
                "Local Justice Area A");
    }

    @Test
    void testHearingObject() {
        Map<String, CourtRoom> result = MagistratesStandardListHelper.processRawListData(inputJson);

        List<Hearing> hearings = result
            .get(COURT_ROOM1).getSittings().get(0)
            .getHearings();

        assertThat(hearings)
            .as(HEARING_MESSAGE)
            .hasSize(1)
            .extracting(Hearing::getSittingStartTime)
            .containsExactly("1:30pm");

        List<Hearing> sittings2 = result
            .get(COURT_ROOM2).getSittings().get(1)
            .getHearings();

        assertThat(sittings2)
            .as(HEARING_MESSAGE)
            .hasSize(3)
            .extracting(Hearing::getSittingStartTime)
            .containsExactly("1:30pm", "1:30pm", "1:30pm");
    }

    @Test
    void testPartyInfo() {
        Map<String, CourtRoom> result = MagistratesStandardListHelper.processRawListData(inputJson);

        PartyInfo partyInfo = result
            .get(COURT_ROOM1).getSittings().get(0)
            .getHearings().get(0)
            .getPartyInfo();

        assertThat(partyInfo)
            .as(SUBJECT_PARTY_MESSAGE)
            .extracting(
                PartyInfo::getNameDetails,
                PartyInfo::getName,
                PartyInfo::getDob,
                PartyInfo::getAge,
                PartyInfo::getAddress,
                PartyInfo::getAsn)
            .containsExactly("Surname A, Forename A MiddleName A (male)",
                             "Surname A, Forename A MiddleName A",
                             "01/01/1950",
                             "20",
                             "Address Line 1A, Address Line 2A, Town A, County A, AA1 AA1",
                             "ABC1234");
    }

    @Test
    void testPartyInfoWhenOrganisation() {
        Map<String, CourtRoom> result = MagistratesStandardListHelper.processRawListData(inputJson);

        PartyInfo partyInfo = result
            .get(COURT_ROOM2).getSittings().get(1)
            .getHearings().get(2)
            .getPartyInfo();

        assertThat(partyInfo)
            .as(SUBJECT_PARTY_MESSAGE)
            .extracting(
                PartyInfo::getName,
                PartyInfo::getAddress)
            .containsExactly(
                "This is an organisation",
                "Address Line 1E, Address Line 2E, Town E, This is a postcode");
    }

    @Test
    void testHearingInfo() {
        Map<String, CourtRoom> result = MagistratesStandardListHelper.processRawListData(inputJson);

        HearingMetadata hearingMetadata = result
            .get(COURT_ROOM1).getSittings().get(0)
            .getHearings().get(0)
            .getHearingMetadata();

        assertThat(hearingMetadata)
            .as(HEARING_INFO_MESSAGE)
            .extracting(
                HearingMetadata::getProsecutingAuthority,
                HearingMetadata::getAttendanceMethod,
                HearingMetadata::getReference,
                HearingMetadata::getReportingRestrictionDetails,
                HearingMetadata::getCaseSequenceIndicator,
                HearingMetadata::getHearingType,
                HearingMetadata::getPanel)
            .containsExactly("Prosecuting Authority Name",
                             "VIDEO HEARING A",
                             "45684548",
                             "This is a case level reporting restriction details example",
                             "2 of 3",
                             "Hearing Type A",
                             "ADULT");
    }

    @Test
    void testHearingInfoWhenApplication() {
        Map<String, CourtRoom> result = MagistratesStandardListHelper.processRawListData(inputJson);

        HearingMetadata hearingMetadata = result
            .get(COURT_ROOM2).getSittings().get(1)
            .getHearings().get(1)
            .getHearingMetadata();

        assertThat(hearingMetadata)
            .as(HEARING_INFO_MESSAGE)
            .extracting(
                HearingMetadata::getReference,
                HearingMetadata::getApplicationType,
                HearingMetadata::getApplicationParticulars)
            .containsExactly("AppRefA",
                             "Application Type 1",
                             "This is an application particulars example");
    }

    @Test
    void testOffences() {
        Map<String, CourtRoom> result = MagistratesStandardListHelper.processRawListData(inputJson);

        List<Offence> offences = result
            .get(COURT_ROOM1).getSittings().get(0)
            .getHearings().get(0)
            .getOffences();

        assertThat(offences)
            .as(OFFENCE_MESSAGE)
            .hasSize(2);

        assertThat(offences.get(0))
            .as(OFFENCE_MESSAGE)
            .extracting(Offence::getOffenceTitle,
                        Offence::getOffenceWording,
                        Offence::getOffenceCode,
                        Offence::getOffenceLegislation,
                        Offence::getOffenceMaxPenalty,
                        Offence::getAdjournedDate,
                        Offence::getConvictionDate,
                        Offence::getPleaDate,
                        Offence::getPlea,
                        Offence::getReportingRestrictionDetails)
            .containsExactly("drink driving",
                             "driving whilst under the influence of alcohol",
                             "dd01-01",
                             "This is a legislation",
                             "100yrs",
                             "02/05/2026",
                             "01/05/2026",
                             "27/06/2026",
                             "NOT_GUILTY",
                             "This is an offence level reporting restriction details example");
    }
}
