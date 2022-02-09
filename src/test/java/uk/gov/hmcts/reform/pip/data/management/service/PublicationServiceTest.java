package uk.gov.hmcts.reform.pip.data.management.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureBlobService;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ArtefactNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.UnauthorisedRequestException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.TestConstants.MESSAGES_MATCH;

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
    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String PROVENANCE = "provenance";
    private static final String PAYLOAD = "This is a payload";
    private static final String PAYLOAD_URL = "https://ThisIsATestPayload";
    private static final String TEST_KEY = "TestKey";
    private static final String TEST_VALUE = "TestValue";
    private static final String SEARCH_TERM_VALUE = "case-id";
    private static final CaseSearchTerm SEARCH_TERM = CaseSearchTerm.CASE_ID;
    private static final Map<String, List<Object>> SEARCH_VALUES = new ConcurrentHashMap<>();
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);
    private static final String ARTEFACT_MATCH_MESSAGE = "Returned Artefacts should match";

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;
    private Artefact artefactWithId;

    @BeforeAll
    public static void setupSearchValues() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
    }

    @BeforeEach
    void setup() {
        artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .build();

        artefactWithPayloadUrl = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .build();

        artefactWithIdAndPayloadUrl = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .build();

        artefactWithId = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .build();

        lenient().when(artefactRepository.findBySourceArtefactIdAndProvenance(SOURCE_ARTEFACT_ID, PROVENANCE))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
        lenient()
            .when(artefactRepository.findArtefactBySearchVerified(eq(SEARCH_TERM_VALUE), eq(TEST_VALUE), any()))
            .thenReturn(List.of(artefactWithIdAndPayloadUrl));
        lenient()
            .when(artefactRepository.findArtefactBySearchUnverified(eq(SEARCH_TERM_VALUE), eq(TEST_VALUE), any()))
            .thenReturn(List.of(artefactWithId));
    }

    @Test
    void testCreationOfNewArtefact() {
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
    void testCreationOfNewArtefactWithFile() {
        artefactWithPayloadUrl.setSearch(null);
        when(azureBlobService.uploadFlatFile(SOURCE_ARTEFACT_ID, PROVENANCE, FILE)).thenReturn(PAYLOAD_URL);

        Artefact returnedArtefact = publicationService.createPublication(artefact, FILE);

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, ARTEFACT_MATCH_MESSAGE);
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

        when(artefactRepository.findArtefactsByCourtIdUnverified(any(), any()))
            .thenReturn(artefactList);
        when(artefactRepository.findArtefactsByCourtIdVerified(any(), any()))
            .thenReturn(artefactList);

        assertEquals(artefactList, publicationService.findAllByCourtId("abc", true),
                     ARTEFACT_MATCH_MESSAGE
        );
        assertEquals(artefactList, publicationService.findAllByCourtId("abc", false),
                     ARTEFACT_MATCH_MESSAGE
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
    void testFindAllBySearchVerified() {
        assertEquals(artefactWithIdAndPayloadUrl,
                     publicationService.findAllBySearch(SEARCH_TERM, TEST_VALUE, true).get(0),
                     ARTEFACT_MATCH_MESSAGE);
    }

    @Test
    void testFindAllBySearchUnverified() {
        assertEquals(artefactWithId,
                     publicationService.findAllBySearch(SEARCH_TERM, TEST_VALUE, false).get(0),
                     ARTEFACT_MATCH_MESSAGE);
    }

    @Test
    void testNoArtefactsThrowsNotFound() {
        ArtefactNotFoundException ex = assertThrows(ArtefactNotFoundException.class, () ->
           publicationService.findAllBySearch(SEARCH_TERM, "not found", true)
        );
        assertEquals("No Artefacts found with for CASE_ID with the value: not found",
                     ex.getMessage(), MESSAGES_MATCH);
    }
}



