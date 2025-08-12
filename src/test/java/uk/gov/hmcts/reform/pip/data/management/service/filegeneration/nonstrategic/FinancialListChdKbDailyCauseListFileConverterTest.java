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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FinancialListChdKbDailyCauseListFileConverterTest {
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

    private static final String IMPORTANT_INFORMATION_ELEMENT_1 = "important-information-line-1";
    private static final String IMPORTANT_INFORMATION_ELEMENT_2 = "important-information-line-2";

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
            .getResourceAsStream("/mocks/non-strategic/financialListChdKbDailyCauseList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testFinancialListChdKbDailyCauseListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/"
                                     + "financialListChdKbDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH, LIST_TYPE_METADATA,
                                              FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Financial List (Chancery Division/King’s Bench Division/Commercial Court) Daily Cause List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Financial List (Chancery Division/King’s Bench Division/Commercial Court) Daily Cause List");

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
            .isEqualTo("Remote hearings before a Judge of the Chancery Division");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("If a representative of the media or member of the public wishes "
                          + "to attend the hearing they should contact the listing "
                          + "office chanceryjudgeslisting@justice.gov.uk who will put "
                          + "them in touch with the relevant person.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Remote hearings before a Judge of the Commercial Court");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("The hearing will be available to representatives of the media "
                          + "upon request. It will be organised and conducted using "
                          + "MS Teams (unless otherwise stated). Any media representative "
                          + "(or any other member of the public) wishing to witness the "
                          + "hearing will need to do so over the internet and provide an "
                          + "email address at which to be sent an appropriate link for access. "
                          + "Please contact comct.listing@justice.gov.uk.");

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
    void testFinancialListChdKbDailyCauseListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/"
                                     + "financialListChdKbDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH, LIST_TYPE_METADATA,
                                              FINANCIAL_LIST_CHD_KB_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(inputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Ariannol (Adran Siawnsri /Adran Mainc y Brenin/Llys Masnach)");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol Ariannol (Adran Siawnsri /Adran Mainc y Brenin/Llys Masnach)");

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
            .isEqualTo("Gwrandawiadau o bell gerbron Barnwr yr Adran Siawnsri");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_1).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Os yw cynrychiolydd o'r cyfryngau neu aelod o'r cyhoedd yn "
                          + "dymuno mynychu'r gwrandawiad, dylent gysylltu â'r "
                          + "swyddfa restru yn chanceryjudgeslisting@justice.gov.uk a "
                          + "fydd yn eu rhoi mewn cysylltiad â'r unigolyn perthnasol.");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_HEADING_2).text())
            .as(BODY_MESSAGE)
            .isEqualTo("Gwrandawiadau o bell gerbron Barnwr y Llys Masnach");

        softly.assertThat(document.getElementById(IMPORTANT_INFORMATION_ELEMENT_2).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Bydd y gwrandawiad ar gael i gynrychiolwyr y cyfryngau ar "
                          + "gais. Bydd yn cael ei drefnu a'i gynnal gan ddefnyddio MS Teams "
                          + "(oni nodir yn wahanol). Bydd angen i unrhyw gynrychiolydd y "
                          + "cyfryngau (neu unrhyw aelod arall o'r cyhoedd) sy'n dymuno gweld y "
                          + "gwrandawiad wneud hynny dros y rhyngrwyd a darparu cyfeiriad e-bost "
                          + "i gael dolen briodol ar gyfer cael mynediad. Cysylltwch â "
                          + "comct.listing@justice.gov.uk.");

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
