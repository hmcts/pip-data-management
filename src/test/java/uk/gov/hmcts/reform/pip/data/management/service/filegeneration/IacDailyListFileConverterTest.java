package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

@ActiveProfiles("test")
class IacDailyListFileConverterTest {

    private static final IacDailyListFileConverter CONVERTER = new IacDailyListFileConverter();

    private static final String GOVUK_TABLE_BODY = "govuk-table__body";
    private static final String TABLE_ROW_ERROR = "Incorrect table rows";
    private static final String CASE_REF = "12341234 [2 of 3]";
    private static final String RESPONDENT = "Authority Surname";
    private static final String IAC_DAILY_LIST = "IAC_DAILY_LIST";
    private static final String IAC_DAILY_LIST_ADDITIONAL_CASES = "IAC_DAILY_LIST_ADDITIONAL_CASES";

    private static Document setupTest(ListType listType) throws IOException {
        Map<String, String> metaData = Map.of("contentDate", "02 October 2022",
                                              "language", "ENGLISH",
                                              "provenance", "MANUAL_UPLOAD",
                                              "locationName", "Location Name",
                                              "listType", listType.name()
        );
        Map<String, Object> language = TestUtils.getLanguageResources(listType, "en");
        JsonNode input = getInput("/mocks/iacDailyList.json");

        String result = CONVERTER.convert(input, metaData, language);
        return Jsoup.parse(result);
    }

    @EnumSource(value = ListType.class, names = {IAC_DAILY_LIST, IAC_DAILY_LIST_ADDITIONAL_CASES})
    @ParameterizedTest
    void testSuccessfulConversionMetadata(ListType listType) throws IOException {
        Document doc = setupTest(listType);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(doc.getElementsByTag("h2"))
            .as("Incorrect h2 element")
            .hasSize(2)
            .extracting(Element::text)
            .containsExactly(
                "First-tier Tribunal: Immigration and Asylum Chamber",
                "Location Name Daily List" + (listType.equals(ListType.IAC_DAILY_LIST_ADDITIONAL_CASES)
                    ? " - Additional Cases" : "")
            );

        softly.assertThat(doc.getElementsByClass("header").get(0).getElementsByTag("p"))
            .as("Incorrect p elements")
            .isNotEmpty()
            .extracting(Element::text)
            .contains(
                "List for 02 October 2022",
                "Last updated 20 October 2022 at 9pm"
            );

        softly.assertThat(doc.getElementsByTag("section").get(0)
                              .getElementsByTag("p").get(0)
                              .text())
            .as("Incorrect data source")
            .isEqualTo("Data Source: MANUAL_UPLOAD");

        softly.assertAll();
    }

    @EnumSource(value = ListType.class, names = {IAC_DAILY_LIST, IAC_DAILY_LIST_ADDITIONAL_CASES})
    @ParameterizedTest
    void testSuccessfulConversionBailList(ListType listType) throws IOException {
        Document doc = setupTest(listType);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(doc.getElementsByTag("h1").get(0).text())
            .as("Incorrect h1 element")
            .isEqualTo("Bail List");

        softly.assertThat(doc.getElementsByClass("govuk-accordion").get(0)
                              .getElementsByTag("h3").get(0)
                              .text())
            .as("Incorrect room name element")
            .isEqualTo("Court Room A, Before Judge Test Name, Magistrate Test Name");

        softly.assertThat(doc.getElementsByClass("govuk-table__head").get(0).getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Start Time",
                "Case Ref",
                "Appellant/Applicant",
                "Respondent",
                "Interpreter Language",
                "Hearing Channel",
                "Hearing Type"
            );

        softly.assertThat(doc.getElementsByClass(GOVUK_TABLE_BODY).get(0).getElementsByTag("td"))
            .as(TABLE_ROW_ERROR)
            .hasSize(7)
            .extracting(Element::text)
            .contains(
                "9:00pm",
                CASE_REF,
                "Surname Rep: Mr Individual Forenames Individual Surname",
                RESPONDENT,
                "French",
                "Teams, Attended",
                "Directions"
            );

        softly.assertThat(doc.getElementsByClass(GOVUK_TABLE_BODY).get(1).getElementsByTag("td"))
            .as(TABLE_ROW_ERROR)
            .hasSize(7)
            .extracting(Element::text)
            .contains(
                "9:00pm",
                CASE_REF,
                "Organisation Name Rep: Organisation Name",
                "Organisation Name",
                "French",
                "VIDEO HEARING",
                ""
            );

        softly.assertAll();
    }

    @EnumSource(value = ListType.class, names = {IAC_DAILY_LIST, IAC_DAILY_LIST_ADDITIONAL_CASES})
    @ParameterizedTest
    void testSuccessfulConversionNonBailList(ListType listType) throws IOException {
        Document doc = setupTest(listType);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(doc.getElementsByTag("h1").get(1).text())
            .as("Incorrect h1 element")
            .isEqualTo("Non Bail List");

        softly.assertThat(doc.getElementsByClass("govuk-accordion").get(1)
                              .getElementsByTag("h3").get(0)
                              .text())
            .as("Incorrect room name element")
            .isEqualTo("Hearing Room: Court Room B");

        softly.assertThat(doc.getElementsByClass("govuk-table__head").get(2).getElementsByTag("th"))
            .as("Incorrect table headers")
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Start Time",
                "Case Ref",
                "Appellant/Applicant",
                "Respondent",
                "Interpreter Language",
                "Hearing Channel",
                "Hearing Type"
            );

        softly.assertThat(doc.getElementsByClass(GOVUK_TABLE_BODY).get(2)
                              .getElementsByClass("govuk-table__row").get(0).getElementsByTag("td"))
            .as(TABLE_ROW_ERROR)
            .hasSize(7)
            .extracting(Element::text)
            .contains(
                "9:20pm",
                CASE_REF,
                "Surname Rep: No Representative",
                RESPONDENT,
                "",
                "Teams, Attended",
                ""
            );

        softly.assertThat(doc.getElementsByClass(GOVUK_TABLE_BODY).get(2)
                              .getElementsByClass("govuk-table__row").get(1).getElementsByTag("td"))
            .as(TABLE_ROW_ERROR)
            .hasSize(7)
            .extracting(Element::text)
            .contains(
                "9:20pm",
                CASE_REF,
                "Surname Rep: Mr Individual Forenames Individual Surname",
                RESPONDENT,
                "",
                "VIDEO HEARING",
                ""
            );

        softly.assertAll();
    }

    private static JsonNode getInput(String resourcePath) throws IOException {
        try (InputStream inputStream = IacDailyListFileConverterTest.class.getResourceAsStream(resourcePath)) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            return new ObjectMapper().readTree(inputRaw);
        }
    }

}
