package uk.gov.hmcts.reform.pip.data.management.service.publication;

import nl.altindag.log.LogCaptor;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateArtefactConflictException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.JsonExtractor;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.FILE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_VALUES;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationCreationRunnerTest {
    private static final String LAST_RECEIVED_DATE_MESSAGE = "Last received date does not match";
    private static final String SEARCH_VALUE_MESSAGE = "Search value does not match";
    private static final String LOG_MESSAGE = "Log message does not match";
    private static final String EXCEPTION_MESSAGE = "Exception does not match";
    private static final String JSON_PUBLICATION_LOG = "Uploaded json publication upload for location "
        + PROVENANCE_ID;
    private static final String FLAT_FILE_PUBLICATION_LOG = "Uploaded flat file publication upload for location "
        + PROVENANCE_ID;
    private static final String JSON_PUBLICATION_DEADLOCK = "Deadlock when creating json publication. "
        + "Please try again later.";
    private static final String FLAT_FILE_PUBLICATION_DEADLOCK = "Deadlock when creating flat file publication. "
        + "Please try again later.";

    private static final Float PAYLOAD_SIZE_WITHIN_LIMIT = 90f;
    private static final Float PAYLOAD_SIZE_OVER_LIMIT = 110f;

    @Mock
    private PublicationService publicationService;

    @Mock
    private ArtefactService artefactService;

    @Mock
    JsonExtractor jsonExtractor;

    @InjectMocks
    private PublicationCreationRunner publicationCreationRunner;

    private final LogCaptor logCaptor = LogCaptor.forClass(PublicationCreationRunner.class);

    @BeforeEach
    void setup() {
        lenient().when(artefactService.payloadWithinJsonSearchLimit(PAYLOAD_SIZE_WITHIN_LIMIT)).thenReturn(true);
        lenient().when(artefactService.payloadWithinJsonSearchLimit(PAYLOAD_SIZE_OVER_LIMIT)).thenReturn(false);
    }

    @Test
    void testRunMethodForJsonPublication() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        artefact.setPayloadSize(PAYLOAD_SIZE_WITHIN_LIMIT);
        when(publicationService.createPublication(artefact, PAYLOAD)).thenReturn(artefact);
        when(jsonExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationCreationRunner.run(artefact, PAYLOAD);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(returnedArtefact.getLastReceivedDate().toLocalDate())
            .as(LAST_RECEIVED_DATE_MESSAGE)
            .isEqualTo(LocalDate.now());

        softly.assertThat(returnedArtefact.getSearch())
            .as(SEARCH_VALUE_MESSAGE)
            .isEqualTo(SEARCH_VALUES);

        softly.assertThat(logCaptor.getInfoLogs().get(0))
            .as(LOG_MESSAGE)
            .contains(JSON_PUBLICATION_LOG);

        softly.assertAll();
    }

    @Test
    void testRunMethodForJsonPublicationWithoutExtractingSearchTerms() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        artefact.setPayloadSize(PAYLOAD_SIZE_WITHIN_LIMIT);
        when(publicationService.createPublication(artefact, PAYLOAD)).thenReturn(artefact);

        Artefact returnedArtefact = publicationCreationRunner.run(artefact, PAYLOAD, false);

        verify(publicationService).applyInternalLocationId(returnedArtefact);
        verify(jsonExtractor, never()).extractSearchTerms(PAYLOAD);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(returnedArtefact.getLastReceivedDate().toLocalDate())
            .as(LAST_RECEIVED_DATE_MESSAGE)
            .isEqualTo(LocalDate.now());

        softly.assertThat(returnedArtefact.getSearch())
            .as(SEARCH_VALUE_MESSAGE)
            .isEqualTo(SEARCH_VALUES);

        softly.assertThat(logCaptor.getInfoLogs().get(0))
            .as(LOG_MESSAGE)
            .contains(JSON_PUBLICATION_LOG);

        softly.assertAll();
    }

    @Test
    void testRunMethodForJsonPublicationWithCannotAcquireLockException() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        artefact.setPayloadSize(PAYLOAD_SIZE_WITHIN_LIMIT);
        doThrow(CannotAcquireLockException.class).when(publicationService).createPublication(artefact, PAYLOAD);
        when(jsonExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        assertThatThrownBy(() -> publicationCreationRunner.run(artefact, PAYLOAD))
            .as(EXCEPTION_MESSAGE)
            .isInstanceOf(CreateArtefactConflictException.class)
            .hasMessage(JSON_PUBLICATION_DEADLOCK);

        assertThat(logCaptor.getInfoLogs())
            .as(LOG_MESSAGE)
            .isEmpty();
    }

    @Test
    void testRunMethodForJsonPublicationWithDataIntegrityViolationException() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        artefact.setPayloadSize(PAYLOAD_SIZE_WITHIN_LIMIT);
        doThrow(DataIntegrityViolationException.class).when(publicationService).createPublication(artefact, PAYLOAD);
        when(jsonExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        assertThatThrownBy(() -> publicationCreationRunner.run(artefact, PAYLOAD))
            .as(EXCEPTION_MESSAGE)
            .isInstanceOf(CreateArtefactConflictException.class)
            .hasMessage(JSON_PUBLICATION_DEADLOCK);

        assertThat(logCaptor.getInfoLogs())
            .as(LOG_MESSAGE)
            .isEmpty();
    }

    @Test
    void testSearchValuesNotGeneratedFOrJsonPublicationWhenPayloadOverLimit() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        artefact.setPayloadSize(PAYLOAD_SIZE_OVER_LIMIT);
        when(publicationService.createPublication(artefact, PAYLOAD)).thenReturn(artefact);

        Artefact returnedArtefact = publicationCreationRunner.run(artefact, PAYLOAD);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(returnedArtefact.getSearch())
            .as(SEARCH_VALUE_MESSAGE)
            .isEqualTo(Collections.emptyMap());

        softly.assertThat(logCaptor.getInfoLogs().get(0))
            .as(LOG_MESSAGE)
            .contains(JSON_PUBLICATION_LOG);

        softly.assertAll();
        verifyNoInteractions(jsonExtractor);
    }

    @Test
    void testRuMethodForFlatFilePublication() {
        Artefact artefact = ArtefactConstantTestHelper.buildSjpPublicArtefact();
        when(publicationService.createPublication(artefact, FILE)).thenReturn(artefact);

        Artefact returnedArtefact = publicationCreationRunner.run(artefact, FILE);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(returnedArtefact.getLastReceivedDate().toLocalDate())
            .as(LAST_RECEIVED_DATE_MESSAGE)
            .isEqualTo(LocalDate.now());

        softly.assertThat(logCaptor.getInfoLogs().get(0))
            .as(LOG_MESSAGE)
            .contains(FLAT_FILE_PUBLICATION_LOG);

        softly.assertAll();
        verifyNoInteractions(jsonExtractor);
    }

    @Test
    void testRuMethodForFlatFilePublicationWithCannotAcquireLockException() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        doThrow(CannotAcquireLockException.class).when(publicationService).createPublication(artefact, FILE);

        assertThatThrownBy(() -> publicationCreationRunner.run(artefact, FILE))
            .as(EXCEPTION_MESSAGE)
            .isInstanceOf(CreateArtefactConflictException.class)
            .hasMessage(FLAT_FILE_PUBLICATION_DEADLOCK);

        assertThat(logCaptor.getInfoLogs())
            .as(LOG_MESSAGE)
            .isEmpty();
    }

    @Test
    void testRuMethodForFlatFilePublicationWithDataIntegrityViolationException() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        doThrow(DataIntegrityViolationException.class).when(publicationService).createPublication(artefact, FILE);

        assertThatThrownBy(() -> publicationCreationRunner.run(artefact, FILE))
            .as(EXCEPTION_MESSAGE)
            .isInstanceOf(CreateArtefactConflictException.class)
            .hasMessage(FLAT_FILE_PUBLICATION_DEADLOCK);

        assertThat(logCaptor.getInfoLogs())
            .as(LOG_MESSAGE)
            .isEmpty();
    }
}
