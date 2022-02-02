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
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.NotFoundException;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Language;
import uk.gov.hmcts.reform.pip.data.management.utils.PayloadExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
    private static final UUID ARTEFACT_ID = UUID.randomUUID();
    private static final String SOURCE_ARTEFACT_ID = "1234";
    private static final String PROVENANCE = "provenance";
    private static final String PAYLOAD = "This is a payload";
    private static final String PAYLOAD_URL = "https://ThisIsATestPayload";
    private static final String TEST_KEY = "TestKey";
    private static final String TEST_VALUE = "TestValue";
    private static final Map<String, List<Object>> SEARCH_VALUES = new ConcurrentHashMap<>();
    private static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);
    private static final String VALIDATION_ARTEFACT_NOT_MATCH = "Artefacts do not match";

    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;

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

        lenient().when(artefactRepository.findBySourceArtefactIdAndProvenance(SOURCE_ARTEFACT_ID, PROVENANCE))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
    }

    @Test
    void testCreationOfNewArtefact() {
        when(azureBlobService.createPayload(SOURCE_ARTEFACT_ID, PROVENANCE, PAYLOAD)).thenReturn(PAYLOAD_URL);
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

        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, "Returned artefacts should match");
    }

    @Test
    void testArtefactContentFromAzureWhenAuthorized() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .build();
        when(artefactRepository.findByArtefactIdVerified(any(), any())).thenReturn(Optional.of(artefact));
        when(azureBlobService.getBlobData(any(), any()))
            .thenReturn(String.valueOf(artefact));
        assertEquals(artefact.toString(), publicationService.getPayloadByArtefactId(ARTEFACT_ID, true),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactContentFromAzureWhenUnauthorized() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .build();
        when(artefactRepository.findByArtefactIdUnverified(any(), any())).thenReturn(Optional.of(artefact));
        when(azureBlobService.getBlobData(any(), any()))
            .thenReturn(String.valueOf(artefact));
        assertEquals(artefact.toString(), publicationService.getPayloadByArtefactId(ARTEFACT_ID, false),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactContentFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactIdVerified(any(), any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, ()
            -> publicationService.getPayloadByArtefactId(ARTEFACT_ID, true),
                      "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenAuthorized() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .build();
        when(artefactRepository.findByArtefactIdVerified(any(), any())).thenReturn(Optional.of(artefact));
        assertEquals(artefact, publicationService.getMetadataByArtefactId(ARTEFACT_ID, true),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenUnauthorized() {
        Artefact artefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .language(Language.ENGLISH)
            .build();
        when(artefactRepository.findByArtefactIdUnverified(any(), any())).thenReturn(Optional.of(artefact));
        assertEquals(artefact, publicationService.getMetadataByArtefactId(ARTEFACT_ID, false),
                     VALIDATION_ARTEFACT_NOT_MATCH
        );
    }

    @Test
    void testArtefactMetadataFromAzureWhenDoesNotExist() {
        when(artefactRepository.findByArtefactIdVerified(any(), any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, ()
            -> publicationService.getPayloadByArtefactId(ARTEFACT_ID, true),
                     "Not Found exception has not been thrown when artefact does not exist"
        );
    }

    @Test
    void testFindByCourtIdFromPostgres() {
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
}



