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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompaniesWindingUpChdDailyCauseListFileConverterTest {
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

    private static final String IMPORTANT_INFORMATION_ELEMENT_1 = "important-information-line-1";

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
            .getResourceAsStream("/mocks/non-strategic/companiesWindingUpChdDailyCauseList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testCompaniesWindingUpChdDailyCauseListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/"
                                     + "companiesWindingUpChdDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH, LIST_TYPE_METADATA,
                                              COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Companies Winding Up (Chancery Division) Daily Cause List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Companies Winding Up (Chancery Division) Daily Cause List");

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
            .isEqualTo("Company Insolvency Pro Bono Scheme");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("The Company Insolvency Pro Bono Scheme provides free legal advice "
                          + "and/or representation in court on Wednesdays. If you have a case "
                          + "listed in the winding up court, please come to Consultation Room 17 "
                          + "on the 2nd Floor of the Rolls Building to speak with a barrister on "
                          + "Wednesday morning. You can also email in advance on "
                          + "admin@companyinsolvencyscheme.com.");

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
    void testCompaniesWindingUpChdDailyCauseListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/"
                                     + "companiesWindingUpChdDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH, LIST_TYPE_METADATA,
                                              COMPANIES_WINDING_UP_CHD_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Dirwyn Cwmnïau i Ben (Adran Siawnsri)");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Dirwyn Cwmnïau i Ben (Adran Siawnsri)");

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
            .isEqualTo("Rhestr ar gyfer 23 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 22 April 2025 am 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TITLE_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_1).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Cynllun Pro Bono Ansolfedd Cwmni");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Mae'r Cynllun Pro Bono Ansolfedd Cwmnïau yn "
                          + "darparu cyngor cyfreithiol a / neu gynrychiolaeth "
                          + "am ddim yn y llys ar ddydd Mercher. Os oes gennych "
                          + "achos wedi'i restru yn y llys dirwyn i ben, dewch i "
                          + "Ystafell Ymgynghori 17 ar 2il Lawr Adeilad y Rolls i "
                          + "siarad â bargyfreithiwr fore Mercher. Gallwch hefyd "
                          + "anfon e-bost at admin@companyinsolvencyscheme.com ymlaen llaw.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Barnwr",
                "Amser",
                "Leoliad",
                "Math",
                "Rhif yr achos",
                "Enw’r achos",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }
}
