package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
class PartyRoleHelperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PARTY = "party";

    private static final String PARTY_NAME_MESSAGE = "Party names do not match";

    private static JsonNode inputJson;

    @BeforeAll
    static void setup() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(
                         Paths.get("src/test/resources/mocks/partyManipulation.json")), writer,
                     Charset.defaultCharset()
        );
        inputJson = OBJECT_MAPPER.readTree(writer.toString());
    }

    @Test
    void testFindManipulatePartyInformationApplicant() {
        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, false);

        assertThat(inputJson.get("applicant").asText())
            .as("applicant is incorrect")
            .isEqualTo("Applicant Title Applicant Forename Applicant Middlename Applicant Surname");
    }

    @Test
    void testFindManipulatePartyInformationApplicantRepresentative() {
        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, false);

        assertThat(inputJson.get("applicantRepresentative").asText())
            .as("applicant representative is incorrect")
            .isEqualTo("Rep Title Rep Forename Rep Middlename Rep Surname");
    }

    @Test
    void testFindManipulatePartyInformationRespondent() {
        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, false);

        assertThat(inputJson.get("respondent").asText())
            .as("respondent is incorrect")
            .isEqualTo("Title Forename Middlename Surname");
    }

    @Test
    void testFindManipulatePartyInformationRespondentWithInitial() {
        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, true);

        assertThat(inputJson.get("respondent").asText())
            .as("respondent is incorrect")
            .isEqualTo("Title F Surname");
    }

    @Test
    void testFindManipulatePartyInformationRespondentRepresentative() {
        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, false);
        assertThat(inputJson.get("respondentRepresentative").asText())
            .as("respondent representative is incorrect")
            .isEqualTo("Mr ForenameB MiddlenameB SurnameB");
    }

    @Test
    void testFindManipulatePartyInformationRespondentRepresentativeWithInitial() {
        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, true);
        assertThat(inputJson.get("respondentRepresentative").asText())
            .as("respondent representative is incorrect")
            .isEqualTo("Mr F SurnameB");
    }

    @Test
    void testFindManipulatePartyInformationProsecutingAuthority() {
        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, false);

        assertThat(inputJson.get("prosecutingAuthority").asText())
            .as("prosecuting authority is incorrect")
            .isEqualTo("Title Forename Middlename Surname");
    }

    @Test
    void testFindManipulatePartyInformationClaimant() {
        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, false);

        assertThat(inputJson.get("claimant").asText())
            .as("claimant is incorrect")
            .isEqualTo("Claimant Title Claimant Forename Claimant Middlename Claimant Surname");
    }

    @Test
    void testFindManipulatePartyInformationClaimantWithInitial() {
        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, true);

        assertThat(inputJson.get("claimant").asText())
            .as("claimant is incorrect")
            .isEqualTo("Claimant Title C Claimant Surname");
    }

    @Test
    void testFindManipulatePartyInformationClaimantRepresentative() {
        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, false);

        assertThat(inputJson.get("claimantRepresentative").asText())
            .as("claimant representative is incorrect")
            .isEqualTo("Rep Title Rep Forename Rep Middlename Rep Surname");
    }

    @Test
    void testFindManipulatePartyInformationClaimantRepresentativeWithInitial() {
        PartyRoleHelper.findAndManipulatePartyInformation(inputJson, true);

        assertThat(inputJson.get("claimantRepresentative").asText())
            .as("claimant representative is incorrect")
            .isEqualTo("Rep Title R Rep Surname");
    }

    @Test
    void testFormatPartyDetails() {
        StringBuilder builder = new StringBuilder("name1");

        PartyRoleHelper.formatPartyDetails(builder, "name2");
        assertThat(builder)
            .as("Party non representative incorrect")
            .hasToString("name2, name1");
    }

    @Test
    void testCreateIndividualDetails() {
        assertThat(PartyRoleHelper.createIndividualDetails(inputJson.get(PARTY).get(0), false))
            .as("Individual details incorrect")
            .isEqualTo("Applicant Title Applicant Forename Applicant Middlename Applicant Surname");
    }

    @Test
    void testCreateIndividualDetailsWithInitials() {
        assertThat(PartyRoleHelper.createIndividualDetails(inputJson.get(PARTY).get(0), true))
            .as("Individual details incorrect")
            .isEqualTo("Applicant Title A Applicant Surname");
    }

    @Test
    void testCreateIndividualDetailsWithOrgInformation() {
        assertThat(PartyRoleHelper.createIndividualDetails(inputJson.get(PARTY).get(8), false))
            .as("Individual details should be blank")
            .isEmpty();
    }

    @Test
    void testCreateOrganisationDetails() {
        assertThat(PartyRoleHelper.createOrganisationDetails(inputJson.get(PARTY).get(8)))
            .as("Organisation details incorrect")
            .isEqualTo("Defendant rep name");
    }

    @Test
    void testCreateOrganisationDetailsWithIndividualInformation() {
        assertThat(PartyRoleHelper.createOrganisationDetails(inputJson.get(PARTY).get(0)))
            .as("Organisation details incorrect")
            .isEmpty();
    }

    @Test
    void testHandleDefendantParty() {
        PartyRoleHelper.handleParties(inputJson);
        assertThat(inputJson.get("defendant").asText())
            .as(PARTY_NAME_MESSAGE)
            .isEqualTo("SurnameA, ForenamesA, SurnameB, ForenamesB");
    }

    @Test
    void testHandleDefendantRepresentativeParty() {
        PartyRoleHelper.handleParties(inputJson);
        assertThat(inputJson.get("defendantRepresentative").asText())
            .as(PARTY_NAME_MESSAGE)
            .isEqualTo("Defendant rep name");
    }

    @Test
    void testHandleProsecutingAuthorityParty() {
        PartyRoleHelper.handleParties(inputJson);
        assertThat(inputJson.get("prosecutingAuthority").asText())
            .as(PARTY_NAME_MESSAGE)
            .isEqualTo("Prosecuting authority name");
    }


}
