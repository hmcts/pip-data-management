package uk.gov.hmcts.reform.pip.data.management.service;

import nl.altindag.log.LogCaptor;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.jpa.JpaSystemException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CreateArtefactConflictException;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.FILE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_VALUES;

@ExtendWith(MockitoExtension.class)
class PublicationCreationRunnerTest {
    private static final String LAST_RECEIVED_DATE_MESSAGE = "Last received date does not match";
    private static final String EXPIRY_DATE_MESSAGE = "Expiry date does not match";
    private static final String SEARCH_VALUE_MESSAGE = "Search value does not match";
    private static final String LOG_MESSAGE = "Log message does not match";
    private static final String EXCEPTION_MESSAGE = "Exception does not match";
    private static final String JSON_PUBLICATION_LOG = "Uploaded json publication upload for location "
        + PROVENANCE_ID;

    @Mock
    private PublicationService publicationService;

    @Mock
    PayloadExtractor payloadExtractor;

    @InjectMocks
    private PublicationCreationRunner publicationCreationRunner;

    private final LogCaptor logCaptor = LogCaptor.forClass(PublicationCreationRunner.class);

    @Test
    void testRuMethodForJsonPublicationWithNonSjpListType() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        when(publicationService.createPublication(artefact, PAYLOAD)).thenReturn(artefact);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationCreationRunner.run(artefact, PAYLOAD);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(returnedArtefact.getLastReceivedDate().toLocalDate())
            .as(LAST_RECEIVED_DATE_MESSAGE)
            .isEqualTo(LocalDate.now());

        softly.assertThat(returnedArtefact.getExpiryDate().toLocalDate())
            .as(EXPIRY_DATE_MESSAGE)
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
    void testRuMethodForJsonPublicationWithSjpPublicListType() {
        Artefact artefact = ArtefactConstantTestHelper.buildSjpPublicArtefact();
        when(publicationService.createPublication(artefact, PAYLOAD)).thenReturn(artefact);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationCreationRunner.run(artefact, PAYLOAD);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(returnedArtefact.getLastReceivedDate().toLocalDate())
            .as(LAST_RECEIVED_DATE_MESSAGE)
            .isEqualTo(LocalDate.now());

        softly.assertThat(returnedArtefact.getExpiryDate().toLocalDate())
            .as(EXPIRY_DATE_MESSAGE)
            .isEqualTo(LocalDate.now().plusDays(7));

        softly.assertThat(returnedArtefact.getSearch())
            .as(SEARCH_VALUE_MESSAGE)
            .isEqualTo(SEARCH_VALUES);

        softly.assertThat(logCaptor.getInfoLogs().get(0))
            .as(LOG_MESSAGE)
            .contains(JSON_PUBLICATION_LOG);

        softly.assertAll();
    }

    @Test
    void testRuMethodForJsonPublicationWithSjpPressListType() {
        Artefact artefact = ArtefactConstantTestHelper.buildSjpPressArtefact();
        when(publicationService.createPublication(artefact, PAYLOAD)).thenReturn(artefact);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationCreationRunner.run(artefact, PAYLOAD);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(returnedArtefact.getLastReceivedDate().toLocalDate())
            .as(LAST_RECEIVED_DATE_MESSAGE)
            .isEqualTo(LocalDate.now());

        softly.assertThat(returnedArtefact.getExpiryDate().toLocalDate())
            .as(EXPIRY_DATE_MESSAGE)
            .isEqualTo(LocalDate.now().plusDays(7));

        softly.assertThat(returnedArtefact.getSearch())
            .as(SEARCH_VALUE_MESSAGE)
            .isEqualTo(SEARCH_VALUES);

        softly.assertThat(logCaptor.getInfoLogs().get(0))
            .as(LOG_MESSAGE)
            .contains(JSON_PUBLICATION_LOG);

        softly.assertAll();
    }

    @Test
    void testRuMethodForJsonPublicationWithCannotAcquireLockException() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        doThrow(CannotAcquireLockException.class).when(publicationService).createPublication(artefact, PAYLOAD);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        assertThatThrownBy(() -> publicationCreationRunner.run(artefact, PAYLOAD))
            .as(EXCEPTION_MESSAGE)
            .isInstanceOf(CreateArtefactConflictException.class)
            .hasMessage("Deadlock when creating json publication. Please try again later.");

        assertThat(logCaptor.getInfoLogs())
            .as(LOG_MESSAGE)
            .isEmpty();
    }

    @Test
    void testRuMethodForJsonPublicationWithJpaSystemException() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        doThrow(JpaSystemException.class).when(publicationService).createPublication(artefact, PAYLOAD);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        assertThatThrownBy(() -> publicationCreationRunner.run(artefact, PAYLOAD))
            .as(EXCEPTION_MESSAGE)
            .isInstanceOf(CreateArtefactConflictException.class)
            .hasMessage("Deadlock when creating json publication. Please try again later.");

        assertThat(logCaptor.getInfoLogs())
            .as(LOG_MESSAGE)
            .isEmpty();
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

        softly.assertThat(returnedArtefact.getExpiryDate().toLocalDate())
            .as(EXPIRY_DATE_MESSAGE)
            .isEqualTo(LocalDate.now());

        softly.assertThat(returnedArtefact.getSearch())
            .as(SEARCH_VALUE_MESSAGE)
            .isNull();

        softly.assertThat(logCaptor.getInfoLogs().get(0))
            .as(LOG_MESSAGE)
            .contains("Uploaded flat file publication upload for location " + PROVENANCE_ID);

        softly.assertAll();
        verifyNoInteractions(payloadExtractor);
    }

    @Test
    void testRuMethodForFlatFilePublicationWithCannotAcquireLockException() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        doThrow(CannotAcquireLockException.class).when(publicationService).createPublication(artefact, FILE);

        assertThatThrownBy(() -> publicationCreationRunner.run(artefact, FILE))
            .as(EXCEPTION_MESSAGE)
            .isInstanceOf(CreateArtefactConflictException.class)
            .hasMessage("Deadlock when creating flat file publication. Please try again later.");

        assertThat(logCaptor.getInfoLogs())
            .as(LOG_MESSAGE)
            .isEmpty();
    }

    @Test
    void testRuMethodForFlatFilePublicationWithJpaSystemException() {
        Artefact artefact = ArtefactConstantTestHelper.buildArtefact();
        doThrow(JpaSystemException.class).when(publicationService).createPublication(artefact, FILE);

        assertThatThrownBy(() -> publicationCreationRunner.run(artefact, FILE))
            .as(EXCEPTION_MESSAGE)
            .isInstanceOf(CreateArtefactConflictException.class)
            .hasMessage("Deadlock when creating flat file publication. Please try again later.");

        assertThat(logCaptor.getInfoLogs())
            .as(LOG_MESSAGE)
            .isEmpty();
    }
}
