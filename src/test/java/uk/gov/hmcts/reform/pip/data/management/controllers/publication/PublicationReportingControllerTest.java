package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationReportingService;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.STATUS_CODE_MATCH;

@ExtendWith(MockitoExtension.class)
class PublicationReportingControllerTest {
    @Mock
    private PublicationReportingService publicationReportingService;

    @InjectMocks
    private PublicationReportingController publicationReportingController;

    @Test
    void testMiDataReturnsSuccessfully() {
        PublicationMiData publicationMiData = new PublicationMiData(
            UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), Language.ENGLISH, "MANUAL_UPLOAD",
            Sensitivity.PUBLIC, UUID.randomUUID().toString(), 0, ArtefactType.GENERAL_PUBLICATION,
            LocalDateTime.now(),"1", ListType.CIVIL_DAILY_CAUSE_LIST);

        PublicationMiData publicationMiData2 = new PublicationMiData(
            UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), Language.ENGLISH, "MANUAL_UPLOAD",
            Sensitivity.PUBLIC, UUID.randomUUID().toString(), 1, ArtefactType.GENERAL_PUBLICATION,
            LocalDateTime.now(), "NoMatch2", ListType.CIVIL_DAILY_CAUSE_LIST);

        when(publicationReportingService.getMiData()).thenReturn(List.of(publicationMiData, publicationMiData2));

        ResponseEntity<List<PublicationMiData>> response = publicationReportingController.getMiData();

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_CODE_MATCH);
        assertThat(response.getBody()).containsExactlyInAnyOrder(publicationMiData, publicationMiData2);
    }

    @Test
    void testReportNoMatchArtefactsSuccess() {
        doNothing().when(publicationReportingService).reportNoMatchArtefacts();
        assertThat(publicationReportingController.reportNoMatchArtefacts().getStatusCode())
            .as(STATUS_CODE_MATCH)
            .isEqualTo(HttpStatus.NO_CONTENT);
    }
}
