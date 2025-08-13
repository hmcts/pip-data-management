package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesadultcourtlist.CaseInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesadultcourtlist.MagistratesAdultCourtList;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.magistratesadultcourtlist.Offence;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@ActiveProfiles("test")
class MagistratesAdultCourtListHelperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static JsonNode inputJson;

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter magistratesPublicWriter = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get("src/test/resources/mocks/magistratesAdultCourtList.json")),
            magistratesPublicWriter, Charset.defaultCharset()
        );

        inputJson = OBJECT_MAPPER.readTree(magistratesPublicWriter.toString());
    }

    @Test
    void testMagistratesAdultCourtListResultCount() {
        List<MagistratesAdultCourtList> results = MagistratesAdultCourtListHelper.processPayload(inputJson,
                                                                                                 Language.ENGLISH);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(results)
            .as("Magistrates Adult Court List result count does not match")
            .hasSize(2);


        softly.assertThat(results.get(0).getCases())
            .as("Magistrates Adult Court List first result case count does not match")
            .hasSize(2);


        softly.assertThat(results.get(1).getCases())
            .as("Magistrates Adult Court List second result case count does not match")
            .hasSize(2);

        softly.assertAll();
    }

    @Test
    void testCourtAndSessionInfo() {
        List<MagistratesAdultCourtList> results = MagistratesAdultCourtListHelper.processPayload(inputJson,
                                                                                                 Language.ENGLISH);
        MagistratesAdultCourtList result = results.get(0);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getLja())
            .as("Magistrates Adult Court List LJA does not match")
            .isEqualTo("North Northumbria Magistrates' Court");

        softly.assertThat(result.getCourtName())
            .as("Magistrates Adult Court List court name does not match")
            .isEqualTo("North Shields Magistrates' Court");

        softly.assertThat(result.getCourtRoom())
            .as("Magistrates Adult Court List court room does not match")
            .isEqualTo("1");

        softly.assertThat(result.getSessionStartTime())
            .as("Magistrates Adult Court List session start time does not match")
            .isEqualTo("9am");

        softly.assertAll();
    }

    @Test
    void testCaseInfo() {
        List<MagistratesAdultCourtList> results = MagistratesAdultCourtListHelper.processPayload(inputJson,
                                                                                                 Language.ENGLISH);
        CaseInfo caseInfo = results.get(0).getCases().get(0);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(caseInfo.getBlockStartTime())
            .as("Magistrates Adult Court List block start time does not match")
            .isEqualTo("9am");

        softly.assertThat(caseInfo.getCaseNumber())
            .as("Magistrates Adult Court List case number does not match")
            .isEqualTo("1000000000");

        softly.assertThat(caseInfo.getDefendantName())
            .as("Magistrates Adult Court List defendant name does not match")
            .isEqualTo("Mr Test User");

        softly.assertThat(caseInfo.getDefendantDob())
            .as("Magistrates Adult Court List defendant dob does not match")
            .isEqualTo("06/11/1975");

        softly.assertThat(caseInfo.getDefendantAge())
            .as("Magistrates Adult Court List defendant age does not match")
            .isEqualTo("50");

        softly.assertThat(caseInfo.getDefendantAddress())
            .as("Magistrates Adult Court List defendant address does not match")
            .isEqualTo("1 High Street, London, SW1A 1AA");

        softly.assertThat(caseInfo.getInformant())
            .as("Magistrates Adult Court List informant does not match")
            .isEqualTo("POL01");

        softly.assertAll();
    }

    @Test
    void testOffenceInfo() {
        List<MagistratesAdultCourtList> results = MagistratesAdultCourtListHelper.processPayload(inputJson,
                                                                                                 Language.ENGLISH);
        Offence offence = results.get(0).getCases().get(0).getOffence();
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(offence.getOffenceCode())
            .as("Magistrates Adult Court List offence code does not match")
            .isEqualTo("TH68001");

        softly.assertThat(offence.getOffenceTitle())
            .as("Magistrates Adult Court List offence title does not match")
            .isEqualTo("Offence title 1");

        softly.assertThat(offence.getOffenceSummary())
            .as("Magistrates Adult Court List offence summary does not match")
            .isEqualTo("Offence summary 1");

        softly.assertAll();
    }

    @Test
    void testWelshOffenceInfo() {
        List<MagistratesAdultCourtList> results = MagistratesAdultCourtListHelper.processPayload(inputJson,
                                                                                                 Language.WELSH);
        Offence offence = results.get(0).getCases().get(0).getOffence();
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(offence.getOffenceCode())
            .as("Magistrates Adult Court List offence code does not match")
            .isEqualTo("TH68001");

        softly.assertThat(offence.getOffenceTitle())
            .as("Magistrates Adult Court List offence title does not match")
            .isEqualTo("Welsh offence title 1");

        softly.assertThat(offence.getOffenceSummary())
            .as("Magistrates Adult Court List offence summary does not match")
            .isEqualTo("Welsh offence summary 1");

        softly.assertAll();
    }
}
