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

import static uk.gov.hmcts.reform.pip.model.publication.ListType.MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MayorAndCityCivilDailyCauseListFileConverterTest {
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
    private static final String ADDRESS_LINE3_ELEMENT = "address-line-3";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String LAST_UPDATED_DATE_ELEMENT = "last-updated-date";
    private static final String SUMMARY_TITLE_CLASS = "govuk-details__summary-text";
    private static final String SUMMARY_TEXT_CLASS = "govuk-details__text";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String VENUE_MESSAGE = "Venue does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String LAST_UPDATED_DATE_MESSAGE = "Last updated date does not match";
    private static final String IMPORTANT_INFORMATION_MESSAGE = "Important information heading does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode cstInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/mayorAndCityCivilDailyCauseList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            cstInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testMayorAndCityCivilDailyCauseListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/mayorAndCityCivilDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Civil Daily Cause List");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Civil Daily Cause List");

        softly.assertThat(document.getElementById(VENUE_NAME_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Mayor & City");

        softly.assertThat(document.getElementById(ADDRESS_LINE1_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Guildhall Buildings");

        softly.assertThat(document.getElementById(ADDRESS_LINE2_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Basinghall Street");

        softly.assertThat(document.getElementById(ADDRESS_LINE3_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("London EC2V 5AR");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("List for 23 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Last updated 22 April 2025 at 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TITLE_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Important information");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Central London County Court and Mayors & City of London Court – Hearings for the "
                          + "County Court at Central London will be heard at the Thomas More Building, "
                          + "located in the Royal Courts of Justice. Cases are also listed at Mayors &"
                          + " City of London Court, Guildhall Buildings, Basinghall Street, London, "
                          + "EC2V 5AR. Due to judicial availability, cases may be moved between the "
                          + "County Court at Central London and Mayors & City of London Court. "
                          + "Please ensure you check the cause lists for Central London County "
                          + "Court and Mayors and City of London Court the day prior to your hearing. "
                          + "Lists aim to be published by 1700.");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Requests for the media and others, including legal bloggers, should be made to "
                          + "County Court at Central London via "
                          + "enquiries.centrallondon.countycourt@justice.gov.uk "
                          + "or Mayors and City of London Court via "
                          + "enquiries.centrallondon.countycourt@justice.gov.uk. "
                          + "Arrangements will then be made for you to attend. When considering the use of "
                          + "telephone and video technology the judiciary will have regard to the "
                          + "principles of open justice. The Court may exclude observers where "
                          + "necessary to secure the proper administration of Justice.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Venue",
                "Judge",
                "Time",
                "Case number",
                "Case details",
                "Hearing type",
                "Additional information"
            );

        softly.assertAll();
    }

    @Test
    void testMayorAndCityCivilDailyCauseListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/mayorAndCityCivilDailyCauseList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, MAYOR_AND_CITY_CIVIL_DAILY_CAUSE_LIST.name(),
                                              LAST_RECEIVED_DATE_METADATA, LAST_RECEIVED_DATE
        );

        String result = converter.convert(cstInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol y Llys Sifil");

        softly.assertThat(document.getElementById(HEADER_ELEMENT).text())
            .as(HEADER_MESSAGE)
            .isEqualTo("Rhestr Achosion Dyddiol y Llys Sifil");

        softly.assertThat(document.getElementById(VENUE_NAME_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Mayor & City");

        softly.assertThat(document.getElementById(ADDRESS_LINE1_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Guildhall Buildings");

        softly.assertThat(document.getElementById(ADDRESS_LINE2_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("Basinghall Street");

        softly.assertThat(document.getElementById(ADDRESS_LINE3_ELEMENT).text())
            .as(VENUE_MESSAGE)
            .isEqualTo("London EC2V 5AR");

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT).text())
            .as(LIST_DATE_MESSAGE)
            .isEqualTo("Rhestr ar gyfer 23 April 2025");

        softly.assertThat(document.getElementById(LAST_UPDATED_DATE_ELEMENT).text())
            .as(LAST_UPDATED_DATE_MESSAGE)
            .isEqualTo("Diweddarwyd ddiwethaf 22 April 2025 am 10:30am");

        softly.assertThat(document.getElementsByClass(SUMMARY_TITLE_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .isEqualTo("Gwybodaeth bwysig");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Llys Sirol Canol Llundain a Llys Maer a Dinas Llundain – Bydd gwrandawiadau ar "
                          + "gyfer Llys Sirol Canol Llundain yn cael eu gwrando yn Adeilad Thomas More, "
                          + "sydd wedi’i leoli yn y Llysoedd Barn Brenhinol. Rhestrir achosion hefyd yn "
                          + "Llys Maer a Dinas Llundain, Adeiladau’r Guildhall, "
                          + "Stryd Basinghall, Llundain, EC2V 5AR. Oherwydd argaeledd barnwrol, gellir symud "
                          + "achosion rhwng y Llys Sirol yng Nghanol Llundain a Llys Maer a Dinas Llundain. "
                          + "Gwnewch yn siŵr eich bod yn gwirio rhestrau achosion Llys Sirol Canol Llundain a "
                          + "Llys Maer a Dinas Llundain y diwrnod cyn eich gwrandawiad. "
                          + "Anelir at gyhoeddi rhestrau erbyn 5pm.");

        softly.assertThat(document.getElementsByClass(SUMMARY_TEXT_CLASS).get(0).text())
            .as(IMPORTANT_INFORMATION_MESSAGE)
            .contains("Dylid gwneud ceisiadau ar gyfer y cyfryngau ac eraill, gan gynnwys blogwyr cyfreithiol, "
                          + "i Lys Sirol Canol Llundain drwy enquiries.centrallondon.countycourt@justice.gov.uk "
                          + "neu Lys Maer a Dinas Llundain drwy enquiries.centrallondon.countycourt@justice.gov.uk. "
                          + "Yna gwneir trefniadau i chi fynychu. Wrth ystyried y defnydd o dechnoleg ffôn a fideo, "
                          + "bydd y farnwriaeth yn ystyried egwyddorion cyfiawnder agored. "
                          + "Gall y llys eithrio arsylwyr lle bo angen i sicrhau gweinyddiaeth briodol cyfiawnder.");

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(7)
            .extracting(Element::text)
            .containsExactly(
                "Lleoliad",
                "Barnwr",
                "Amser",
                "Rhif yr achos",
                "Manylion yr achos",
                "Math o wrandawiad",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }
}
