package uk.gov.hmcts.reform.pip.data.management.service.publication;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.database.ArtefactRepository;
import uk.gov.hmcts.reform.pip.data.management.database.AzureArtefactBlobService;
import uk.gov.hmcts.reform.pip.data.management.database.LocationRepository;
import uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.PublicationManagementService;
import uk.gov.hmcts.reform.pip.model.publication.ArtefactType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;
import uk.gov.hmcts.reform.pip.model.report.PublicationMiData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.FILE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.LOCATION_VENUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.MANUAL_UPLOAD_PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.NO_COURT_EXISTS_IN_REFERENCE_DATA;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PAYLOAD_URL;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.PROVENANCE_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.ROWID_RETURNS_UUID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SEARCH_VALUES;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.SOURCE_ARTEFACT_ID;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.START_OF_TODAY_CONTENT_DATE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_KEY;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.TEST_VALUE;
import static uk.gov.hmcts.reform.pip.data.management.helpers.ArtefactConstantTestHelper.VALIDATION_ARTEFACT_NOT_MATCH;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports"})
class PublicationServiceTest {

    @Mock
    ArtefactRepository artefactRepository;

    @Mock
    LocationRepository locationRepository;

    @Mock
    AzureArtefactBlobService azureArtefactBlobService;

    @Mock
    PublicationManagementService publicationManagementService;

    @InjectMocks
    PublicationService publicationService;


    private Artefact artefact;
    private Artefact artefactWithPayloadUrl;
    private Artefact artefactWithIdAndPayloadUrl;

    private static final char DELIMITER = ',';
    private static final String LOCATION_NAME_WITH_ID_3 = "Oxford Combined Court Centre";
    private static final String LOCATION_NAME_WITH_ID_9 = "Single Justice Procedure";

    private static final List<String> MI_DATA_WITH_VALID_LOCATION_ID =
        List.of(
            "0beac960-68a3-41db-9f51-8c71826eaf30,2022-07-25 14:45:18.836,2022-09-29 14:45:18.836,BI_LINGUAL,"
                + "MANUAL_UPLOAD,PUBLIC,MANUAL_UPLOAD,LIST,2022-06-29 00:00:00.0,0,3,FAMILY_DAILY_CAUSE_LIST",
            "165ca91d-1e58-412a-80f5-1e5475a093e4,2022-06-29 14:45:18.836,2022-09-29 14:45:18.836,WELSH,"
                + "MANUAL_UPLOAD,PUBLIC,MANUAL_UPLOAD,GENERAL_PUBLICATION,2022-06-29 00:00:00.0,1,9,SJP_PUBLIC_LIST",
            "10238a0f-d398-4356-9af4-a4dbbb17d455,2022-06-29 14:45:18.836,2022-09-29 14:45:18.836,ENGLISH,"
                + "MANUAL_UPLOAD,PUBLIC,MANUAL_UPLOAD,GENERAL_PUBLICATION,2022-06-29 00:00:00.0,2,9,SJP_PUBLIC_LIST"
        );

    private static final List<String> MI_DATA_WITH_INVALID_LOCATION_ID =
        List.of(
            "0beac960-68a3-41db-9f51-8c71826eaf30,2022-07-25 14:45:18.836,2022-09-29 14:45:18.836,BI_LINGUAL,"
                + "MANUAL_UPLOAD,PUBLIC,MANUAL_UPLOAD,LIST,2022-06-29 00:00:00.0,0,1823,FAMILY_DAILY_CAUSE_LIST",
            "165ca91d-1e58-412a-80f5-1e5475a093e4,2022-06-29 14:45:18.836,2022-09-29 14:45:18.836,WELSH,"
                + "MANUAL_UPLOAD,PUBLIC,MANUAL_UPLOAD,GENERAL_PUBLICATION,2022-06-29 00:00:00.0,1,1815,SJP_PUBLIC_LIST",
            "10238a0f-d398-4356-9af4-a4dbbb17d455,2022-06-29 14:45:18.836,2022-09-29 14:45:18.836,ENGLISH,"
                + "MANUAL_UPLOAD,PUBLIC,MANUAL_UPLOAD,GENERAL_PUBLICATION,2022-06-29 00:00:00.0,2,1815,SJP_PUBLIC_LIST"
        );

