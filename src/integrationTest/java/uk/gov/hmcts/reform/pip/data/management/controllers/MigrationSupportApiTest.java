package uk.gov.hmcts.reform.pip.data.management.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.pip.data.management.Application;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactSearchRepository;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.ExceptionResponse;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactSearch;
import uk.gov.hmcts.reform.pip.data.management.utils.IntegrationTestBase;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@WithMockUser(username = "admin", authorities = {"APPROLE_api.request.admin"})
class MigrationSupportApiTest extends IntegrationTestBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String PUBLICATION_URL = "/migration-support/publication";
    private static final String FROM_PARAM = "lastReceivedDateFrom";
    private static final String TO_PARAM = "lastReceivedDateTo";

    private static final String UNAUTHORIZED_ROLE = "APPROLE_unknown.authorized";
    private static final String UNAUTHORIZED_USERNAME = "unauthorized_isAuthorized";

    // Dates far in the past so filtered results are not affected by publications created by other tests
    private static final LocalDateTime BEFORE_RANGE = LocalDateTime.of(2000, 12, 31, 23, 59, 59);
    private static final LocalDateTime START_OF_RANGE = LocalDateTime.of(2001, 1, 1, 0, 0, 0);
    private static final LocalDateTime END_OF_RANGE = LocalDateTime.of(2001, 1, 15, 23, 59, 59);
    private static final LocalDateTime AFTER_RANGE = LocalDateTime.of(2001, 1, 16, 0, 0, 0);

    private static final String CASE_NUMBER = "T485913";
    private static final String CASE_NAME = "Migration case";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArtefactRepository artefactRepository;

    @Autowired
    private ArtefactSearchRepository artefactSearchRepository;

    private Artefact beforeRangeArtefact;
    private Artefact startOfRangeArtefact;
    private Artefact endOfRangeArtefact;
    private Artefact afterRangeArtefact;
    private List<ArtefactSearch> searchData;

    @BeforeAll
    static void startup() {
        OBJECT_MAPPER.findAndRegisterModules();
    }

    @BeforeEach
    void setup() {
        beforeRangeArtefact = createArtefact(BEFORE_RANGE);
        startOfRangeArtefact = createArtefact(START_OF_RANGE);
        endOfRangeArtefact = createArtefact(END_OF_RANGE);
        afterRangeArtefact = createArtefact(AFTER_RANGE);

        searchData = artefactSearchRepository.saveAll(List.of(
            new ArtefactSearch(null, endOfRangeArtefact.getArtefactId(), CASE_NUMBER, CASE_NAME),
            new ArtefactSearch(null, endOfRangeArtefact.getArtefactId(), "T485914", "Another case")
        ));
    }

    @AfterEach
    void tearDown() {
        artefactSearchRepository.deleteAll(searchData);
        artefactRepository.deleteAll(List.of(
            beforeRangeArtefact, startOfRangeArtefact, endOfRangeArtefact, afterRangeArtefact
        ));
    }

    private Artefact createArtefact(LocalDateTime lastReceivedDate) {
        Artefact artefact = Artefact.builder()
            .provenance("MANUAL_UPLOAD")
            .sourceArtefactId("migration-" + UUID.randomUUID())
            .type(ArtefactType.LIST)
            .sensitivity(Sensitivity.PUBLIC)
            .language(Language.ENGLISH)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .locationId(UUID.randomUUID().toString())
            .contentDate(lastReceivedDate.toLocalDate().atStartOfDay())
            .displayFrom(lastReceivedDate)
            .displayTo(lastReceivedDate.plusDays(1))
            .payload(BLOB_PAYLOAD_URL + "/" + UUID.randomUUID())
            .lastReceivedDate(lastReceivedDate)
            .supersededCount(2)
            .payloadSize(12.5f)
            .build();
        return artefactRepository.save(artefact);
    }

    private List<JsonNode> getPublications(String from, String to) throws Exception {
        MvcResult result = mockMvc.perform(get(PUBLICATION_URL).queryParam(FROM_PARAM, from)
                                               .queryParam(TO_PARAM, to))
            .andExpect(status().isOk())
            .andReturn();
        List<JsonNode> publications = new ArrayList<>();
        OBJECT_MAPPER.readTree(result.getResponse().getContentAsString()).forEach(publications::add);
        return publications;
    }

    private static List<UUID> artefactIds(List<JsonNode> publications) {
        return publications.stream().map(p -> UUID.fromString(p.get("artefactId").asText())).toList();
    }

    private static JsonNode findPublication(List<JsonNode> publications, UUID artefactId) {
        return publications.stream()
            .filter(p -> artefactId.toString().equals(p.get("artefactId").asText()))
            .findFirst()
            .orElseThrow();
    }

    @Test
    void testNoFiltersReturnsAllPublicationsWithAllFieldsAndSearchData() throws Exception {
        List<JsonNode> publications = getPublications(null, null);

        assertThat(artefactIds(publications)).contains(
            beforeRangeArtefact.getArtefactId(), startOfRangeArtefact.getArtefactId(),
            endOfRangeArtefact.getArtefactId(), afterRangeArtefact.getArtefactId()
        );

        JsonNode json = findPublication(publications, endOfRangeArtefact.getArtefactId());
        assertThat(json.get("provenance").asText()).isEqualTo("MANUAL_UPLOAD");
        assertThat(json.get("sourceArtefactId").asText()).isEqualTo(endOfRangeArtefact.getSourceArtefactId());
        assertThat(json.get("type").asText()).isEqualTo("LIST");
        assertThat(json.get("sensitivity").asText()).isEqualTo("PUBLIC");
        assertThat(json.get("language").asText()).isEqualTo("ENGLISH");
        assertThat(json.get("listType").asText()).isEqualTo("CIVIL_DAILY_CAUSE_LIST");
        assertThat(json.get("locationId").asText()).isEqualTo(endOfRangeArtefact.getLocationId());
        assertThat(json.get("contentDate").asText()).isEqualTo("2001-01-15T00:00:00Z");
        assertThat(json.get("displayFrom").asText()).isEqualTo("2001-01-15T23:59:59Z");
        assertThat(json.get("displayTo").asText()).isEqualTo("2001-01-16T23:59:59Z");
        assertThat(json.get("isFlatFile").asBoolean()).isFalse();
        assertThat(json.get("payload").asText()).isEqualTo(endOfRangeArtefact.getPayload());
        assertThat(json.get("lastReceivedDate").asText()).isEqualTo("2001-01-15T23:59:59");
        assertThat(json.get("supersededCount").asInt()).isEqualTo(2);
        assertThat(json.get("payloadSize").floatValue()).isEqualTo(12.5f);

        List<ArtefactSearch> returnedSearchData = OBJECT_MAPPER.convertValue(
            json.get("searchData"), new TypeReference<>() {}
        );
        assertThat(returnedSearchData).containsExactlyInAnyOrderElementsOf(searchData);
    }

    @Test
    void testPublicationWithNoSearchDataReturnsEmptyList() throws Exception {
        JsonNode json = findPublication(getPublications(null, null), afterRangeArtefact.getArtefactId());

        assertThat(json.get("searchData")).isEmpty();
    }

    @Test
    void testBothFiltersReturnsPublicationsBetweenDatesInclusive() throws Exception {
        List<UUID> artefactIds = artefactIds(getPublications("2001-01-01", "2001-01-15"));

        assertThat(artefactIds).containsExactly(
            startOfRangeArtefact.getArtefactId(), endOfRangeArtefact.getArtefactId()
        );
    }

    @Test
    void testFromFilterOnlyReturnsPublicationsReceivedOnOrAfterDate() throws Exception {
        List<UUID> artefactIds = artefactIds(getPublications("2001-01-01", null));

        assertThat(artefactIds)
            .contains(startOfRangeArtefact.getArtefactId(), endOfRangeArtefact.getArtefactId(),
                      afterRangeArtefact.getArtefactId())
            .doesNotContain(beforeRangeArtefact.getArtefactId());
    }

    @Test
    void testToFilterOnlyReturnsPublicationsReceivedOnOrBeforeDate() throws Exception {
        List<UUID> artefactIds = artefactIds(getPublications(null, "2001-01-15"));

        assertThat(artefactIds)
            .contains(beforeRangeArtefact.getArtefactId(), startOfRangeArtefact.getArtefactId(),
                      endOfRangeArtefact.getArtefactId())
            .doesNotContain(afterRangeArtefact.getArtefactId());
    }

    @Test
    void testInvalidDateReturnsBadRequest() throws Exception {
        mockMvc.perform(get(PUBLICATION_URL).queryParam(FROM_PARAM, "2001-13-45"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testFromAfterToReturnsBadRequest() throws Exception {
        MvcResult result = mockMvc.perform(get(PUBLICATION_URL)
                                               .queryParam(FROM_PARAM, "2001-01-16")
                                               .queryParam(TO_PARAM, "2001-01-15"))
            .andExpect(status().isBadRequest())
            .andReturn();

        ExceptionResponse response = OBJECT_MAPPER.readValue(result.getResponse().getContentAsString(),
                                                             ExceptionResponse.class);
        assertThat(response.getMessage()).isEqualTo("lastReceivedDateFrom must not be after lastReceivedDateTo");
    }

    @Test
    @WithMockUser(username = UNAUTHORIZED_USERNAME, authorities = {UNAUTHORIZED_ROLE})
    void testUnauthorisedGetPublicationsReturnsForbidden() throws Exception {
        mockMvc.perform(get(PUBLICATION_URL))
            .andExpect(status().isForbidden());
    }
}
