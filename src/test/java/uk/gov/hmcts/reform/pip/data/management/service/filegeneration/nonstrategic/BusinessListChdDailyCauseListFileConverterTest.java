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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.BUSINESS_LIST_CHD_DAILY_CAUSE_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BusinessListChdDailyCauseListFileConverterTest {
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
    private static final String VENUE_NAME_ELEMENT = "venue-name";
    private static final String ADDRESS_LINE1_ELEMENT = "address-line-1";
    private static final String ADDRESS_LINE2_ELEMENT = "address-line-2";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String SUMMARY_TITLE_CLASS = "govuk-details__summary-text";

    private static final String IMPORTANT_INFORMATION_HEADING_1 = "important-information-heading-1";
    private static final String IMPORTANT_INFORMATION_HEADING_2 = "important-information-heading-2";
    private static final String IMPORTANT_INFORMATION_HEADING_3 = "important-information-heading-3";

    private static final String IMPORTANT_INFORMATION_ELEMENT_1 = "important-information-line-1";
    private static final String IMPORTANT_INFORMATION_ELEMENT_2 = "important-information-line-2";
    private static final String IMPORTANT_INFORMATION_ELEMENT_3 = "important-information-line-3";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String VENUE_MESSAGE = "Venue does not match";
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
            .getResourceAsStream("/mocks/non-strategic/businessListChdDailyCauseList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testBusinessListChdDailyCauseListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/"
                                     + "businessListChdDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH, LIST_TYPE_METADATA,
                                              BUSINESS_LIST_CHD_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Business List (Chancery Division) Daily Cause List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Business List (Chancery Division) Daily Cause List");

        softly.assertThat(document.getElementById(VENUE_NAME_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Rolls Building");

        softly.assertThat(document.getElementById(ADDRESS_LINE1_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Fetter Lane, London");

        softly.assertThat(document.getElementById(ADDRESS_LINE2_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("EC4A 1NL");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("List for 23 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Last updated 22 April 2025 at 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TITLE_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Remote hearings before a High Court Judge");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("If a representative of the media or member of the public "
                          + "wishes to attend the hearing they should contact the "
                          + "listing office chanceryjudgeslisting@justice.gov.uk "
                          + "who will put them in touch with the relevant person.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Remote hearings before a Chancery Master");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("If a representative of the media or member of the public "
                          + "wishes to attend the remote hearing they should contact "
                          + "chancery.mastersappointments@justice.gov.uk who will put "
                          + "them in touch with the relevant person.");


        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_3).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Remote judgments");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_3).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Remote hand-down: This judgment will be handed down "
                          + "remotely by circulation to the parties or their "
                          + "representatives by email and release to The National "
                          + "Archives. A copy of the judgment in final form as "
                          + "handed down should be available on The National Archives website "
                          + "shortly thereafter. Members of the media can obtain a copy on request "
                          + "by email to the Judicial Office press.enquiries@judiciary.uk");



        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Judge",
                "Time",
                "Venue",
                "Type",
                "Case number",
                "Case name",
                "Additional information"
            );

        softly.assertAll();
    }

    @Test
    void testBusinessListChdDailyCauseListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/"
                                     + "businessListChdDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH, LIST_TYPE_METADATA,
                                              BUSINESS_LIST_CHD_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol y Llys Busnes (Adran Siawnsri)");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol y Llys Busnes (Adran Siawnsri)");

        softly.assertThat(document.getElementById(VENUE_NAME_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Adeilad Rolls");

        softly.assertThat(document.getElementById(ADDRESS_LINE1_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Fetter Lane, London");

        softly.assertThat(document.getElementById(ADDRESS_LINE2_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("EC4A 1NL");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("Rhestr ar gyfer 23 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 22 April 2025 am 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TITLE_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Gwrandawiadau o bell gerbron Barnwr yr Uchel Lys");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Os yw cynrychiolydd o'r cyfryngau neu aelod o'r "
                          + "cyhoedd yn dymuno mynychu'r gwrandawiad, dylent gysylltu â'r "
                          + "swyddfa restru yn chanceryjudgeslisting@justice.gov.uk a fydd "
                          + "yn eu rhoi mewn cysylltiad â'r unigolyn perthnasol.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Gwrandawiadau o bell gerbron Meistr y Siawnsri");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Os yw cynrychiolydd o'r cyfryngau neu aelod o'r "
                          + "cyhoedd yn dymuno mynychu'r gwrandawiad o bell, "
                          + "dylent gysylltu â chancery.mastersappointments@justice.gov.uk a "
                          + "fydd yn eu rhoi mewn cysylltiad â'r unigolyn perthnasol.");


        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_3).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Dyfarniadau o bell");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_3).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Traddodi o Bell: Bydd y dyfarniad hwn yn cael ei "
                          + "draddodi o bell trwy gylchrediad i'r partïon neu eu "
                          + "cynrychiolwyr trwy e-bost a'i ryddhau i'r Archifau Cenedlaethol. "
                          + "Dylai copi o'r dyfarniad ar ffurf derfynol fel y'i rhoddwyd fod ar "
                          + "gael ar wefan yr Archifau Cenedlaethol yn fuan wedyn. Gall aelodau'r "
                          + "cyfryngau gael copi ar gais trwy e-bost i'r Swyddfa Farnwrol "
                          + "press.enquiries@judiciary.uk");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Barnwr",
                "Amser",
                "Lleoliad",
                "Math",
                "Rhif yr achos",
                "Enw’r achos",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }
}
