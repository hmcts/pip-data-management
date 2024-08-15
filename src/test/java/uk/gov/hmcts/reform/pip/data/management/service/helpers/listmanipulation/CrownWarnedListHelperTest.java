package uk.gov.hmcts.reform.pip.data.management.service.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.templatemodels.CrownWarnedList;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrownWarnedListHelperTest {
    private static final String HEARING_TYPE_MESSAGE = "Hearing types do not match";
    private static final String ROW_COUNT_MESSAGE = "Row count does not match";
    private static final String ROW_VALUE_MESSAGE = "Row values do not match";

    JsonNode rawListJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(
            "/mocks/crownWarnedList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            rawListJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testOrderingOfHearingTypes() {
        Set<String> expectedHearingTypes = new LinkedHashSet<>(
            Arrays.asList(
                "Trial",
                "Pre-Trial review",
                "Appeal",
                "Appeal against Conviction",
                "Sentence",
                "To be allocated"
            )
        );

        assertThat(CrownWarnedListHelper.processRawListData(rawListJson, Language.ENGLISH))
            .as(HEARING_TYPE_MESSAGE)
            .hasSize(6)
            .extracting(r -> r.keySet())
            .isEqualTo(expectedHearingTypes);
    }

    @Test
    void testTableRowCountForEachHearingType() {
        List<List<CrownWarnedList>> values = CrownWarnedListHelper
            .processRawListData(rawListJson, Language.ENGLISH)
            .values()
            .stream()
            .toList();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(values.get(0))
            .as(ROW_COUNT_MESSAGE)
            .hasSize(3);

        softly.assertThat(values.get(1))
            .as(ROW_COUNT_MESSAGE)
            .hasSize(1);

        softly.assertThat(values.get(2))
            .as(ROW_COUNT_MESSAGE)
            .hasSize(3);

        softly.assertThat(values.get(3))
            .as(ROW_COUNT_MESSAGE)
            .hasSize(1);

        softly.assertThat(values.get(4))
            .as(ROW_COUNT_MESSAGE)
            .hasSize(2);

        softly.assertThat(values.get(5))
            .as(ROW_COUNT_MESSAGE)
            .hasSize(2);

        softly.assertAll();
    }

    @Test
    void testTableRowValuesForFirstHearingType() {
        List<CrownWarnedList> rows = CrownWarnedListHelper.processRawListData(rawListJson, Language.ENGLISH)
            .values()
            .stream()
            .toList()
            .get(0);

        assertThat(rows.get(0))
            .as(ROW_VALUE_MESSAGE)
            .extracting(CrownWarnedList::getCaseReference,
                        CrownWarnedList::getDefendant,
                        CrownWarnedList::getHearingDate,
                        CrownWarnedList::getDefendantRepresentative,
                        CrownWarnedList::getProsecutingAuthority,
                        CrownWarnedList::getLinkedCases,
                        CrownWarnedList::getListingNotes)
            .containsExactly("12345678",
                             "Surname 1, Forename 1",
                             "27/07/2022",
                             "Defendant rep 1",
                             "Prosecutor",
                             "123456, 123457",
                             "Note 1");
    }
}