    private static final List<String> MI_DATA_WITH_NON_DIGITS_LOCATION_ID =
        List.of(
            "0beac960-68a3-41db-9f51-8c71826eaf30,2022-07-25 14:45:18.836,2022-09-29 14:45:18.836,BI_LINGUAL,"
                + "MANUAL_UPLOAD,PUBLIC,MANUAL_UPLOAD,LIST,2022-06-29 00:00:00.0,0,null,FAMILY_DAILY_CAUSE_LIST",
            "165ca91d-1e58-412a-80f5-1e5475a093e4,2022-06-29 14:45:18.836,2022-09-29 14:45:18.836,WELSH,MANUAL_UPLOAD,"
                + "PUBLIC,MANUAL_UPLOAD,GENERAL_PUBLICATION,2022-06-29 00:00:00.0,1,NoMatch3,SJP_PUBLIC_LIST",
            "10238a0f-d398-4356-9af4-a4dbbb17d455,2022-06-29 14:45:18.836,2022-09-29 14:45:18.836,ENGLISH,"
                + "MANUAL_UPLOAD,PUBLIC,MANUAL_UPLOAD,GENERAL_PUBLICATION,2022-06-29 00:00:00.0,2,,SJP_PUBLIC_LIST"
        );

    private static final Float PAYLOAD_SIZE_WITHIN_LIMIT = 90f;
    private static final Float PAYLOAD_SIZE_OVER_LIMIT = 110f;

    @BeforeAll
    public static void setupSearchValues() {
        SEARCH_VALUES.put(TEST_KEY, List.of(TEST_VALUE));
    }

