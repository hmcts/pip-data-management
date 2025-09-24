package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.crownpddalist.CrownWarnedPddaList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrownWarnedPddaListHelperTest {
    private static final String HEARING_TYPE_MESSAGE = "Hearing types do not match";
    private static final String ROW_COUNT_MESSAGE = "Row count does not match";
    private static final String ROW_VALUE_MESSAGE = "Row values do not match";
    private static final String WEEK_COMMENCING_MESSAGE = "Week commencing date does not match";

    JsonNode rawListJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(
            "/mocks/crownWarnedPddaList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            rawListJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testOrderingOfHearingTypes() {
        Set<String> expectedHearingTypes = new LinkedHashSet<>(
            Arrays.asList(
                "TestHearingDescription",
                "To be allocated"
            )
        );

        assertThat(CrownWarnedPddaListHelper.processPayload(rawListJson))
            .as(HEARING_TYPE_MESSAGE)
            .hasSize(2)
            .extracting(r -> r.keySet())
            .isEqualTo(expectedHearingTypes);
    }

    @Test
    void testTableRowCountForEachHearingType() {
        List<List<CrownWarnedPddaList>> values = CrownWarnedPddaListHelper
            .processPayload(rawListJson)
            .values()
            .stream()
            .toList();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(values.get(0))
            .as(ROW_COUNT_MESSAGE)
            .hasSize(1);

        softly.assertThat(values.get(1))
            .as(ROW_COUNT_MESSAGE)
            .hasSize(1);

        softly.assertAll();
    }

    @Test
    void testTableRowValuesForFirstHearingType() {
        Map<String, List<CrownWarnedPddaList>> listData = CrownWarnedPddaListHelper.processPayload(rawListJson);

        List<CrownWarnedPddaList> firstList = listData.values().iterator().next();

        assertThat(firstList.get(0))
            .as(ROW_VALUE_MESSAGE)
            .extracting(CrownWarnedPddaList::getFixedDate,
                        CrownWarnedPddaList::getCaseReference,
                        CrownWarnedPddaList::getDefendantNames,
                        CrownWarnedPddaList::getProsecutingAuthority,
                        CrownWarnedPddaList::getLinkedCases,
                        CrownWarnedPddaList::getListingNotes)
            .containsExactly("01/01/2024",
                             "T00112233",
                             "TestDefendantRequestedName",
                             "Crown Prosecution Service",
                             "TestLinkedCaseNumber",
                             "TestListNote");
    }

    @ParameterizedTest
    @CsvSource({
        "'01 January 2024', english, '01 January 2024'", // Monday
        "'02 January 2024', english, '01 January 2024'", // Tuesday -> Monday
        "'01 January 2024', welsh, '01 Ionawr 2024'", // Monday in Welsh
        "'02 January 2024', welsh, '01 Ionawr 2024'", // Tuesday -> Monday in Welsh
    })
    void testFormatContentDateShouldReturnCorrectMondayDateInSpecifiedLanguage(
        String inputDate, String language, String expectedOutput) {

        String result = CrownWarnedPddaListHelper.formatContentDate(inputDate, language);

        assertThat(result).isEqualTo(expectedOutput, WEEK_COMMENCING_MESSAGE);
    }


    @Test
    void testFormatContentDateShouldMaintainCorrectDateLogic() {
        // Test that the Monday calculation is correct for various days
        String[] testDates = {
            "01 January 2024", // Monday
            "02 January 2024", // Tuesday
            "03 January 2024", // Wednesday
            "04 January 2024", // Thursday
            "05 January 2024", // Friday
            "06 January 2024", // Saturday
            "07 January 2024"  // Sunday
        };

        String expectedMonday = "01 January 2024";

        for (String testDate : testDates) {
            String result = CrownWarnedPddaListHelper.formatContentDate(testDate, "english");
            assertThat(result).isEqualTo(expectedMonday, WEEK_COMMENCING_MESSAGE);
        }
    }
}
