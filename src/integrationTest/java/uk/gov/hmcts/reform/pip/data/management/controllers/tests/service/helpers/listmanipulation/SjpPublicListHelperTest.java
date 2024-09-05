package uk.gov.hmcts.reform.pip.data.management.controllers.tests.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.SjpPublicList;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation.SjpPublicListHelper;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class SjpPublicListHelperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String JSON_PATH = "src/test/resources/mocks/sjpPublicList.json";

    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String PARTY = "party";
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String INDIVIDUAL_FORENAMES = "individualForenames";
    private static final String INDIVIDUAL_SURNAME = "individualSurname";

    @Test
    void testSjpCaseIsGeneratedWhenAccusedHasIndividualDetails() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(JSON_PATH)), writer, Charset.defaultCharset());

        JsonNode hearingNode = OBJECT_MAPPER.readTree(writer.toString())
            .get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0)
            .get(HEARING).get(0);

        SjpPublicList expectedSjpCase = new SjpPublicList(
            "A This is a surname",
            "AA",
            "This is an offence title, This is an offence title 2",
            "This is a prosecutor organisation"
        );

        assertThat(SjpPublicListHelper.constructSjpCase(hearingNode))
            .isPresent()
            .hasValue(expectedSjpCase);
    }

    @Test
    void testSjpCaseIsGeneratedWhenAccusedHasOrganisationDetails() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(JSON_PATH)), writer, Charset.defaultCharset());

        JsonNode hearingNode = OBJECT_MAPPER.readTree(writer.toString())
            .get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(1)
            .get(SITTINGS).get(0)
            .get(HEARING).get(0);

        SjpPublicList expectedSjpCase = new SjpPublicList(
            "This is an accused organisation name",
            "A9",
            "This is an offence title 3",
            "This is a prosecutor organisation 2"
        );

        assertThat(SjpPublicListHelper.constructSjpCase(hearingNode))
            .isPresent()
            .hasValue(expectedSjpCase);
    }

    @Test
    void testSjpCaseIsGeneratedWhenAccusedHasSurnameOnly() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(JSON_PATH)), writer, Charset.defaultCharset());

        JsonNode hearingNode = OBJECT_MAPPER.readTree(writer.toString())
            .get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0)
            .get(HEARING).get(0);

        ((ObjectNode) hearingNode.get(PARTY).get(0)
            .get(INDIVIDUAL_DETAILS))
            .remove(INDIVIDUAL_FORENAMES);

        SjpPublicList expectedSjpCase = new SjpPublicList(
            "This is a surname",
            "AA",
            "This is an offence title, This is an offence title 2",
            "This is a prosecutor organisation"
        );

        assertThat(SjpPublicListHelper.constructSjpCase(hearingNode))
            .isPresent()
            .hasValue(expectedSjpCase);
    }

    @Test
    void testSjpCaseIsGeneratedWhenAccusedHasForenamesOnly() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(JSON_PATH)), writer, Charset.defaultCharset());

        JsonNode hearingNode = OBJECT_MAPPER.readTree(writer.toString())
            .get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0)
            .get(HEARING).get(0);

        ((ObjectNode) hearingNode.get(PARTY).get(0)
            .get(INDIVIDUAL_DETAILS))
            .remove(INDIVIDUAL_SURNAME);

        SjpPublicList expectedSjpCase = new SjpPublicList(
            "A",
            "AA",
            "This is an offence title, This is an offence title 2",
            "This is a prosecutor organisation"
        );

        assertThat(SjpPublicListHelper.constructSjpCase(hearingNode))
            .isPresent()
            .hasValue(expectedSjpCase);
    }

    @Test
    void testSjpCaseIsGeneratedWhenNameMissing() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get(JSON_PATH)), writer, Charset.defaultCharset());

        JsonNode hearingNode = OBJECT_MAPPER.readTree(writer.toString())
            .get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0)
            .get(HEARING).get(0);

        ((ObjectNode) hearingNode.get(PARTY).get(0)
            .get(INDIVIDUAL_DETAILS))
            .remove(INDIVIDUAL_FORENAMES);

        ((ObjectNode) hearingNode.get(PARTY).get(0)
            .get(INDIVIDUAL_DETAILS))
            .remove(INDIVIDUAL_SURNAME);

        assertThat(SjpPublicListHelper.constructSjpCase(hearingNode)).isEmpty();
    }

    @Test
    void testSjpCaseIsNotGeneratedWhenPostcodeMissing() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/sjpPublicListMissingPostcode.json")),
                     writer, Charset.defaultCharset());

        JsonNode hearingNode = OBJECT_MAPPER.readTree(writer.toString())
            .get(COURT_LISTS).get(0)
            .get(COURT_HOUSE)
            .get(COURT_ROOM).get(0)
            .get(SESSION).get(0)
            .get(SITTINGS).get(0)
            .get(HEARING).get(0);

        assertThat(SjpPublicListHelper.constructSjpCase(hearingNode)).isEmpty();
    }
}
