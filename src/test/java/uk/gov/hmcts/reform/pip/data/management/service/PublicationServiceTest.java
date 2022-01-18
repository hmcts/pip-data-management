package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.HeaderValidationException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.UnauthorisedRequestException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationServiceTest {

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    AzureBlobService azureBlobService;

    @Mock
    PayloadExtractor payloadExtractor;

    @InjectMocks
    PublicationService publicationService;
    private static final String VALIDATION_EXPECTED_MESSAGE =
        "The expected exception does not contain the correct message";
    private static final LocalDateTime DISPLAY_FROM = LocalDateTime.now();
    private static final LocalDateTime DISPLAY_TO = LocalDateTime.now();
    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String PROVENANCE = "provenance";
    private static final String PAYLOAD = "This is a payload";
    private static final String PAYLOAD_URL = "https://ThisIsATestPayload";
    private static final String TEST_KEY = "TestKey";
    private static final String TEST_VALUE = "TestValue";
    private static final Map<String, List<Object>> SEARCH_VALUES = new ConcurrentHashMap<>();

    @BeforeAll
    public static void setup() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
    }

    @Test
    void testCreationOfNewArtefact() {

        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .build();

        Artefact artefactWithPayloadUrl = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .build();

        Artefact artefactWithIdAndPayloadUrl = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .build();

        when(artefactRepository.findBySourceArtefactIdAndProvenance(SOURCE_ARTEFACT_ID, PROVENANCE))
            .thenReturn(Optional.empty());
        when(azureBlobService.createPayload(SOURCE_ARTEFACT_ID, PROVENANCE, PAYLOAD)).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, "Row ID must match returned UUID");
    }

    @Test
    void testUpdatingOfExistingArtefact() {

        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .build();

        Artefact existingArtefact = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .build();

        Artefact newArtefactWithId = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .build();

        when(artefactRepository.findBySourceArtefactIdAndProvenance(SOURCE_ARTEFACT_ID, PROVENANCE))
            .thenReturn(Optional.of(existingArtefact));
        when(azureBlobService.createPayload(SOURCE_ARTEFACT_ID, PROVENANCE, PAYLOAD)).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(newArtefactWithId)).thenReturn(newArtefactWithId);
        when(payloadExtractor.extractSearchTerms(PAYLOAD)).thenReturn(SEARCH_VALUES);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(newArtefactWithId, returnedArtefact, "Row ID must match returned UUID");
    }

    @Test
    void testGetBlobFromAzureService() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .build();
        when(artefactRepository.findByArtefactId(any())).thenReturn(Optional.of(artefact));
        when(azureBlobService.getBlobData(any(), any()))
            .thenReturn(String.valueOf(artefact));
        assertEquals(artefact.toString(), publicationService.getByArtefactId(ARTEFACT_ID, true),
                     "Artefacts do not match"
        );
    }

    @Test
    void testFindArtefactsFromPostgres() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .build();

        Artefact artefact2 = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.WELSH)
            .build();

        List<Artefact> artefactList = new ArrayList<>();
        artefactList.add(artefact);
        artefactList.add(artefact2);

        when(artefactRepository.findArtefactsBySearchUnverified(any(), any()))
            .thenReturn(artefactList);
        when(artefactRepository.findArtefactsBySearchVerified(any(), any()))
            .thenReturn(artefactList);

        assertEquals(artefactList, publicationService.findAllByCourtId("abc", true),
                     "Message"
        );
        assertEquals(artefactList, publicationService.findAllByCourtId("abc", false),
                     "Message"
        );
    }

    @Test
    void checkForUnauthorised() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .build();

        when(artefactRepository.findByArtefactId(any())).thenReturn(Optional.of(artefact));
        assertThrows(UnauthorisedRequestException.class, () -> {
            publicationService.getByArtefactId(UUID.randomUUID(), false);
        }, "Should throw an unauthorised request exception.");
    }

    @Test
    void testCreationOfPublicationJudgementTypeAndEmptyDateTo() {
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                publicationService.validateDateFromDateTo(DISPLAY_FROM, null, ArtefactType.JUDGEMENT);
            });
        assertEquals("Date to field is mandatory for publication type JUDGEMENT",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationListTypeAndEmptyDateTo() {
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                publicationService.validateDateFromDateTo(DISPLAY_FROM, null, ArtefactType.LIST);
            });
        assertEquals("Date to field is mandatory for publication type LIST",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationOutcomeTypeAndEmptyDateTo() {
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                publicationService.validateDateFromDateTo(DISPLAY_FROM, null, ArtefactType.OUTCOME);
            });
        assertEquals("Date to field is mandatory for publication type OUTCOME",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationListTypeAndEmptyDateFrom() {
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                publicationService.validateDateFromDateTo(null, DISPLAY_TO, ArtefactType.LIST);
            });
        assertEquals("Date from field is mandatory for publication type LIST",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationJudgementTypeAndEmptyDateFrom() {
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                publicationService.validateDateFromDateTo(null, DISPLAY_TO, ArtefactType.JUDGEMENT);
            });
        assertEquals("Date from field is mandatory for publication type JUDGEMENT",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }

    @Test
    void testCreationOfPublicationOutcomeTypeAndEmptyDateFrom() {
        HeaderValidationException dateHeaderValidationException =
            assertThrows(HeaderValidationException.class, () -> {
                publicationService.validateDateFromDateTo(null, DISPLAY_TO, ArtefactType.OUTCOME);
            });
        assertEquals("Date from field is mandatory for publication type OUTCOME",
                     dateHeaderValidationException.getMessage(), VALIDATION_EXPECTED_MESSAGE
        );
    }


    @Test
    void testValidationOfStatusUpdateTypeWithNoDateFrom() {
        assertFalse(
            publicationService.validateDateFromDateTo(null,
                                                      LocalDateTime.now(), ArtefactType.STATUS_UPDATES
            ),
            VALIDATION_EXPECTED_MESSAGE
        );

        assertTrue(
            publicationService.validateDateFromDateTo(DISPLAY_FROM, DISPLAY_TO, ArtefactType.STATUS_UPDATES),
            VALIDATION_EXPECTED_MESSAGE
        );

    }
}



