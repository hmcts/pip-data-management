package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles(profiles = "test")
class PublicationTest {

    @Autowired
    BlobContainerClient blobContainerClient;

    @Autowired
    BlobClient blobClient;

    @Autowired
    private MockMvc mockMvc;

    private static final String URL = "/publication";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String PROVENANCE = "provenance";
    private static final String PAYLOAD = "payload";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final String TEST_VALUE = "test";
    private static final String PAYLOAD_URL = "https://localhost";

    private static final String VALIDATION_EMPTY_RESPONSE = "Response should contain a Artefact";

    private static MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .content(PAYLOAD)
            .contentType(MediaType.APPLICATION_JSON);
    }

    @DisplayName("Should create a valid artefact and return the created artefact to the user")
    @Test
    void creationOfAValidArtefact() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);

        Artefact artefact = objectMapper.readValue(
            response.getResponse().getContentAsString(), Artefact.class);

        assertNotNull(artefact.getArtefactId(), "Artefact ID is not populated");
        assertEquals(artefact.getSourceArtefactId(), SOURCE_ARTEFACT_ID, "Source artefact ID "
            + "does not match input source artefact id");
        assertEquals(artefact.getDisplayFrom(), DISPLAY_FROM, "Display from does not match input display from");
        assertEquals(artefact.getDisplayTo(), DISPLAY_TO, "Display to does not match input display to");
        assertEquals(artefact.getProvenance(), PROVENANCE, "Provenance does not match input provenance");
        assertEquals(artefact.getLanguage(), LANGUAGE, "Language does not match input language");
        assertEquals(artefact.getSensitivity(), SENSITIVITY, "Sensitivity does not match input sensitivity");
        assertEquals(artefact.getPayload(), PAYLOAD_URL + "/" + SOURCE_ARTEFACT_ID + '-' + PROVENANCE,
                     "Payload does not match input payload");
    }

    @DisplayName("Should return a 400 Bad Request if the artifact type header is missing")
    @Test
    void testMissingArtifactType() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .content(PAYLOAD)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if an invalid artifact type is provided")
    @Test
    void testInvalidArtifactType() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(PublicationConfiguration.TYPE_HEADER, TEST_VALUE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .content(PAYLOAD)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), "Response should contain a string");
    }

    @DisplayName("Should return a 400 Bad Request if the provenance header is missing")
    @Test
    void testMissingProvenance() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .content(PAYLOAD)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if the source artifact id is missing")
    @Test
    void testMissingSourceArtifactId() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .content(PAYLOAD)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if an invalid display to is provided")
    @Test
    void testInvalidDisplayTo() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, TEST_VALUE)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .content(PAYLOAD)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if an invalid display from is provided")
    @Test
    void testInvalidDisplayFrom() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, TEST_VALUE)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .content(PAYLOAD)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
    }

    @DisplayName("Should return a 400 Bad Request if an invalid language is provided")
    @Test
    void testInvalidLanguageHeader() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LANGUAGE_HEADER, TEST_VALUE)
            .content(PAYLOAD)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
            .andExpect(status().isBadRequest()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATION_EMPTY_RESPONSE);
    }

    @DisplayName("Should update the artefact and return the same Artefact ID as the originally created one")
    @Test
    void updatingOfAnArtefactThatAlreadyExists() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(PAYLOAD_URL);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .content(PAYLOAD)
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult createResponse =
            mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        Artefact createdArtefact = objectMapper.readValue(createResponse.getResponse().getContentAsString(),
                                                          Artefact.class);

        mockHttpServletRequestBuilder.header(PublicationConfiguration.LANGUAGE_HEADER,
                                             Language.BI_LINGUAL);

        MvcResult updatedResponse = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(
            status().isOk()).andReturn();

        Artefact updatedArtefact = objectMapper.readValue(updatedResponse.getResponse().getContentAsString(),
                                                          Artefact.class);

        assertEquals(createdArtefact.getArtefactId(), updatedArtefact.getArtefactId(), "A new artefact has "
            + "been created rather than it being updated");

        assertEquals(Language.BI_LINGUAL, updatedArtefact.getLanguage(), "The updated artefact does "
            + "not contain the new language");
    }
}
