package uk.gov.hmcts.reform.pip.data.management.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-jpa")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArtefactRepositorySearchTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String CASE_NAME = "Test case name";
    private static final String CASE_NUMBER = "Test case number";
    private static final String CASE_URN = "Test case URN";

    private static final LocalDateTime TODAY = LocalDateTime.now();
    private static final LocalDateTime TOMORROW = TODAY.plusDays(1);
    private static final LocalDateTime YESTERDAY = TODAY.minusDays(1);

    private static final String ARTEFACT_MATCHED_MESSAGE = "Artefact does not match";
    private static final String ARTEFACT_EMPTY_MESSAGE = "Artefact is not empty";

    private UUID artefactId1;
    private UUID artefactId2;

    @Autowired
    ArtefactRepository artefactRepository;

    @BeforeAll
    void setup() throws IOException {
        OBJECT_MAPPER.findAndRegisterModules();

        Artefact artefact1 = new Artefact();
        Map<String, List<Object>> caseNumberSearchValues = getSearchData("data/caseNumberSearchValues.json");
        artefact1.setSearch(caseNumberSearchValues);
        artefact1.setDisplayFrom(YESTERDAY);
        artefact1.setDisplayTo(TOMORROW);

        Artefact savedArtefact = artefactRepository.save(artefact1);
        artefactId1 = savedArtefact.getArtefactId();

        Artefact artefact2 = new Artefact();
        Map<String, List<Object>> caseUrnSearchValues = getSearchData("data/caseUrnSearchValues.json");
        artefact2.setSearch(caseUrnSearchValues);
        artefact2.setDisplayFrom(YESTERDAY);
        artefact2.setDisplayTo(TOMORROW);

        savedArtefact = artefactRepository.save(artefact2);
        artefactId2 = savedArtefact.getArtefactId();
    }

    @AfterAll
    void shutdown() {
        artefactRepository.deleteAll();
    }

    private Map<String, List<Object>> getSearchData(String resourcePath) throws IOException {
        try (InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream(resourcePath)) {
            String jsonData = IOUtils.toString(inputStream, Charset.defaultCharset());
            return OBJECT_MAPPER.readValue(jsonData, new TypeReference<>() {});
        }
    }

    @Test
    void shouldFindArtefactByCaseName() {
        assertThat(artefactRepository.findArtefactByCaseName(CASE_NAME, TODAY))
            .as(ARTEFACT_MATCHED_MESSAGE)
            .hasSize(2)
            .extracting(Artefact::getArtefactId)
            .containsExactlyInAnyOrder(artefactId1, artefactId2);
    }

    @Test
    void shouldNotFindArtefactByCaseNameUsingCaseNumber() {
        assertThat(artefactRepository.findArtefactByCaseName(CASE_NUMBER, TODAY))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldNotFindArtefactByCaseNameIfCurrentDateNotAfterDisplayFromDate() {
        assertThat(artefactRepository.findArtefactByCaseName(CASE_NUMBER, YESTERDAY))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldNotFindArtefactByCaseNameIfCurrentDateNotBeforeDisplayToDate() {
        assertThat(artefactRepository.findArtefactByCaseName(CASE_NUMBER, TOMORROW))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldFindArtefactBySearchUsingCaseNumber() {
        assertThat(artefactRepository.findArtefactBySearch(CaseSearchTerm.CASE_ID.dbValue, CASE_NUMBER, TODAY))
            .as(ARTEFACT_MATCHED_MESSAGE)
            .hasSize(1)
            .extracting(Artefact::getArtefactId)
            .containsExactly(artefactId1);
    }

    @Test
    void shouldNotFindArtefactBySearchWithCaseIdSearchTermUsingCaseUrn() {
        assertThat(artefactRepository.findArtefactBySearch(CaseSearchTerm.CASE_ID.dbValue, CASE_URN, TODAY))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldFindArtefactBySearchUsingCaseUrn() {
        assertThat(artefactRepository.findArtefactBySearch(CaseSearchTerm.CASE_URN.dbValue, CASE_URN, TODAY))
            .as(ARTEFACT_MATCHED_MESSAGE)
            .hasSize(1)
            .extracting(Artefact::getArtefactId)
            .containsExactly(artefactId2);
    }

    @Test
    void shouldNotFindArtefactBySearchWithCaseUrnSearchTermUsingCaseNumber() {
        assertThat(artefactRepository.findArtefactBySearch(CaseSearchTerm.CASE_URN.dbValue, CASE_NUMBER, TODAY))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldNotFindArtefactBySearchIfCurrentDateNotAfterDisplayFromDate() {
        assertThat(artefactRepository.findArtefactBySearch(CaseSearchTerm.CASE_ID.dbValue, CASE_NUMBER, YESTERDAY))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldNotFindArtefactBySearchIfCurrentDateNotBeforeDisplayToDate() {
        assertThat(artefactRepository.findArtefactBySearch(CaseSearchTerm.CASE_URN.dbValue, CASE_URN, TOMORROW))
            .as(ARTEFACT_EMPTY_MESSAGE)
            .isEmpty();
    }
}
