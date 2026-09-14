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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.pip.model.publication.ListType.BUSINESS_AND_PROPERTY_DIVISION_ROLLS_BUILDING_DAILY_CAUSE_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BusinessAndPropertyDivisionRollsBuildingDailyCauseListFileConverterTest {
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
    private static final Map<String, String> COMMON_METADATA = Map.of(
        CONTENT_DATE_METADATA, CONTENT_DATE,
        PROVENANCE_METADATA, PROVENANCE,
        LIST_TYPE_METADATA, BUSINESS_AND_PROPERTY_DIVISION_ROLLS_BUILDING_DAILY_CAUSE_LIST.name(),
        LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
    );

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String VENUE_NAME_ELEMENT = "venue-name";
    private static final String ADDRESS_LINE1_ELEMENT = "address-line-1";
    private static final String ADDRESS_LINE2_ELEMENT = "address-line-2";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String SUMMARY_TITLE_CLASS = "govuk-details__summary-text";
    private static final String LINK_CLASS = "govuk-link";
    private static final String HREF = "href";
    private static final String BODY_CLASS = "govuk-body";
    private static final String HEADING_CLASS = "govuk-heading-l";

    private static final String IMPORTANT_INFORMATION_HEADING_1 = "important-information-heading-1";
    private static final String IMPORTANT_INFORMATION_HEADING_2 = "important-information-heading-2";
    private static final String IMPORTANT_INFORMATION_HEADING_3 = "important-information-heading-3";
    private static final String IMPORTANT_INFORMATION_ELEMENT_1 = "important-information-line-1";
    private static final String IMPORTANT_INFORMATION_ELEMENT_2 = "important-information-line-2";
    private static final String IMPORTANT_INFORMATION_ELEMENT_3 = "important-information-line-3";
    private static final String IMPORTANT_INFORMATION_ELEMENT_3_EMAIL = "important-information-line-3-email";
    private static final String IMPORTANT_INFORMATION_ELEMENT_4 = "important-information-line-9";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LINK_MESSAGE = "Link does not match";
    private static final String VENUE_MESSAGE = "Venue does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String LAST_UPDATED_DATE_MESSAGE = "Last updated date does not match";
    private static final String IMPORTANT_INFORMATION_MESSAGE = "Important information does not match";
    private static final String SECTION_HEADING_MESSAGE = "Section heading does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode inputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/businessAndPropertyDivisionRollsBuildingDailyCauseList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testBusinessAndPropertyDivisionRollsBuildingDailyCauseListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/"
                                     + "businessAndPropertyDivisionRollsBuildingDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.putAll(COMMON_METADATA);
        metadata.put(LANGUAGE_METADATA, ENGLISH);

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Business and Property Division Rolls Building Daily Cause List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Business and Property Division Rolls Building Daily Cause List");

        softly.assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Find contact details and other information about courts and tribunals in England "
                           + "and Wales, and some non-devolved tribunals in Scotland.");

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

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("These lists are subject to change until 4:30pm. Any alterations after this time will be "
                           + "telephoned or emailed direct to the parties or their legal representatives.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Remote Hearings");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("If a member of the public or media wishes to attend a remote hearing, they should contact the "
                           + "relevant listing office. The correct office depends on the judge hearing the case.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Contact details:");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_3).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Business and Property Division High Court Judge:");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_3_EMAIL).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("BPD.HCJListing@justice.gov.uk");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_3).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Remote Judgments");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_4).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Judgments may be handed down remotely. They are sent to the parties (or their representatives) "
                           + "by email and published on The National Archives website shortly afterwards.");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(1).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Appeal List");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(2).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Business List");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(3).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Commercial Court");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(4).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Financial List");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(5).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Insolvency and Companies Court");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(6).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Intellectual Property and Enterprise Court");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(7).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Intellectual Property List");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(8).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("London Circuit Commercial Court");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(9).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Patents Court");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(10).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Property, Trusts and Probate List");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(11).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Technology and Construction Court");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(12).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Admiralty Court");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(13).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Companies Winding Up");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(14).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Competition List");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(15).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Pensions List");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(16).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Revenue List");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(105)
            .extracting(Element::text)
            .containsSequence(
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
    void testBusinessAndPropertyDivisionRollsBuildingDailyCauseListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/"
                                     + "businessAndPropertyDivisionRollsBuildingDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.putAll(COMMON_METADATA);
        metadata.put(LANGUAGE_METADATA, WELSH);

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Adran Busnes ac Eiddo - Adeilad Rolls");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Adran Busnes ac Eiddo - Adeilad Rolls");

        softly.assertThat(document.getElementsByClass(LINK_CLASS).get(0)
                              .getElementsByTag("a").get(0)
                              .attr(HREF))
            .as(LINK_MESSAGE)
            .isEqualTo("https://www.find-court-tribunal.service.gov.uk/");

        assertThat(document.getElementsByClass(BODY_CLASS).get(0).text())
            .as(LINK_MESSAGE)
            .isEqualTo("Dod o hyd i fanylion cyswllt a gwybodaeth arall am lysoedd a thribiwnlysoedd yng "
                           + "Nghymru a Lloegr a rhai tribiwnlysoedd heb eu datganoli yn yr Alban.");

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

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gall y rhestrau canlynol fod yn destun newid tan 4:30pm. Bydd unrhyw newidiadau ar ôl yr amser "
                           + "hwn yn cael eu cyfathrebu dros y ffôn neu drwy e-bost yn uniongyrchol at y partïon neu "
                           + "eu cynrychiolwyr cyfreithiol.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwrandawiadau o Bell");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Os yw aelod o'r cyhoedd neu'r cyfryngau eisiau mynychu gwrandawiad o bell, dylent gysylltu â'r "
                           + "swyddfa restru berthnasol. Mae'r swyddfa gywir yn dibynnu ar y barnwr sy'n gwrando'r "
                           + "achos.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Manylion cyswllt:");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_3).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Barnwr Uchel Lys - Yr Adran Busnes ac Eiddo:");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_3_EMAIL).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("BPD.HCJListing@justice.gov.uk");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_3).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Dyfarniadau o Bell");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_4).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gall dyfarniadau gael eu traddodi o bell. Maent yn cael eu hanfon at y partïon (neu eu "
                           + "cynrychiolwyr) trwy e-bost ac yn cael eu cyhoeddi ar wefan yr Archifau Cenedlaethol yn "
                           + "fuan ar ôl hynny.");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(1).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Rhestr Apeliadau");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(2).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Rhestr Fusnes");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(3).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Llys Masnach");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(4).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Rhestr Ariannol");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(5).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Llys Ansolfedd a Chwmnïau");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(6).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Llys Mentrau Eiddo Deallusol");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(7).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Rhestr Eiddo Deallusol");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(8).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Llys Masnach - Cylchdaith Llundain");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(9).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Llys Patentau");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(10).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Rhestr Eiddo, Ymddiriedolaethau a Phrofiant");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(11).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Llys Technoleg ac Adeiladwaith");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(12).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Llys y Morlys");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(13).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Dirwyn Cwmnïau i Ben");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(14).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Rhestr Gystadleuaeth");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(15).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Rhestr Pensiynau");

        assertThat(document.getElementsByClass(HEADING_CLASS).get(16).text())
            .as(SECTION_HEADING_MESSAGE)
            .isEqualTo("Y Rhestr Refeniw");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(105)
            .extracting(Element::text)
            .containsSequence(
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

    @Test
    void testBusinessAndPropertyDivisionRollsBuildingDailyCauseListTableContents() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/"
                                     + "businessAndPropertyDivisionRollsBuildingDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                }
            );
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.putAll(COMMON_METADATA);
        metadata.put(LANGUAGE_METADATA, ENGLISH);

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        assertThat(document.getElementsByTag("td"))
            .as("Table contents does not match")
            .extracting(Element::text)
            .containsSequence(
                "Judge A",
                "9am",
                "Venue A",
                "Type A",
                "12345",
                "Case name A",
                "This is additional information"
            );
    }
}
