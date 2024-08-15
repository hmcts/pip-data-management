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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
class CrownDailyListHelperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";

    private static final String TIME = "time";
    private static final String LISTING_NOTES = "listingNotes";
    private static final String TIME_ERROR = "Unable to find correct case time";
    private static final String NO_BORDER_BOTTOM = "no-border-bottom";
    private static final String LINKED_CASES_BORDER = "linkedCasesBorder";
    private static final String REPORTING_RESTRICTIONS_DETAILS_BORDER = "reportingRestrictionDetailBorder";
    private static final String CASE_CELL_BORDER = "caseCellBorder";
    private static final String INCORRECT_BORDER_TEXT = "Incorrect border text";
    private static JsonNode inputJsonCrownDailyList;

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter crownDailyWriter = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/crownDailyList.json")),
                     crownDailyWriter, Charset.defaultCharset()
        );

        inputJsonCrownDailyList = OBJECT_MAPPER.readTree(crownDailyWriter.toString());
    }

    @Test
    void testFindUnallocatedCasesInCrownDailyListDataMethod() {
        assertEquals(4, inputJsonCrownDailyList.get(COURT_LISTS).size(),
                     "Unable to find correct court List array");
        CrownDailyListHelper.findUnallocatedCases(inputJsonCrownDailyList);
        assertEquals(5, inputJsonCrownDailyList.get(COURT_LISTS).size(),
                     "Unable to find correct court List array when unallocated cases are there");
        assertTrue(inputJsonCrownDailyList.get(COURT_LISTS).get(4).get("unallocatedCases").asBoolean(),
                   "Unable to find unallocated case section");
        assertFalse(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get("unallocatedCases").asBoolean(),
                    "Unable to find allocated case section");
        assertTrue(inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(1)
                       .get("exclude").asBoolean(),
                   "Unable to find unallocated courtroom");
        assertEquals("to be allocated", inputJsonCrownDailyList.get(COURT_LISTS).get(4).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get("courtRoomName").asText(),
                     "Unable to find unallocated courtroom");
    }

    @Test
    void testManipulatedCrownDailyListDataMethod() {
        CrownDailyListHelper.manipulatedCrownDailyListData(inputJsonCrownDailyList, Language.ENGLISH);

        assertEquals("10:40am", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM)
                         .get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(),
                     TIME_ERROR);

        assertEquals("1pm", inputJsonCrownDailyList.get(COURT_LISTS).get(2).get(COURT_HOUSE).get(COURT_ROOM)
                         .get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(),
                     TIME_ERROR);

        assertEquals("Surname 1, Forename 1", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("defendant").asText(),
                     "Unable to find information for defendant");

        assertEquals("Pro_Auth", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM)
                         .get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("prosecutingAuthority").asText(),
                     "Unable to find information for prosecution authority");

        assertEquals("caseid111, caseid222", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("linkedCases").asText(),
                     "Unable to find linked cases for a particular case");

        assertEquals("", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0).get(CASE).get(1)
                         .get("linkedCases").asText(),
                     "able to find linked cases for a particular case");

        assertEquals("This is a reporting restriction detail, This is another reporting restriction detail",
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0).get(CASE).get(0)
                         .get("combinedReportingRestriction").asText(),
                     "able to find reporting restriction detail for a case");

        assertEquals("Listing details text", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(LISTING_NOTES).asText(),
                     "Unable to find listing notes for a particular hearing");

        assertEquals("", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(1).get(LISTING_NOTES).asText(),
                     "Able to find listing notes for a particular hearing");

        assertEquals("no-border-bottom", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("caseCellBorder").asText(),
                     "Unable to find linked cases css for a particular case");

        assertEquals("no-border-bottom", inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("linkedCasesBorder").asText(),
                     "Unable to find linked cases css for a particular case");
    }

    @Test
    void testManipulatedCrownDailyListBordersAreCalculatedCorrectlyForACaseWithAllInfo() {
        CrownDailyListHelper.manipulatedCrownDailyListData(inputJsonCrownDailyList, Language.ENGLISH);

        assertEquals(NO_BORDER_BOTTOM,
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0).get(CASE).get(0)
                         .get(LINKED_CASES_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);

        assertEquals(NO_BORDER_BOTTOM,
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0).get(CASE).get(0)
                         .get(REPORTING_RESTRICTIONS_DETAILS_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);

        assertEquals(NO_BORDER_BOTTOM,
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0).get(CASE).get(0)
                         .get(CASE_CELL_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);
    }

    @Test
    void testManipulatedCrownDailyListBordersAreCalculatedCorrectlyForACaseWithNoInfo() {
        CrownDailyListHelper.manipulatedCrownDailyListData(inputJsonCrownDailyList, Language.ENGLISH);

        assertEquals("",
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(1).get(CASE).get(0)
                         .get(LINKED_CASES_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);

        assertEquals("",
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(1).get(CASE).get(0)
                         .get(REPORTING_RESTRICTIONS_DETAILS_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);

        assertEquals("",
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(1).get(CASE).get(0)
                         .get(CASE_CELL_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);
    }

    @Test
    void testManipulatedCrownDailyListBordersAreCalculatedCorrectlyForACaseWithPartialInfo() {
        CrownDailyListHelper.manipulatedCrownDailyListData(inputJsonCrownDailyList, Language.ENGLISH);

        assertEquals("",
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(1).get(HEARING).get(0).get(CASE).get(0)
                         .get(LINKED_CASES_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);

        assertEquals(NO_BORDER_BOTTOM,
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(1).get(HEARING).get(0).get(CASE).get(0)
                         .get(REPORTING_RESTRICTIONS_DETAILS_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);

        assertEquals(NO_BORDER_BOTTOM,
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(1).get(HEARING).get(0).get(CASE).get(0)
                         .get(CASE_CELL_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);
    }

    @Test
    void testManipulatedCrownDailyListBordersAreCalculatedCorrectlyForACaseWithOnlyReportingRestrictionDetail() {
        CrownDailyListHelper.manipulatedCrownDailyListData(inputJsonCrownDailyList, Language.ENGLISH);

        assertEquals("",
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(1).get(HEARING).get(1).get(CASE).get(0)
                         .get(LINKED_CASES_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);

        assertEquals("",
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(1).get(HEARING).get(1).get(CASE).get(0)
                         .get(REPORTING_RESTRICTIONS_DETAILS_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);

        assertEquals(NO_BORDER_BOTTOM,
                     inputJsonCrownDailyList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0)
                         .get(SESSION).get(0).get(SITTINGS).get(1).get(HEARING).get(1).get(CASE).get(0)
                         .get(CASE_CELL_BORDER).asText(),
                     INCORRECT_BORDER_TEXT);
    }
}
