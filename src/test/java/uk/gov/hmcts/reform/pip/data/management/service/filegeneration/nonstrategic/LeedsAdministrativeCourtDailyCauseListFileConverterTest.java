package uk.gov.hmcts.reform.pip.data.management.service.filegeneration.nonstrategic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.NonStrategicListFileConverter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

class LeedsAdministrativeCourtDailyCauseListFileConverterTest {

    private static final String CONTENT_DATE = "23 April 2025";
    private static final String LAST_RECEIVED_DATE = "2025-04-22T09:30:00Z";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";
    private static final String LAST_RECEIVED_DATE_METADATA = "lastReceivedDate";

    private static final String ENGLISH = "ENGLISH";
    private static final String WELSH = "WELSH";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String SUMMARY_TITLE_CLASS = "govuk-details__summary-text";

    private static final String IMPORTANT_INFORMATION_HEADING_3 = "important-information-heading-3";

    private static final String IMPORTANT_INFORMATION_ELEMENT_1 = "important-information-line-1";
    private static final String IMPORTANT_INFORMATION_ELEMENT_2 = "important-information-line-2";
    private static final String IMPORTANT_INFORMATION_ELEMENT_3 = "important-information-line-3";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String LAST_UPDATED_DATE_MESSAGE = "Last updated date does not match";
    private static final String IMPORTANT_INFORMATION_MESSAGE = "Important information heading does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";
    private static final String BODY_MESSAGE = "Body does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode inputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/leedsAdministrativeCourtDailyCauseList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testLeedsAdministrativeCourtDailyCauseListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/"
                                     + "leedsAdministrativeCourtDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH, LIST_TYPE_METADATA,
                                              LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Leeds Administrative Court Daily Cause List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Leeds Administrative Court Daily Cause List");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("List for 23 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Last updated 22 April 2025 at 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TITLE_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("A hearing before the Administrative Court in Leeds is a public hearing "
                          + "unless otherwise specified. If any person or representative of the media "
                          + "wishes to attend a remote hearing they should contact the listing office "
                          + "in good time before the scheduled hearing at "
                          + "leeds@administrativecourtoffice.justice.gov.uk.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("If you have any other listing enquiries please contact the Administrative "
                          + "Court at Leeds by telephone on 0113 306 2578 or alternatively on "
                          + "the email address above.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_3).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Judgments");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_3).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Judgments handed down by the judge remotely will be released "
                          + "by circulation to the parties’ representatives by email and "
                          + "release to the National Archives. The date and time for hand-down "
                          + "will be deemed to be not before time listed. A copy of the judgment "
                          + "in final form as handed down can be made available after that time, "
                          + "on request by email.");


        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Venue",
                "judge",
                "Time",
                "Case number",
                "Case details",
                "Hearing type",
                "Additional information"
            );

        softly.assertAll();
    }

    @Test
    void testLeedsAdministrativeCourtDailyCauseListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/"
                                     + "leedsAdministrativeCourtDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH, LIST_TYPE_METADATA,
                                              LEEDS_ADMINISTRATIVE_COURT_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Llys Gweinyddol Leeds");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Llys Gweinyddol Leeds");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("Rhestr ar gyfer 23 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 22 April 2025 am 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TITLE_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");


        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Mae gwrandawiad gerbron y Llys Gweinyddol yn Leeds yn wrandawiad "
                          + "cyhoeddus oni nodir fel arall. Os yw unrhyw unigolyn neu "
                          + "gynrychiolydd o'r cyfryngau yn dymuno mynychu gwrandawiad o bell, "
                          + "dylent gysylltu â'r swyddfa restru mewn da bryd cyn y gwrandawiad "
                          + "yn leeds@administrativecourtoffice.justice.gov.uk.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Os oes gennych unrhyw ymholiadau rhestru eraill, cysylltwch â'r "
                          + "Llys Gweinyddol yn Leeds dros y ffôn ar 0113 306 2578 "
                          + "neu fel arall yn y cyfeiriad e-bost uchod.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_3).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Dyfarniadau");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_3).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Bydd dyfarniadau a draddodir gan y barnwr o bell yn cael "
                          + "eu rhyddhau trwy gylchrediad i gynrychiolwyr y partïon "
                          + "trwy e-bost a'u rhyddhau i'r Archifau Cenedlaethol. Ystyrir "
                          + "na fydd y dyddiad a'r amser ar gyfer traddodi yn digwydd cyn yr "
                          + "amser a restrwyd. Gellir darparu copi o'r dyfarniad ar ffurf derfynol "
                          + "fel y'i traddodwyd ar ôl yr amser hwnnw, ar gais drwy e-bost.");


        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Lleoliad",
                "Barnwyr",
                "Amser",
                "Rhif yr Achos",
                "Manylion yr achos",
                "Math o wrandawiad",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }
}
