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

@ActiveProfiles("test")
class MagistratesPublicListHelperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";
    private static final String FORMATTED_SESSION_COURT_ROOM = "formattedSessionCourtRoom";
    private static final String TIME = "time";
    private static final String LISTING_NOTES = "listingNotes";
    private static final String TIME_ERROR = "Unable to find correct case time";
    private static JsonNode inputJsonMagistratesPublicList;

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter magistratesPublicWriter = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/magistratesPublicList.json")),
                     magistratesPublicWriter, Charset.defaultCharset()
        );

        inputJsonMagistratesPublicList = OBJECT_MAPPER.readTree(magistratesPublicWriter.toString());
    }

    @Test
    void testManipulatedMagistratesPublicListDataMethod() {
        MagistratesPublicListHelper.manipulatedMagistratesPublicListData(inputJsonMagistratesPublicList,
                                                                         Language.ENGLISH);

        assertEquals("10:40am", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(),
                     TIME_ERROR);
        assertEquals("1pm", inputJsonMagistratesPublicList.get(COURT_LISTS).get(2).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(TIME).asText(),
                     TIME_ERROR);
        assertEquals("Surname 1, Forename 1", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0)
                         .get(COURT_HOUSE).get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0)
                         .get(HEARING).get(0).get(CASE).get(0).get("defendant").asText(),
                     "Unable to find information for defendant");
        assertEquals("Pro_Auth", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(0)
                         .get(CASE).get(0).get("prosecutingAuthority").asText(),
                     "Unable to find information for prosecution authority");
        assertEquals("Listing details text", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0)
                         .get(COURT_HOUSE).get(COURT_ROOM).get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING)
                         .get(0).get(LISTING_NOTES).asText(),
                     "Unable to find listing notes for a particular hearing");
        assertEquals("", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM)
                         .get(0).get(SESSION).get(0).get(SITTINGS).get(0).get(HEARING).get(1).get(LISTING_NOTES)
                         .asText(),
                     "Able to find listing notes for a particular hearing");

    }

    @Test
    void testFormattedCourtRoomNameMethodMagistratesPublicList() {
        MagistratesPublicListHelper.manipulatedMagistratesPublicListData(inputJsonMagistratesPublicList,
                                                                         Language.ENGLISH);

        assertEquals("1: Judge KnownAs, Judge KnownAs 2", inputJsonMagistratesPublicList
                         .get(COURT_LISTS).get(0).get(COURT_HOUSE).get(COURT_ROOM).get(0).get(SESSION).get(0)
                         .get(FORMATTED_SESSION_COURT_ROOM).asText(),
                     "Unable to find formatted courtroom name");

        assertEquals("to be allocated", inputJsonMagistratesPublicList.get(COURT_LISTS).get(0).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(1).get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(),
                     "Unable to find unallocated formatted courtroom name");

        assertEquals("CourtRoom 1", inputJsonMagistratesPublicList.get(COURT_LISTS).get(1).get(COURT_HOUSE)
                         .get(COURT_ROOM).get(0).get(SESSION).get(0).get(FORMATTED_SESSION_COURT_ROOM).asText(),
                     "Unable to find formatted courtroom name without judge");
    }
}
