package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.account.PiUser;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.pip.model.account.Roles.SYSTEM_ADMIN;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = { "APPROLE_api.request.admin" })
class RegexValidationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    private static final String POST_URL = "/publication";
    private static final String TEST_CONTENT_MESSAGE = "Response is not populated";
    private static final String TEST_ARTEFACT_ID_MESSAGE = "Artefact ID is not populated";

    private static final String SYSTEM_ADMIN_ID = UUID.randomUUID().toString();

    private static ObjectMapper objectMapper;
    private static PiUser piUser;

    private MockHttpServletRequestBuilder createMockServletRequestBuilder(InputStream mockFile) throws IOException {
        return MockMvcRequestBuilders
            .post(POST_URL)
            .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
            .header(PublicationConfiguration.PROVENANCE_HEADER, "Provenance")
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, "12345")
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, LocalDateTime.now())
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, LocalDateTime.now().plusMonths(1))
            .header(PublicationConfiguration.COURT_ID, 1)
            .header(PublicationConfiguration.LIST_TYPE, ListType.SJP_PUBLIC_LIST)
            .header(PublicationConfiguration.LANGUAGE_HEADER, Language.ENGLISH)
            .header(PublicationConfiguration.REQUESTER_ID_HEADER, SYSTEM_ADMIN_ID)
            .header(PublicationConfiguration.CONTENT_DATE, LocalDateTime.now())
            .content(mockFile.readAllBytes())
            .contentType(MediaType.APPLICATION_JSON);
    }

    @BeforeAll
    public static void setup() {
        piUser = new PiUser();
        piUser.setUserId(SYSTEM_ADMIN_ID);
        piUser.setEmail("test@justice.gov.uk");
        piUser.setRoles(SYSTEM_ADMIN);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @DisplayName("Should throw a validation error due to containing a tag with content")
    @Test
    void testBadRequestIsThrownWhenHtmlTagIsPresent() throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/regex-testing/html-tag-populated.json")) {

            when(accountManagementService.getUserById(any())).thenReturn(piUser);
            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = createMockServletRequestBuilder(mockFile);
            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isBadRequest()).andReturn();

            assertNotNull(response.getResponse().getContentAsString(), TEST_CONTENT_MESSAGE);

            ExceptionResponse exceptionResponse = objectMapper.readValue(
                response.getResponse().getContentAsString(),
                ExceptionResponse.class
            );

            assertEquals(
                exceptionResponse.getMessage(), "$.document.documentName: does not match the regex"
                                                            + " pattern ^(?!(.|\\r|\\n)*<[^>]+>)(.|\\r|\\n)*$",
                "Publication date is not displayed in the exception response"
            );
        }
    }

    @DisplayName("Should not throw a validation error if only a < is present")
    @Test
    void testBadRequestIsNotThrownWhenOnlyTheStartOf() throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/regex-testing/html-tag-only-start.json")) {

            when(accountManagementService.getUserById(any())).thenReturn(piUser);
            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = createMockServletRequestBuilder(mockFile);
            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            assertNotNull(response.getResponse().getContentAsString(), TEST_CONTENT_MESSAGE);

            Artefact artefact = objectMapper.readValue(
                response.getResponse().getContentAsString(), Artefact.class);

            assertNotNull(artefact.getArtefactId(), TEST_ARTEFACT_ID_MESSAGE);
        }
    }

    @DisplayName("Should not throw a validation error if only a > is present")
    @Test
    void testBadRequestIsNotThrownWhenOnlyTheEndOf() throws Exception {
        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/regex-testing/html-tag-only-end.json")) {

            when(accountManagementService.getUserById(any())).thenReturn(piUser);
            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = createMockServletRequestBuilder(mockFile);
            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            assertNotNull(response.getResponse().getContentAsString(), TEST_CONTENT_MESSAGE);

            Artefact artefact = objectMapper.readValue(
                response.getResponse().getContentAsString(), Artefact.class);

            assertNotNull(artefact.getArtefactId(), TEST_ARTEFACT_ID_MESSAGE);
        }
    }

}
