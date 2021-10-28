package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.implementation.ModelHelper;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final String ARTEFACT_ID = "1234";
    private static final ArtefactType ARTEFACT_TYPE = ArtefactType.LIST;
    private static final Sensitivity SENSITIVITY = Sensitivity.PUBLIC;
    private static final String PROVENANCE = "provenance";
    private static final String PAYLOAD = "payload";
    private static final String SEARCH = "search";
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final Language LANGUAGE = Language.ENGLISH;

    private static final String HEADER_ARTEFACT_ID = "x-artefact-id";
    private static final String HEADER_SOURCE_ARTEFACT_ID = "x-source-artefact-id";
    private static final String HEADER_DISPLAY_FROM = "x-display-from";
    private static final String HEADER_DISPLAY_TO = "x-display-to";
    private static final String HEADER_LANGUAGE = "x-language";
    private static final String HEADER_PROVENANCE = "x-provenance";
    private static final String HEADER_SEARCH = "x-search";
    private static final String HEADER_SENSITIVITY = "x-sensitivity";
    private static final String HEADER_ARTEFACT_TYPE = "x-type";

    private static final String TABLE_ARTEFACT_ID = "artefactId";
    private static final String TABLE_PROVENANCE = "provenance";
    private static final String TABLE_SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final String TABLE_TYPE = "type";
    private static final String TABLE_SENSITIVITY = "sensitivity";
    private static final String TABLE_LANGUAGE = "language";
    private static final String TABLE_SEARCH = "search";
    private static final String TABLE_DISPLAY_FROM = "displayFrom";
    private static final String TABLE_DISPLAY_TO = "displayTo";
    private static final String TABLE_PAYLOAD = "payload";

    @DisplayName("Should create a valid artefact and return the UUID to the user")
    @Test
    void creationOfAValidArtefact() throws Exception {
        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of());

        doNothing().when(tableClient).createEntity(any());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(HEADER_ARTEFACT_ID, ARTEFACT_ID)
            .header(HEADER_ARTEFACT_TYPE, ARTEFACT_TYPE)
            .header(HEADER_SENSITIVITY, SENSITIVITY)
            .header(HEADER_PROVENANCE, PROVENANCE)
            .header(HEADER_SEARCH, SEARCH)
            .header(HEADER_SOURCE_ARTEFACT_ID, SOURCE_ARTEFACT_ID)
            .header(HEADER_DISPLAY_TO, DISPLAY_TO)
            .header(HEADER_DISPLAY_FROM, DISPLAY_FROM)
            .header(HEADER_LANGUAGE, LANGUAGE)
            .content(objectMapper.writeValueAsString(PAYLOAD))
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), "Response should contain a string");
    }

    @DisplayName("Should return an error response when failing to connect with Azure")
    @Test
    void creationOfAnArtefactWhenErrorConnectingWithAzure() throws Exception {
        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of());

        doThrow(new TableServiceException(EXCEPTION_MESSAGE, null)).when(tableClient).createEntity(any());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(HEADER_ARTEFACT_ID, ARTEFACT_ID)
            .header(HEADER_ARTEFACT_TYPE, ARTEFACT_TYPE)
            .header(HEADER_SENSITIVITY, SENSITIVITY)
            .header(HEADER_PROVENANCE, PROVENANCE)
            .header(HEADER_SEARCH, SEARCH)
            .header(HEADER_SOURCE_ARTEFACT_ID, SOURCE_ARTEFACT_ID)
            .header(HEADER_DISPLAY_FROM, DISPLAY_TO)
            .header(HEADER_DISPLAY_FROM, DISPLAY_FROM)
            .header(HEADER_LANGUAGE, LANGUAGE)
            .content(objectMapper.writeValueAsString(PAYLOAD))
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(
            status().isInternalServerError()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), "Response should be present");

        String errorResponse = response.getResponse().getContentAsString();
        ExceptionResponse exceptionResponse = objectMapper.readValue(errorResponse, ExceptionResponse.class);

        assertEquals(EXCEPTION_MESSAGE, exceptionResponse.getMessage(), "Response should contain the correct message");
    }

    @DisplayName("Should return an error response when the artefact already exists")
    @Test
    void creationOfAnArtefactThatAlreadyExists() throws Exception {

        Map<String, Object> entityProperties = new ConcurrentHashMap<>();
        entityProperties.put(TABLE_ARTEFACT_ID, ARTEFACT_ID);
        entityProperties.put(TABLE_PROVENANCE, PROVENANCE);
        entityProperties.put(TABLE_SOURCE_ARTEFACT_ID, SOURCE_ARTEFACT_ID);
        entityProperties.put(TABLE_TYPE, ARTEFACT_TYPE);
        entityProperties.put(TABLE_PAYLOAD, PAYLOAD);
        entityProperties.put(TABLE_SENSITIVITY, SENSITIVITY);
        entityProperties.put(TABLE_LANGUAGE, LANGUAGE);
        entityProperties.put(TABLE_SEARCH, SEARCH);
        entityProperties.put(TABLE_DISPLAY_FROM, DISPLAY_FROM);
        entityProperties.put(TABLE_DISPLAY_TO, DISPLAY_TO);

        TableEntity tableEntity = ModelHelper.createEntity(entityProperties);

        when(tableClient.listEntities(any(), any(), any())).thenReturn(tableEntities);
        when(tableEntities.stream()).thenReturn(Stream.of(tableEntity));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
            .put(URL)
            .header(HEADER_ARTEFACT_ID, ARTEFACT_ID)
            .header(HEADER_ARTEFACT_TYPE, ARTEFACT_TYPE)
            .header(HEADER_SENSITIVITY, SENSITIVITY)
            .header(HEADER_PROVENANCE, PROVENANCE)
            .header(HEADER_SEARCH, SEARCH)
            .header(HEADER_SOURCE_ARTEFACT_ID, SOURCE_ARTEFACT_ID)
            .header(HEADER_DISPLAY_FROM, DISPLAY_TO)
            .header(HEADER_DISPLAY_FROM, DISPLAY_FROM)
            .header(HEADER_LANGUAGE, LANGUAGE)
            .content(objectMapper.writeValueAsString(PAYLOAD))
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(
            status().isBadRequest()).andReturn();

        assertNotNull(response.getResponse().getContentAsString(), "Response should be present");

        String errorResponse = response.getResponse().getContentAsString();
        ExceptionResponse exceptionResponse = objectMapper.readValue(errorResponse, ExceptionResponse.class);

        assertEquals(String.format("Duplicate publication found with ID %s", ARTEFACT_ID),
                     exceptionResponse.getMessage(), "Response should contain the correct error message");
    }
}
