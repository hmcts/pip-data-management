package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownfirmpddalist.CrownFirmPddaList;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownfirmpddalist.HearingInfo;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownfirmpddalist.SittingInfo;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@ActiveProfiles("test")
class CrownFirmPddaListHelperTest {
    private static JsonNode inputJson;

    @BeforeAll
    public static void setup()  throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(
            Files.newInputStream(Paths.get("src/test/resources/mocks/crownFirmPddaList.json")), writer,
            Charset.defaultCharset()
        );

        inputJson = new ObjectMapper().readTree(writer.toString());
    }

    @Test
    void testCrownFirmPddaListFormattedMethod() {
        List<CrownFirmPddaList> results = CrownFirmPddaListHelper.crownFirmPddaListFormatted(inputJson);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(results)
            .as("Crown Firm PDDA List result count does not match")
            .hasSize(1);

        softly.assertThat(results.getFirst().getSittings())
            .as("Crown Firm PDDA List sitting count does not match")
            .hasSize(1);

        softly.assertThat(results.getFirst().getSittings().getFirst().getHearings())
            .as("Crown Firm PDDA List hearing count does not match")
            .hasSize(1);

        softly.assertAll();

    }

    @Test
    void testCourtListInfo() {
        List<CrownFirmPddaList> results = CrownFirmPddaListHelper.crownFirmPddaListFormatted(inputJson);

        CrownFirmPddaList result = results.getFirst();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSittingDate())
            .as("Crown Firm PDDA List sitting date does not match")
            .isEqualTo("01 January 2024");

        softly.assertThat(result.getCourtName())
            .as("Crown Firm PDDA List court name does not match")
            .isEqualTo("TestCourtHouseName");

        softly.assertThat(result.getCourtAddress())
            .as("Crown Firm PDDA List court address does not match")
            .isEqualTo("TestAddressLine1, TestPostcode");

        softly.assertThat(result.getCourtPhone())
            .as("Crown Firm PDDA List court phone number does not match")
            .isEqualTo("TestTelephone");

        softly.assertAll();
    }

    @Test
    void testSittingInfo() {
        List<CrownFirmPddaList> results = CrownFirmPddaListHelper.crownFirmPddaListFormatted(inputJson);

        SittingInfo result = results.getFirst().getSittings().getFirst();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getCourtRoomNumber())
            .as("Crown Firm PDDA List court room number does not match")
            .isEqualTo("1");

        softly.assertThat(result.getSittingAt())
            .as("Crown Firm PDDA List sitting at does not match")
            .isEqualTo("10am");

        softly.assertThat(result.getJudgeName())
            .as("Crown Firm PDDA List judge name does not match")
            .isEqualTo("TestJudgeRequested, TestJusticeRequested");

        softly.assertAll();
    }

    @Test
    void testHearingInfo() {
        List<CrownFirmPddaList> results = CrownFirmPddaListHelper.crownFirmPddaListFormatted(inputJson);

        HearingInfo result = results.getFirst().getSittings().getFirst().getHearings().getFirst();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getCaseNumber())
            .as("Crown Firm PDDA List case number does not match")
            .isEqualTo("T00112233");

        softly.assertThat(result.getDefendantName())
            .as("Crown Firm PDDA List defendant name does not match")
            .isEqualTo("1TestMaskedName, 2TestDefendantRequestedName, 3TestDefendantTitle"
                           + " 3TestDefendantForename 3TestDefendantSurname 3TestDefendantSuffix");

        softly.assertThat(result.getHearingType())
            .as("Crown Firm PDDA List hearing type does not match")
            .isEqualTo("TestHearingDescription");

        softly.assertThat(result.getRepresentativeName())
            .as("Crown Firm PDDA List representative name does not match")
            .isEqualTo("TestSolicitorRequestedName, TestSolicitorOrg");

        softly.assertThat(result.getProsecutingAuthority())
            .as("Crown Firm PDDA List prosecuting authority does not match")
            .isEqualTo("Crown Prosecution Service");

        softly.assertThat(result.getListNote())
            .as("Crown Firm PDDA List list note does not match")
            .isEqualTo("TestListNote");

        softly.assertAll();
    }

}