    @BeforeEach
    void setup() {
        createPayloads();

        lenient().when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),
                                                                    artefact.getContentDate(),
                                                                    artefact.getLanguage(),
                                                                    artefact.getListType(),
                                                                    artefact.getProvenance()))
            .thenReturn(Optional.empty());
        lenient().when(artefactRepository.save(artefactWithPayloadUrl)).thenReturn(artefactWithIdAndPayloadUrl);
    }

    private void createPayloads() {
        artefact = ArtefactConstantTestHelper.buildArtefact();
        artefactWithPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithPayloadUrl();
        artefactWithIdAndPayloadUrl = ArtefactConstantTestHelper.buildArtefactWithIdAndPayloadUrl();
    }

    @Test
    void testCreationOfNewArtefactWhenVenue() {
        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(any())).thenReturn(artefactWithIdAndPayloadUrl);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        verify(azureArtefactBlobService, never()).deleteBlob(anyString());
        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testCreationOfNewArtefactWhenOwningHearingLocation() {
        artefact.setListType(ListType.SSCS_DAILY_LIST);
        artefactWithPayloadUrl.setLocationId("12341234");

        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(any())).thenReturn(artefactWithIdAndPayloadUrl);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        verify(azureArtefactBlobService, never()).deleteBlob(anyString());
        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testUpdatingOfExistingArtefact() {
        Artefact existingArtefact = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .payloadSize(PAYLOAD_SIZE_WITHIN_LIMIT)
            .build();

        Artefact artefactToBeCreated = Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .locationId(PROVENANCE_ID)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .payloadSize(PAYLOAD_SIZE_WITHIN_LIMIT)
            .build();

        when(artefactRepository.findArtefactByUpdateLogic(artefactToBeCreated.getLocationId(),
                                                          artefactToBeCreated.getContentDate(),
                                                          artefactToBeCreated.getLanguage(),
                                                          artefactToBeCreated.getListType(),
                                                          artefactToBeCreated.getProvenance()))
            .thenReturn(Optional.of(existingArtefact));
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(any())).thenReturn(artefactToBeCreated);

        Artefact returnedArtefact = publicationService.createPublication(artefact, PAYLOAD);

        verify(azureArtefactBlobService).deleteBlob(anyString());
        assertEquals(artefactToBeCreated, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testUpdatingOfExistingArtefactWithNewPayloadNotWithinLimit() {
        Artefact existingArtefact = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .payloadSize(PAYLOAD_SIZE_OVER_LIMIT)
            .build();

        Artefact artefactToBeCreated = Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .provenance(PROVENANCE)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .locationId(PROVENANCE_ID)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .payload(PAYLOAD_URL)
            .payloadSize(PAYLOAD_SIZE_OVER_LIMIT)
            .build();

        when(artefactRepository.findArtefactByUpdateLogic(artefactToBeCreated.getLocationId(),
                                                          artefactToBeCreated.getContentDate(),
                                                          artefactToBeCreated.getLanguage(),
                                                          artefactToBeCreated.getListType(),
                                                          artefactToBeCreated.getProvenance()))
            .thenReturn(Optional.of(existingArtefact));
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(any())).thenReturn(artefactToBeCreated);

        Artefact returnedArtefact = publicationService.createPublication(artefactToBeCreated, PAYLOAD);

        verify(azureArtefactBlobService).deleteBlob(anyString());
        verify(publicationManagementService).deleteFiles(artefactToBeCreated.getArtefactId(),
                                                         artefactToBeCreated.getListType(),
                                                         artefactToBeCreated.getLanguage());

        assertEquals(artefactToBeCreated, returnedArtefact, ROWID_RETURNS_UUID);
    }

    @Test
    void testCreationOfNewArtefactWithFile() {
        artefactWithPayloadUrl.setSearch(null);
        artefactWithPayloadUrl.setLocationId(NO_COURT_EXISTS_IN_REFERENCE_DATA);
        when(azureArtefactBlobService.uploadFlatFile(any(), eq(FILE))).thenReturn(PAYLOAD_URL);
        when(artefactRepository.save(artefact)).thenReturn(artefactWithIdAndPayloadUrl);

        Artefact returnedArtefact = publicationService.createPublication(artefact, FILE);

        verify(azureArtefactBlobService, never()).deleteBlob(anyString());
        assertEquals(artefactWithIdAndPayloadUrl, returnedArtefact, VALIDATION_ARTEFACT_NOT_MATCH);
    }

    @Test
    void testApplyInternalLocationIdForManualUpload() {
        Artefact artefact = new Artefact();
        artefact.setLocationId(PROVENANCE_ID);
        artefact.setProvenance(MANUAL_UPLOAD_PROVENANCE);

        publicationService.applyInternalLocationId(artefact);

        verifyNoInteractions(locationRepository);
        assertThat(artefact.getLocationId()).isEqualTo(PROVENANCE_ID);

    }

    @Test
    void testApplyInternalLocationIdIfLocationExists() {
        Location location = new Location();
        location.setLocationId(Integer.valueOf(LOCATION_ID));
        when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID, LOCATION_VENUE))
            .thenReturn(Optional.of(location));

        publicationService.applyInternalLocationId(artefact);
        assertThat(artefact.getLocationId()).isEqualTo(LOCATION_ID);
    }

    @Test
    void testApplyInternalLocationIdIfLocationDoesNotExist() {
        when(locationRepository.findByLocationIdByProvenance(PROVENANCE, PROVENANCE_ID, LOCATION_VENUE))
            .thenReturn(Optional.empty());

        publicationService.applyInternalLocationId(artefact);
        assertThat(artefact.getLocationId()).isEqualTo("NoMatch" + PROVENANCE_ID);
    }

    @Test
    void testCreationOfNewArtefactWhenListTypeSjpPress() {
        publicationService.applyInternalLocationId(artefact);
        assertThat(artefact.getLocationId()).isEqualTo(NO_COURT_EXISTS_IN_REFERENCE_DATA);
    }

    @Test
    void testMaskEmail() {
        assertEquals("t*******@email.com",
                     publicationService.maskEmail("testUser@email.com"),
                     "Email was not masked correctly");
    }

    @Test
    void testMaskEmailNotValidEmail() {
        assertEquals("a****",
                     publicationService.maskEmail("abcde"),
                     "Email was not masked correctly");
    }

    @Test
    void testMaskEmailEmptyString() {
        assertEquals("",
                     publicationService.maskEmail(""),
                     "Email was not masked correctly");
    }

    @ParameterizedTest
    @MethodSource("parametersForGetMiData")
    void testGetMiData(List<String> miData, String firstLocationName, String secondLocationName) {
        when(artefactRepository.getMiData()).thenReturn(miData);

        Location locationWithId3 = new Location();
        locationWithId3.setLocationId(3);
        locationWithId3.setName(LOCATION_NAME_WITH_ID_3);

        Location locationWithId9 = new Location();
        locationWithId9.setLocationId(9);
        locationWithId9.setName(LOCATION_NAME_WITH_ID_9);

        lenient().when(locationRepository.getLocationByLocationId(3)).thenReturn(Optional.of(locationWithId3));
        lenient().when(locationRepository.getLocationByLocationId(9)).thenReturn(Optional.of(locationWithId9));
        lenient().when(locationRepository.getLocationByLocationId(1823)).thenReturn(Optional.empty());
        lenient().when(locationRepository.getLocationByLocationId(1815)).thenReturn(Optional.empty());

        String testString = publicationService.getMiData();
        String[] splitLineString = testString.split(System.lineSeparator());
        long countLine1 = splitLineString[0].chars().filter(character -> character == ',').count();

        assertThat(testString)
            .as("Header row missing")
            .contains("source_artefact_id");

        assertThat(splitLineString)
            .as("Only one line exists - data must be missing, as only headers are printing")
            .as("Wrong comma count compared to header row!")
            .hasSize(4)
            .allSatisfy(
                e -> assertThat(e.chars().filter(character -> character == ',').count()).isEqualTo(countLine1));

        assertThat(getLocationName(splitLineString[1]))
            .isEqualTo(firstLocationName);

        assertThat(getLocationName(splitLineString[2]))
            .isEqualTo(secondLocationName);

        assertThat(splitLineString[0]).contains("superseded_count");
    }

    private String getLocationName(String line) {
        return line.substring(
            line.lastIndexOf(DELIMITER, line.lastIndexOf(DELIMITER) - 1) + 1,
            line.lastIndexOf(DELIMITER)
        );
    }

    private static Stream<Arguments> parametersForGetMiData() {
        return Stream.of(
            Arguments.of(MI_DATA_WITH_VALID_LOCATION_ID, LOCATION_NAME_WITH_ID_3, LOCATION_NAME_WITH_ID_9),
            Arguments.of(MI_DATA_WITH_INVALID_LOCATION_ID, "", "")
        );
    }

    @Test
    void testGetMiDataWithNonDigitsLocationId() {
        when(artefactRepository.getMiData()).thenReturn(MI_DATA_WITH_NON_DIGITS_LOCATION_ID);
        String result = publicationService.getMiData();

        verifyNoInteractions(locationRepository);

        String[] splitLineString = result.split(System.lineSeparator());
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(getLocationName(splitLineString[1]))
            .isEmpty();

        softly.assertThat(getLocationName(splitLineString[2]))
            .isEmpty();

        softly.assertThat(getLocationName(splitLineString[3]))
            .isEmpty();

        softly.assertAll();
    }

    @Test
    void testSupersededCountIsUpdated() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),
                                                          artefact.getContentDate(),
                                                          artefact.getLanguage(),
                                                          artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.of(artefact));

        artefact.setPayload("/" + UUID.randomUUID());
        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);

        ArgumentCaptor<Artefact> captor = ArgumentCaptor.forClass(Artefact.class);
        when(artefactRepository.save(captor.capture())).thenReturn(artefactWithIdAndPayloadUrl);

        publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(1, captor.getValue().getSupersededCount(), "Superseded count has not been incremented");
    }

    @Test
    void testSupersededCountIsNotUpdated() {
        when(artefactRepository.findArtefactByUpdateLogic(artefact.getLocationId(),
                                                          artefact.getContentDate(),
                                                          artefact.getLanguage(),
                                                          artefact.getListType(),
                                                          artefact.getProvenance()))
            .thenReturn(Optional.empty());

        artefactWithPayloadUrl.setLocationId(PROVENANCE_ID);
        when(azureArtefactBlobService.createPayload(any(), eq(PAYLOAD))).thenReturn(PAYLOAD_URL);

        ArgumentCaptor<Artefact> captor = ArgumentCaptor.forClass(Artefact.class);
        when(artefactRepository.save(captor.capture())).thenReturn(artefactWithIdAndPayloadUrl);

        publicationService.createPublication(artefact, PAYLOAD);

        assertEquals(0, captor.getValue().getSupersededCount(), "Superseded count has been incremented");
    }

    @Test
    void testGetMiDataV2() {
        PublicationMiData publicationMiData = new PublicationMiData(
            UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), Language.ENGLISH, "MANUAL_UPLOAD",
            Sensitivity.PUBLIC, UUID.randomUUID().toString(), 0, ArtefactType.GENERAL_PUBLICATION,
            LocalDateTime.now(),"1", ListType.CIVIL_DAILY_CAUSE_LIST);

        PublicationMiData publicationMiData2 = new PublicationMiData(
            UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), Language.ENGLISH, "MANUAL_UPLOAD",
            Sensitivity.PUBLIC, UUID.randomUUID().toString(), 1, ArtefactType.GENERAL_PUBLICATION,
            LocalDateTime.now(), "NoMatch2", ListType.CIVIL_DAILY_CAUSE_LIST);

        when(artefactRepository.getMiDataV2()).thenReturn(List.of(publicationMiData, publicationMiData2));

        List<PublicationMiData> publicationMiDataList = publicationService.getMiDataV2();

        assertThat(publicationMiDataList).containsExactlyInAnyOrder(publicationMiData, publicationMiData2);
    }
}
