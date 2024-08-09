package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.pip.data.management.service.filegeneration.EtFortnightlyPressListFileConverter.preprocessArtefactForThymeLeafConverter;

@ActiveProfiles("test")
class EtFortnightlyPressListHelperTest {
    private static JsonNode inputJson;
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";
    private static final String PROVENANCE = "provenance";

    Map<String, Object> languageResources = Map.of("rep", "Rep: ",
                                          "noRep", "Rep: ",
                                          "legalAdvisor", "Legal Advisor: ");

    Map<String, String> metadataMap = Map.of("contentDate", Instant.now().toString(),
                                             PROVENANCE, PROVENANCE,
                                             "locationName", "location",
                                             "language", "ENGLISH",
                                             "listType", "ET_FORTNIGHTLY_PRESS_LIST"
    );

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths
            .get("src/test/resources/mocks/etFortnightlyPressList.json")), writer,
                     Charset.defaultCharset()
        );

        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    void testEtFortnightlyListFormattedMethod() {
        preprocessArtefactForThymeLeafConverter(inputJson, metadataMap, languageResources);
        Language language = Language.valueOf(metadataMap.get("language"));
        EtFortnightlyPressListHelper.manipulatedListData(inputJson, language, true);
        EtFortnightlyPressListHelper.etFortnightlyListFormatted(inputJson, languageResources);

        JsonNode sitting = inputJson.get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0).get(SESSION).get(0)
            .get(SITTINGS).get(0);

        assertEquals(2, inputJson.get(COURT_LISTS).size(),
                     "Unable to find correct court List array");

        assertEquals("Sunday 13 February 2022", sitting.get("sittingDate").asText(),
                    "Unable to find sitting date");

        assertEquals("9:30am", sitting.get("time").asText(),
                     "Unable to find time");

        JsonNode hearing = sitting.get(HEARING).get(0);

        assertEquals("2 hours", hearing.get("formattedDuration").asText(),
                     "Unable to find duration");

        assertEquals("12341234", hearing.get(CASE).get(0).get("caseNumber").asText(),
                     "Unable to find case number");

        assertEquals("", hearing.get(CASE).get(0).get("claimant").asText(),
                     "Unable to find claimant");

        assertEquals("Rep: Mr T Test Surname 2", hearing.get(CASE).get(0).get("claimantRepresentative").asText(),
                     "Unable to find claimant representative");

        assertEquals("Capt. T Test Surname", hearing.get(CASE).get(0).get("respondent").asText(),
                     "Unable to find respondent");

        assertEquals("Rep: Dr T Test Surname 2", hearing.get(CASE).get(0).get("respondentRepresentative").asText(),
                     "Unable to find respondent representative");

        assertEquals("This is a hearing type", hearing.get("hearingType").asText(),
                     "Unable to find hearing type");

        assertEquals("This is a sitting channel", hearing.get("caseHearingChannel").asText(),
                     "Unable to find Hearing Platform");
    }

    @Test
    void testSplitByCourtAndDateMethod() {
        preprocessArtefactForThymeLeafConverter(inputJson, metadataMap, languageResources);
        Language language = Language.valueOf(metadataMap.get("language"));
        EtFortnightlyPressListHelper.manipulatedListData(inputJson, language, true);
        EtFortnightlyPressListHelper.etFortnightlyListFormatted(inputJson,languageResources);
        EtFortnightlyPressListHelper.splitByCourtAndDate(inputJson);

        JsonNode sitting = inputJson.get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0).get(SESSION).get(0)
            .get(SITTINGS).get(0);

        assertEquals(2, inputJson.get(COURT_LISTS).size(),
                     "Unable to find correct court List array");

        assertEquals("Sunday 13 February 2022", sitting.get("sittingDate").asText(),
                     "Unable to find sitting date");

        assertEquals("9:30am", sitting.get("time").asText(),
                     "Unable to find time");

        JsonNode hearing = sitting.get(HEARING).get(0);

        assertEquals("2 hours", hearing.get("formattedDuration").asText(),
                     "Unable to find duration");

        assertEquals("12341234", hearing.get(CASE).get(0).get("caseNumber").asText(),
                     "Unable to find case number");

        assertEquals("", hearing.get(CASE).get(0).get("claimant").asText(),
                     "Unable to find claimant");

        assertEquals("Rep: Mr T Test Surname 2", hearing.get(CASE).get(0).get("claimantRepresentative").asText(),
                     "Unable to find claimant representative");

        assertEquals("Capt. T Test Surname", hearing.get(CASE).get(0).get("respondent").asText(),
                     "Unable to find respondent");

        assertEquals("Rep: Dr T Test Surname 2", hearing.get(CASE).get(0).get("respondentRepresentative").asText(),
                     "Unable to find respondent representative");

        assertEquals("This is a hearing type", hearing.get("hearingType").asText(),
                     "Unable to find hearing type");

        assertEquals("This is a sitting channel", hearing.get("caseHearingChannel").asText(),
                     "Unable to find Hearing Platform");
    }
}
