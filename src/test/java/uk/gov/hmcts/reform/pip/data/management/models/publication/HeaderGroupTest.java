package uk.gov.hmcts.reform.pip.data.management.models.publication;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeaderGroupTest {

    private static final String TEST_PROVENANCE = "Test Provenance";
    private static final String TEST_SOURCE_ARTEFACT_ID = "Test Artefact ID";
    private static final ArtefactType TEST_TYPE = ArtefactType.LIST;
    private static final Sensitivity TEST_SENSITIVITY = Sensitivity.PUBLIC;
    private static final Language TEST_LANGUAGE = Language.ENGLISH;
    private static final LocalDateTime TEST_CONTENT_DATE = LocalDateTime.now();
    private static final LocalDateTime TEST_DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime TEST_DISPLAY_TO = LocalDateTime.now();
    private static final String TEST_COURT_ID = "1234D";
    private static final ListType TEST_LIST_TYPE = ListType.CIVIL_DAILY_CAUSE_LIST;

    @Test
    void testCreateHeaderGroup() {
        HeaderGroup headerGroup = new HeaderGroup(TEST_PROVENANCE,
                                                  TEST_SOURCE_ARTEFACT_ID,
                                                  TEST_TYPE,
                                                  TEST_SENSITIVITY,
                                                  TEST_LANGUAGE,
                                                  TEST_DISPLAY_FROM,
                                                  TEST_DISPLAY_TO,
                                                  TEST_LIST_TYPE,
                                                  TEST_COURT_ID,
                                                  TEST_CONTENT_DATE);

        Map<String, String> appInsightsMap = headerGroup.getAppInsightsHeaderMap();

        assertEquals(TEST_PROVENANCE, appInsightsMap.get("PROVENANCE"), "Provenance does not match");
        assertEquals(TEST_SOURCE_ARTEFACT_ID, appInsightsMap.get("SOURCE_ARTEFACT_ID"),
                     "Source Artefact ID does not match");
        assertEquals(TEST_TYPE.toString(), appInsightsMap.get("TYPE"),
                     "Type does not match");
        assertEquals(TEST_SENSITIVITY.toString(), appInsightsMap.get("SENSITIVITY"),
                     "Sensitivity does not match");
        assertEquals(TEST_LANGUAGE.toString(), appInsightsMap.get("LANGUAGE"),
                     "Language does not match");
        assertEquals(TEST_DISPLAY_FROM.toString(), appInsightsMap.get("DISPLAY_FROM"),
                     "Display From does not match");
        assertEquals(TEST_DISPLAY_TO.toString(), appInsightsMap.get("DISPLAY_TO"),
                     "Display To does not match");
        assertEquals(TEST_LIST_TYPE.toString(), appInsightsMap.get("LIST_TYPE"),
                     "List Type does not match");
        assertEquals(TEST_COURT_ID, appInsightsMap.get("COURT_ID"),
                     "Court ID does not match");
        assertEquals(TEST_CONTENT_DATE.toString(), appInsightsMap.get("CONTENT_DATE"),
                     "Content Date does not match");
    }

}
