package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.pip.data.management.service.ListConversionFactory;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SscsDailyListFileConverterTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE = "contentDate";

    private static final String TITLE_TEXT = "Incorrect Title Text";

    private final ListConversionFactory listConversionFactory = new ListConversionFactory();

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SSCS_DAILY_LIST", "SSCS_DAILY_LIST_ADDITIONAL_HEARINGS"})
    void testSscsDailyList(ListType listType) throws IOException {
        Map<String, Object> language = TestUtils.getLanguageResources(listType, "en");
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/", "sscsDailyList.json")),
                     writer, Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "Livingston",
                                                 "language", "ENGLISH",
                                                 "listType", listType.name()
        );
        JsonNode inputJson = OBJECT_MAPPER.readTree(writer.toString());
        String outputHtml = listConversionFactory.getFileConverter(listType)
            .get().convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("no HTML found").isNotEmpty();

        String expectedTitle = "";
        if (listType.equals(ListType.SSCS_DAILY_LIST)) {
            expectedTitle = "SSCS Daily List for Livingston - ";
        } else if (listType.equals(ListType.SSCS_DAILY_LIST_ADDITIONAL_HEARINGS)) {
            expectedTitle = "SSCS Daily List - Additional Hearings for Livingston - ";
        }
        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo(expectedTitle
                           + metadataMap.get(CONTENT_DATE));

        assertThat(document.getElementsByClass("mainHeaderText")
                       .select(".mainHeaderText > h1:nth-child(1)").text())
            .as("incorrect header text")
            .isEqualTo("Social Security and Child Support");

        assertThat(document.getElementsByClass("govuk-warning-text__text").get(0).text())
            .as("incorrect warning text")
            .isEqualTo("Please note: There may be 2 hearing lists available for this date. Please make sure "
                           + "you look at both lists to see all hearings happening on this date for this location.");

        assertThat(document.getElementsByTag("a")
                       .get(0).attr("title"))
            .as(TITLE_TEXT).contains("How to observe a court or tribunal hearing");

        assertThat(document.getElementsByTag("h2").get(1).text())
            .as("Header seems to be missing.")
            .isEqualTo("Test court house name");

        assertThat(document.getElementsByTag("p"))
            .as("data is missing")
            .hasSize(8)
            .extracting(Element::text)
            .containsSequence("Thank you for reading this document thoroughly.");

        assertThat(document.getElementsByTag("td"))
            .as("Incorrect channel when channel is present")
            .extracting(Element::text)
            .contains("Teams, Attended");

        assertThat(document.getElementsByTag("td"))
            .as("Incorrect channel when session is used")
            .extracting(Element::text)
            .contains("VIDEO HEARING");

        assertThat(document.getElementsByTag("td"))
            .as("Incorrect channel when sitting and session are empty")
            .extracting(Element::text)
            .contains("");

        assertThat(document.getElementsByTag("td"))
            .as("Incorrect appellant")
            .extracting(Element::text)
            .contains("Surname, Legal Advisor: Mr Individual Forenames Individual Middlename Individual Surname");

        assertThat(document.getElementsByTag("td"))
            .as("Incorrect respondent")
            .extracting(Element::text)
            .contains("Respondent Organisation, Respondent Organisation 2");

        assertThat(document.getElementsByTag("h5"))
            .as("Incorrect published date")
            .first()
            .asString()
            .contains(" at ");
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SSCS_DAILY_LIST", "SSCS_DAILY_LIST_ADDITIONAL_HEARINGS"})
    void testSscsDailyListWelsh(ListType listType) throws IOException {
        Map<String, Object> language = TestUtils.getLanguageResources(listType, "cy");
        StringWriter writer = new StringWriter();
        IOUtils.copy(Files.newInputStream(Paths.get("src/test/resources/mocks/", "sscsDailyList.json")), writer,
                     Charset.defaultCharset()
        );
        Map<String, String> metadataMap = Map.of(CONTENT_DATE, Instant.now().toString(),
                                                 PROVENANCE, PROVENANCE,
                                                 "locationName", "Livingston",
                                                 "language", "WELSH",
                                                 "listType", listType.name()
        );
        JsonNode inputJson = OBJECT_MAPPER.readTree(writer.toString());
        String outputHtml = listConversionFactory.getFileConverter(listType)
            .get().convert(inputJson, metadataMap, language);
        Document document = Jsoup.parse(outputHtml);
        assertThat(outputHtml).as("no HTML found").isNotEmpty();

        assertThat(document.title()).as("incorrect title found.")
            .isEqualTo("Rhestr Ddyddiol SSCS ar gyfer Livingston - "
                           + metadataMap.get(CONTENT_DATE));

        assertThat(document.getElementsByClass("mainHeaderText")
                       .select(".mainHeaderText > h1:nth-child(1)").text())
            .as("incorrect header text").isEqualTo("Nawdd Cymdeithasol a Chynnal Plant");

        assertThat(document.getElementsByTag("a")
                       .get(0).attr("title"))
            .as(TITLE_TEXT).contains("Sut i arsylwi gwrandawiad llys neu dribiwnlys");

        assertThat(document.getElementsByTag("h2").get(1).text())
            .as("Header seems to be missing.")
            .isEqualTo("Test court house name");

        assertThat(document.getElementsByTag("p"))
            .as("data is missing")
            .hasSize(8)
            .extracting(Element::text)
            .containsSequence("Ffynhonnell y Data: provenance");

        assertThat(document.getElementsByTag("td"))
            .as("Incorrect appellant")
            .extracting(Element::text)
            .contains("Surname, Cynghorydd Cyfreithiol: "
                          + "Mr Individual Forenames Individual Middlename Individual Surname");

        assertThat(document.getElementsByTag("td"))
            .as("Incorrect respondent")
            .extracting(Element::text)
            .contains("Respondent Organisation, Respondent Organisation 2");

        assertThat(document.getElementsByTag("h5"))
            .as("Incorrect published date")
            .first()
            .asString()
            .contains(" am ");
    }

    @ParameterizedTest
    @EnumSource(value = ListType.class, names = {"SSCS_DAILY_LIST", "SSCS_DAILY_LIST_ADDITIONAL_HEARINGS"})
    void testConvertToExcelReturnsDefault(ListType listType) throws IOException {
        StringWriter writer = new StringWriter();
        JsonNode inputJson = OBJECT_MAPPER.readTree(writer.toString());

        assertEquals(0, listConversionFactory.getFileConverter(listType)
                         .get().convertToExcel(inputJson, listType).length,
                     "byte array wasn't empty"
        );
    }
}
