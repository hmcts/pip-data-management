package uk.gov.hmcts.reform.pip.data.management.service.publication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.NoMatchArtefact;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationServicesService;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.MANUAL_UPLOAD_PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationReportingServiceTest {
    @Mock
    private ArtefactRepository artefactRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private PublicationServicesService publicationServicesService;

    @InjectMocks
    private PublicationReportingService publicationReportingService;

    private final Artefact noMatchArtefact = ArtefactConstantTestHelper.buildNoMatchArtefact();

    @Test
    void testGetMiData()  {
        LocalDateTime localDateTime = LocalDateTime.now();
        UUID randomId = UUID.randomUUID();

        Location location = new Location();
        location.setLocationId(1);
        location.setName("Test Location");

        PublicationMiData publicationMiData = new PublicationMiData(
            randomId, localDateTime, localDateTime, Language.ENGLISH, MANUAL_UPLOAD_PROVENANCE,
            Sensitivity.PUBLIC, randomId.toString(), 0, ArtefactType.GENERAL_PUBLICATION,
            localDateTime, "1", ListType.CIVIL_DAILY_CAUSE_LIST);

        PublicationMiData publicationMiData2 = new PublicationMiData(
            UUID.randomUUID(), localDateTime, localDateTime, Language.ENGLISH, MANUAL_UPLOAD_PROVENANCE,
            Sensitivity.PUBLIC, UUID.randomUUID().toString(), 1, ArtefactType.GENERAL_PUBLICATION,
            localDateTime, "NoMatch2", ListType.CIVIL_DAILY_CAUSE_LIST);

        when(locationRepository.findAll()).thenReturn(List.of(location));
        when(artefactRepository.getMiData(any())).thenReturn(List.of(publicationMiData, publicationMiData2));

        List<PublicationMiData> publicationMiDataList = publicationReportingService.getMiData();

        PublicationMiData publicationMiDataWithLocationName = new PublicationMiData(
            randomId, localDateTime, localDateTime, Language.ENGLISH, MANUAL_UPLOAD_PROVENANCE,
            Sensitivity.PUBLIC, randomId.toString(), 0, ArtefactType.GENERAL_PUBLICATION,
            localDateTime,"1", ListType.CIVIL_DAILY_CAUSE_LIST);
        publicationMiDataWithLocationName.setLocationName("Test Location");

        assertIterableEquals(List.of(publicationMiDataWithLocationName, publicationMiData2), publicationMiDataList,
                             "Publications MI do not match");
    }

    @Test
    void testGetMiDataWhenArtefactRepositoryReturnsEmpty()  {
        when(locationRepository.findAll()).thenReturn(List.of());

        PublicationMiData publicationMiData = new PublicationMiData(
            UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), Language.ENGLISH, MANUAL_UPLOAD_PROVENANCE,
            Sensitivity.PUBLIC, UUID.randomUUID().toString(), 0, ArtefactType.GENERAL_PUBLICATION,
            LocalDateTime.now(),"100", ListType.CIVIL_DAILY_CAUSE_LIST);

        when(artefactRepository.getMiData(any())).thenReturn(List.of(publicationMiData));

        List<PublicationMiData> publicationMiDataList = publicationReportingService.getMiData();

        assertThat(publicationMiDataList.get(0).getLocationName())
            .as("Location name is not null").isNull();
    }

    @Test
    void testGetMiDataWhenCourtIdIsADouble()  {
        PublicationMiData publicationMiData = new PublicationMiData(
            UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), Language.ENGLISH, MANUAL_UPLOAD_PROVENANCE,
            Sensitivity.PUBLIC, UUID.randomUUID().toString(), 0, ArtefactType.GENERAL_PUBLICATION,
            LocalDateTime.now(),"100.12", ListType.CIVIL_DAILY_CAUSE_LIST);

        when(artefactRepository.getMiData(any())).thenReturn(List.of(publicationMiData));

        List<PublicationMiData> publicationMiDataList = publicationReportingService.getMiData();

        assertThat(publicationMiDataList.get(0).getLocationName())
            .as("Location name is not blank").isNull();
    }

    @Test
    void testReportNoMatchArtefacts() {
        when(artefactRepository.findAllNoMatchArtefacts()).thenReturn(List.of(noMatchArtefact));
        publicationReportingService.reportNoMatchArtefacts();
        verify(publicationServicesService).sendNoMatchArtefactsForReporting(List.of(new NoMatchArtefact(
            ARTEFACT_ID,
            PROVENANCE,
            PROVENANCE_ID
        )));
    }

    @Test
    void testReportMatchArtefactsWhenArtefactsNotFound() {
        when(artefactRepository.findAllNoMatchArtefacts()).thenReturn(Collections.emptyList());
        publicationReportingService.reportNoMatchArtefacts();
        verifyNoInteractions(publicationServicesService);
    }
}
