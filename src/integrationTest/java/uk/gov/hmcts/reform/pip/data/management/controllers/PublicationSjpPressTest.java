package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.config.PublicationConfiguration;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ListType;

import java.io.InputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles(profiles = "test")
@RunWith(SpringRunner.class)
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
public class PublicationSjpPressTest {

    @Autowired
    BlobContainerClient blobContainerClient;

    @Autowired
    BlobClient blobClient;

    @Autowired
    private MockMvc mockMvc;

    private static final String BLOB_PAYLOAD_URL = "https://localhost";
    private static final String POST_URL = "/publication";

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @DisplayName("Should create a valid artefact and return the created artefact to the user")
    @Test
    void testCreationOfValidSjpPressList() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/sjp-press-list/sjpPressList.json")) {

            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(POST_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
                .header(PublicationConfiguration.PROVENANCE_HEADER, "Provenance")
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, "12345")
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, LocalDateTime.now())
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, LocalDateTime.now().plusMonths(1))
                .header(PublicationConfiguration.COURT_ID, 1)
                .header(PublicationConfiguration.LIST_TYPE, ListType.SJP_PRESS_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, LocalDateTime.now())
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);


            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated()).andReturn();

            assertNotNull(response.getResponse().getContentAsString(), "Response should contain a Artefact");

            Artefact artefact = objectMapper.readValue(
                response.getResponse().getContentAsString(), Artefact.class);

            assertNotNull(artefact.getArtefactId(), "Artefact ID is not populated");
        }
    }

    @DisplayName("Should return an error message back to the user when creating an invalid blob")
    @Test
    void testCreationOfInvalidSjpPressList() throws Exception {
        when(blobContainerClient.getBlobClient(any())).thenReturn(blobClient);
        when(blobContainerClient.getBlobContainerUrl()).thenReturn(BLOB_PAYLOAD_URL);

        try (InputStream mockFile = this.getClass().getClassLoader()
            .getResourceAsStream("data/sjp-press-list/sjpPressListInvalid.json")) {

            MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(POST_URL)
                .header(PublicationConfiguration.TYPE_HEADER, ArtefactType.LIST)
                .header(PublicationConfiguration.PROVENANCE_HEADER, "Provenance")
                .header(PublicationConfiguration.SOURCE_ARTEFACT_ID_HEADER, "12345")
                .header(PublicationConfiguration.DISPLAY_FROM_HEADER, LocalDateTime.now())
                .header(PublicationConfiguration.DISPLAY_TO_HEADER, LocalDateTime.now().plusMonths(1))
                .header(PublicationConfiguration.COURT_ID, 1)
                .header(PublicationConfiguration.LIST_TYPE, ListType.SJP_PRESS_LIST)
                .header(PublicationConfiguration.CONTENT_DATE, LocalDateTime.now())
                .content(mockFile.readAllBytes())
                .contentType(MediaType.APPLICATION_JSON);


            MvcResult response = mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isBadRequest()).andReturn();

            assertNotNull(response.getResponse().getContentAsString(), "Response should contain a Artefact");

            ExceptionResponse exceptionResponse = objectMapper.readValue(
                response.getResponse().getContentAsString(),
                ExceptionResponse.class
            );

            assertTrue(
                exceptionResponse.getMessage().contains("individualSurname"),
                "Individual Surname is not displayed in the exception response"
            );
        }
    }

}
