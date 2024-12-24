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
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.NonStrategicListFileConverter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.pip.model.publication.ListType.RPT_EASTERN_WEEKLY_HEARING_LIST;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RptEasternWeeklyHearingListFileConverterTest {

    private static final String CONTENT_DATE = "12 December 2024";
    private static final String PROVENANCE = "provenance";
    private static final String CONTENT_DATE_METADATA = "contentDate";
    private static final String PROVENANCE_METADATA = "provenance";
    private static final String LANGUAGE_METADATA = "language";
    private static final String LIST_TYPE_METADATA = "listType";

    private static final String ENGLISH = "ENGLISH";
    private static final String WELSH = "WELSH";

    private static final String LIST_NAME = RPT_EASTERN_WEEKLY_HEARING_LIST.name();
    private static final String LIST_ENGLISH_NAME = "Residential Property Tribunal: Eastern region Weekly Hearing List";
    private static final String LIST_DATE_ENGLISH = "List for 12 December 2024";
    private static final String LIST_DATE_WELSH = "Rhestr ar gyfer 12 December 2024";
    private static final String OBSERVE_HEARING_ENGLISH = "Observe a court or tribunal hearing as a journalist, "
        + "researcher or member of the public";
    private static final String OBSERVE_HEARING_WELSH = "Arsylwi gwrandawiad llys neu dribiwnlys fel newyddiadurwr, "
        + "ymchwilydd neu aelod o'r cyhoedd";
    private static final String LIST_WELSH_NAME = "Tribiwnlys Eiddo Preswyl: Rhestr o Wrandawiadau "
        + "Wythnosol rhanbarth Dwyrain Lloegr";

    private static final String HEADER_ELEMENT = "page-heading";
    private static final String LIST_DATE_ELEMENT = "list-date";
    private static final String CONTACT_MESSAGE_ELEMENT = "contact-message";
    private static final String OBSERVE_HEARING_ELEMENT =  "observe-hearing";

    private static final String TITLE_MESSAGE = "Title does not match";
    private static final String HEADER_MESSAGE = "Header does not match";
    private static final String LIST_DATE_MESSAGE = "List date does not match";
    private static final String BODY_MESSAGE = "Body does not match";
    private static final String TABLE_HEADERS_MESSAGE = "Table headers does not match";

    private final NonStrategicListFileConverter converter = new NonStrategicListFileConverter();

    private JsonNode listInputJson;

    @BeforeAll
    void setup() throws IOException {
        try (InputStream inputStream = getClass()
            .getResourceAsStream("/mocks/non-strategic/fftResidentialPropertyTribunalWeeklyHearingList.json")) {
            String inputRaw = IOUtils.toString(inputStream, Charset.defaultCharset());
            listInputJson = new ObjectMapper().readTree(inputRaw);
        }
    }

    @Test
    void testRptEasternWeeklyHearingListFileConversionInEnglish() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/en/non-strategic/rptEasternWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, ENGLISH,
                                              LIST_TYPE_METADATA, LIST_NAME
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo(LIST_ENGLISH_NAME);

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_ENGLISH_NAME);

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_ENGLISH);

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Members of the public wishing to observe a hearing or representatives "
                           + "of the media may, on their request, join any telephone or video hearing "
                           + "remotely while they are taking place by sending an email in advance to "
                           + "the tribunal at [insert office email] with the following details in the "
                           + "subject line “[OBSERVER/MEDIA] REQUEST – [case reference] – [hearing date]” "
                           + "and appropriate arrangements will be made to allow access where reasonably practicable.");

        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_ENGLISH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(9)
            .extracting(Element::text)
            .containsExactly(
                "Date",
                "Time",
                "Venue",
                "Case Type",
                "Case reference number",
                "Judge(s)",
                "Member(s)",
                "Hearing Method",
                "Additional information"
            );

        softly.assertAll();
    }

    @Test
    void testRptEasternWeeklyHearingListFileConversionInWelsh() throws IOException {
        Map<String, Object> languageResource;
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("templates/languages/cy/non-strategic/rptEasternWeeklyHearingList.json")) {
            languageResource = new ObjectMapper().readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }

        Map<String, String> metadata = Map.of(CONTENT_DATE_METADATA, CONTENT_DATE,
                                              PROVENANCE_METADATA, PROVENANCE,
                                              LANGUAGE_METADATA, WELSH,
                                              LIST_TYPE_METADATA, LIST_NAME
        );

        String result = converter.convert(listInputJson, metadata, languageResource);
        Document document = Jsoup.parse(result);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(document.title())
            .as(TITLE_MESSAGE)
            .isEqualTo(LIST_WELSH_NAME);

        softly.assertThat(document.getElementById(HEADER_ELEMENT))
            .as(HEADER_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_WELSH_NAME);

        softly.assertThat(document.getElementById(LIST_DATE_ELEMENT))
            .as(LIST_DATE_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(LIST_DATE_WELSH);

        softly.assertThat(document.getElementById(CONTACT_MESSAGE_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo("Gall aelodau o’r cyhoedd sy’n dymuno arsylwi gwrandawiad neu "
                           + "gynrychiolwyr y cyfryngau ymuno ag unrhyw wrandawiad dros y ffôn "
                           + "neu drwy fideo o bell ar gais tra’u bod yn cael eu cynnal drwy anfon "
                           + "e-bost ymlaen llaw at y tribiwnlys yn [insert office email] gyda’r "
                           + "manylion canlynol yn y llinell bwnc “CAIS [ARSYLLWR/CYFRYNGAU] – "
                           + "[cyfeirnod yr achos] – [dyddiad y gwrandawiad] (angen cynnwys unrhyw "
                           + "wybodaeth arall sy’n ofynnol gan y tribiwnlys)” a gwneir trefniadau priodol i "
                           + "ganiatáu mynediad lle bo hynny’n rhesymol ymarferol.");


        softly.assertThat(document.getElementById(OBSERVE_HEARING_ELEMENT))
            .as(BODY_MESSAGE)
            .extracting(Element::text)
            .isEqualTo(OBSERVE_HEARING_WELSH);

        softly.assertThat(document.getElementsByTag("th"))
            .as(TABLE_HEADERS_MESSAGE)
            .hasSize(9)
            .extracting(Element::text)
            .containsExactly(
                "Dyddiad",
                "Amser",
                "Lleoliad",
                "Case Type",
                "Cyfeirnod yr achos",
                "Barnwr/Barnwyr",
                "Aelod(au)",
                "Math o wrandawiad",
                "Gwybodaeth ychwanegol"
            );

        softly.assertAll();
    }
}
