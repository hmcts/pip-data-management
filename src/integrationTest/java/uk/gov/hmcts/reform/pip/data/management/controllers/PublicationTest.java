package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.implementation.ModelHelper;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.AzureConfigurationClientTest;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {AzureConfigurationClientTest.class, Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
class PublicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    TableClient tableClient;

    @Autowired
    PagedIterable<TableEntity> tableEntities;

    private static final String URL = "/publication";
    private static final String EXCEPTION_MESSAGE = "Error communicating with Azure";
    private static final String EXCEPTION_MESSAGE_CREATE = "Server error while creating a publication in Azure";
    private static final String EXCEPTION_MESSAGE_UPDATE = "Server error while updating a publication in Azure";
    private static final String EXCEPTION_MESSAGE_RETRIEVE = "Server error while retrieving publications from Azure";
    private static final String VALIDATE_RESPONSE_MESSAGE = "Response should be present";
    private static final String VALIDATE_RESPONSE_CONTENT = "Response should contain the correct error message";

    private static final String ARTEFACT_ID = "1234";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String PROVENANCE = "provenance";
    private static final String PAYLOAD = "payload";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;

    private static MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(PublicationConfiguration.TYPE_HEADER, ARTEFACT_TYPE)
            .header(PublicationConfiguration.SENSITIVITY_HEADER, SENSITIVITY)
            .header(PublicationConfiguration.PROVENANCE_HEADER, PROVENANCE)
            .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, SOURCE_ARTEFACT_ID)
            .header(PublicationConfiguration.DISPLAY_TO_HEADER, DISPLAY_TO)
            .header(PublicationConfiguration.DISPLAY_FROM_HEADER, DISPLAY_FROM)
            .header(PublicationConfiguration.LANGUAGE_HEADER, LANGUAGE)
            .content(objectMapper.writeValueAsString(PAYLOAD))
            .contentType(MediaType.APPLICATION_JSON);
    }

    @DisplayName("Should create a valid artefact and return the UUID to the user")
    @Test
    void creationOfAValidArtefact() throws Exception {
        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of());

        doNothing().when(tableClient).createEntity(any());

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), "Response should contain a string");
    }

    @DisplayName("Should return an error response due to failing to connect with Azure - Creation")
    @Test
    void creationOfAnArtefactWhenErrorConnectingWithAzure() throws Exception {
        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of());

        doThrow(new TableServiceException(EXCEPTION_MESSAGE, null)).when(tableClient).createEntity(any());

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(
            status().isInternalServerError()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATE_RESPONSE_MESSAGE);

        String errorResponse = response.getResponse().getContentAsString();
        ExceptionResponse exceptionResponse = objectMapper.readValue(errorResponse, ExceptionResponse.class);

        assertEquals(EXCEPTION_MESSAGE_CREATE, exceptionResponse.getMessage(), VALIDATE_RESPONSE_CONTENT);
    }

    @DisplayName("Should update the artefact and return the same Artefact ID as the originally created one")
    @Test
    void updatingOfAnArtefactThatAlreadyExists() throws Exception {

        Map<String, Object> entityProperties = new ConcurrentHashMap<>();
        entityProperties.put(PublicationConfiguration.ARTIFACT_ID_TABLE, ARTEFACT_ID);
        entityProperties.put(PublicationConfiguration.PROVENANCE_TABLE, PROVENANCE);
        entityProperties.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE, SOURCE_ARTEFACT_ID);
        entityProperties.put(PublicationConfiguration.TYPE_TABLE, ARTEFACT_TYPE);
        entityProperties.put(PublicationConfiguration.PAYLOAD_TABLE, PAYLOAD);
        entityProperties.put(PublicationConfiguration.SENSITIVITY_TABLE, SENSITIVITY);
        entityProperties.put(PublicationConfiguration.LANGUAGE_TABLE, LANGUAGE);
        entityProperties.put(PublicationConfiguration.DISPLAY_FROM_TABLE, DISPLAY_FROM);
        entityProperties.put(PublicationConfiguration.DISPLAY_TO_TABLE, DISPLAY_TO);

        TableEntity tableEntity = ModelHelper.createEntity(entityProperties);

        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of(tableEntity));

        doNothing().when(tableClient).updateEntity(any());

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(
            status().isOk()).andReturn();

        assertEquals(ARTEFACT_ID, response.getResponse().getContentAsString(), "The artefact ID being returned"
            + "should be the same as the existing artefact");
    }

    @DisplayName("Should return an error due to failing to connect to azure - Updating")
    @Test
    void updatingOfAnArtefactWhenErrorConnectingWithAzure() throws Exception {

        Map<String, Object> entityProperties = new ConcurrentHashMap<>();
        entityProperties.put(PublicationConfiguration.ARTIFACT_ID_TABLE, ARTEFACT_ID);
        entityProperties.put(PublicationConfiguration.PROVENANCE_TABLE, PROVENANCE);
        entityProperties.put(PublicationConfiguration.SOURCE_ARTEFACT_ID_TABLE, SOURCE_ARTEFACT_ID);
        entityProperties.put(PublicationConfiguration.TYPE_TABLE, ARTEFACT_TYPE);
        entityProperties.put(PublicationConfiguration.PAYLOAD_TABLE, PAYLOAD);
        entityProperties.put(PublicationConfiguration.SENSITIVITY_TABLE, SENSITIVITY);
        entityProperties.put(PublicationConfiguration.LANGUAGE_TABLE, LANGUAGE);
        entityProperties.put(PublicationConfiguration.DISPLAY_FROM_TABLE, DISPLAY_FROM);
        entityProperties.put(PublicationConfiguration.DISPLAY_TO_TABLE, DISPLAY_TO);

        TableEntity tableEntity = ModelHelper.createEntity(entityProperties);

        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of(tableEntity));

        doThrow(new TableServiceException(EXCEPTION_MESSAGE, null)).when(tableClient).updateEntity(any());

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(
            status().isInternalServerError()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATE_RESPONSE_MESSAGE);

        String errorResponse = response.getResponse().getContentAsString();
        ExceptionResponse exceptionResponse = objectMapper.readValue(errorResponse, ExceptionResponse.class);

        assertEquals(EXCEPTION_MESSAGE_UPDATE, exceptionResponse.getMessage(), VALIDATE_RESPONSE_CONTENT);
    }

    @DisplayName("Should return an error response when failing to connect with Azure")
    @Test
    void retrievingOfAnArtefactWhenErrorConnectingWithAzure() throws Exception {
        doThrow(new TableServiceException(EXCEPTION_MESSAGE, null)).when(tableClient).listEntities(any(), any(), any());

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(
            status().isInternalServerError()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), VALIDATE_RESPONSE_MESSAGE);

        String errorResponse = response.getResponse().getContentAsString();
        ExceptionResponse exceptionResponse = objectMapper.readValue(errorResponse, ExceptionResponse.class);

        assertEquals(EXCEPTION_MESSAGE_RETRIEVE, exceptionResponse.getMessage(), VALIDATE_RESPONSE_CONTENT);
        reset(tableClient);
    }

}
