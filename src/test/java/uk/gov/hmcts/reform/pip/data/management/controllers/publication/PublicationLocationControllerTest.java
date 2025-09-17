package uk.gov.hmcts.reform.pip.data.management.controllers.publication;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationArtefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.publication.PublicationLocationService;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ConstantsTestHelper.STATUS_CODE_MATCH;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicationLocationControllerTest {
    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String SOURCE_ARTEFACT_ID = "sourceArtefactId";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final String PROVENANCE = "provenance";
    private static final String LOCATION_ID = "123";
    private static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    private static final String PAYLOAD_URL = "This is a test payload";
    private static final String NOT_EQUAL_MESSAGE = "The expected strings are not the same";
    private static final List<LocationArtefact> COURT_PER_LOCATION = new ArrayList<>();

    private static final Artefact ARTEFACT_WITH_ID = Artefact.builder()
        .artefactId(ARTEFACT_ID)
        .sourceArtefactId(SOURCE_ARTEFACT_ID)
        .displayFrom(DISPLAY_FROM)
        .displayTo(DISPLAY_TO)
        .language(Language.ENGLISH)
        .provenance(PROVENANCE)
        .sensitivity(Sensitivity.PUBLIC)
        .type(ArtefactType.LIST)
        .payload(PAYLOAD_URL)
        .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
        .locationId(LOCATION_ID)
        .contentDate(CONTENT_DATE)
        .search(new ConcurrentHashMap<>())
        .payloadSize(10f)
        .build();

    @Mock
    private PublicationLocationService publicationLocationService;

    @InjectMocks
    private PublicationLocationController publicationLocationController;

    @Test
    void checkCountArtefactByLocationReturnsData() {
        COURT_PER_LOCATION.add(new LocationArtefact("1", 2));
        when(publicationLocationService.countArtefactsByLocation()).thenReturn(COURT_PER_LOCATION);
        ResponseEntity<List<LocationArtefact>> result =
            publicationLocationController.countByLocation("123-456");
        assertEquals(HttpStatus.OK, result.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(COURT_PER_LOCATION, result.getBody(), NOT_EQUAL_MESSAGE);
    }

    @Test
    void testGetLocationTypeReturnsOk() {
        when(publicationLocationService.getLocationType(ListType.CIVIL_DAILY_CAUSE_LIST))
            .thenReturn(LocationType.VENUE);
        assertEquals(
            HttpStatus.OK,
            publicationLocationController.getLocationType(ListType.CIVIL_DAILY_CAUSE_LIST).getStatusCode(),
            STATUS_CODE_MATCH
        );
    }

    @Test
    void testGetAllNoMatchArtefacts() {
        List<Artefact> artefactList = List.of(ARTEFACT_WITH_ID);

        when(publicationLocationService.findAllNoMatchArtefacts()).thenReturn(artefactList);

        ResponseEntity<List<Artefact>> response =
            publicationLocationController.getAllNoMatchArtefacts(USER_ID.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode(), STATUS_CODE_MATCH);
        assertEquals(artefactList, response.getBody(), "Body should match");
    }

    @Test
    void testDeleteArtefactsByLocationReturnsOk() throws JsonProcessingException {
        int locationId = 1;
        String requesterId = UUID.randomUUID().toString();
        when(publicationLocationService.deleteArtefactByLocation(locationId, requesterId)).thenReturn("Success");

        assertEquals(HttpStatus.OK,
                     publicationLocationController.deleteArtefactsByLocation(requesterId, locationId).getStatusCode(),
                     "Delete artefacts for location endpoint has not returned OK");
    }
}
