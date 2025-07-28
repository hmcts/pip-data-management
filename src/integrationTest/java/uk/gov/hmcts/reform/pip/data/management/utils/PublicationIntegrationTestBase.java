package uk.gov.hmcts.reform.pip.data.management.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PublicationIntegrationTestBase extends IntegrationTestBase {
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PUBLICATION_URL = "/publication";

    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final String PROVENANCE = "MANUAL_UPLOAD";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String COURT_ID = "1";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay()
        .truncatedTo(ChronoUnit.SECONDS);
    private static final String SYSTEM_ADMIN_ID = UUID.randomUUID().toString();

    private static PiUser piUser;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeAll
    public void startup() {
        piUser = new PiUser();
        piUser.setUserId(SYSTEM_ADMIN_ID);
        piUser.setEmail("test@justice.gov.uk");
        piUser.setRoles(SYSTEM_ADMIN);

        OBJECT_MAPPER.findAndRegisterModules();
    }

    protected Artefact createDailyList(Sensitivity sensitivity) throws Exception {
        return createDailyList(sensitivity, DISPLAY_FROM.minusMonths(2), CONTENT_DATE);
    }

    protected Artefact createDailyList(Sensitivity sensitivity, LocalDateTime displayFrom, LocalDateTime contentDate)
        throws Exception {
        return createDailyList(sensitivity, displayFrom, DISPLAY_TO, contentDate, PROVENANCE, COURT_ID);
    }

    protected Artefact createDailyList(Sensitivity sensitivity, LocalDateTime displayFrom, LocalDateTime displayTo,
                                       LocalDateTime contentDate, String provenance, String courtId) throws Exception {
        when(accountManagementService.getUserById(any())).thenReturn(piUser);

        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/civil-daily-cause-list/civilDailyCauseList.json")) {

            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(PUBLICATION_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
                .header(PublicationConfiguration.PROVENANCE_HEADER, provenance)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, displayFrom)
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, displayTo.plusMonths(1))
                .header(PublicationConfiguration.COURT_ID, courtId)
                .header(PublicationConfiguration.LIST_TYPE, ListType.CIVIL_DAILY_CAUSE_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, contentDate)
                .header(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity)
                .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
                .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);

            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            return OBJECT_MAPPER.readValue(
                response.getResponse().getContentAsString(), Artefact.class);
        }
    }

    protected Artefact createSscsDailyList(Sensitivity sensitivity, String provenance) throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/sscs-daily-list/sscsDailyList.json")) {
            when(accountManagementService.getUserById(any())).thenReturn(piUser);
            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(PUBLICATION_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
                .header(PublicationConfiguration.PROVENANCE_HEADER, provenance)
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM.minusMonths(2))
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO.plusMonths(1))
                .header(PublicationConfiguration.COURT_ID, COURT_ID)
                .header(PublicationConfiguration.LIST_TYPE, ListType.SSCS_DAILY_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, CONTENT_DATE)
                .header(PublicationConfiguration.SENSITIVITY_HEADER, sensitivity)
                .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
                .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);

            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            return OBJECT_MAPPER.readValue(
                response.getResponse().getContentAsString(), Artefact.class);
        }
    }

    protected boolean compareArtefacts(Artefact expectedArtefact, Artefact returnedArtefact) {
        return expectedArtefact.getArtefactId().equals(returnedArtefact.getArtefactId())
            && expectedArtefact.getProvenance().equals(returnedArtefact.getProvenance())
            && expectedArtefact.getSensitivity().equals(returnedArtefact.getSensitivity())
            && expectedArtefact.getPayload().equals(returnedArtefact.getPayload())
            && expectedArtefact.getType().equals(returnedArtefact.getType())
            && expectedArtefact.getSearch().equals(returnedArtefact.getSearch())
            && expectedArtefact.getLocationId().equals(returnedArtefact.getLocationId())
            && expectedArtefact.getLanguage().equals(returnedArtefact.getLanguage())
            && expectedArtefact.getListType().equals(returnedArtefact.getListType())
            && expectedArtefact.getDisplayTo().equals(returnedArtefact.getDisplayTo())
            && expectedArtefact.getDisplayFrom().equals(returnedArtefact.getDisplayFrom())
            && expectedArtefact.getContentDate().equals(returnedArtefact.getContentDate())
            && expectedArtefact.getIsFlatFile().equals(returnedArtefact.getIsFlatFile())
            && expectedArtefact.getSourceArtefactId().equals(returnedArtefact.getSourceArtefactId());
    }

}
