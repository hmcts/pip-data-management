package uk.gov.hmcts.reform.pip.data.management.helpers;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.utils.CaseSearchTerm;
import uk.gov.hmcts.reform.pip.model.location.LocationCsv;
import uk.gov.hmcts.reform.pip.model.location.LocationType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;
import uk.gov.hmcts.reform.pip.model.publication.Sensitivity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ArtefactConstantTestHelper {

    public static final UUID ARTEFACT_ID = UUID.randomUUID();
    public static final UUID USER_ID = UUID.randomUUID();
    public static final String SOURCE_ARTEFACT_ID = "1234";
    public static final String PROVENANCE = "provenance";
    public static final String PROVENANCE_ID = "1234";
    public static final String MANUAL_UPLOAD_PROVENANCE = "MANUAL_UPLOAD";
    public static final String PAYLOAD = "This is a payload";
    public static final String PAYLOAD_URL = "https://ThisIsATestPayload";
    public static final String PAYLOAD_STRIPPED = "ThisIsATestPayload";
    public static final String LOCATION_ID = "123";
    public static final String TEST_KEY = "TestKey";
    public static final String TEST_VALUE = "TestValue";
    public static final CaseSearchTerm SEARCH_TERM_CASE_ID = CaseSearchTerm.CASE_ID;
    public static final CaseSearchTerm SEARCH_TERM_CASE_NAME = CaseSearchTerm.CASE_NAME;
    public static final CaseSearchTerm SEARCH_TERM_CASE_URN = CaseSearchTerm.CASE_URN;
    public static final CaseSearchTerm SEARCH_TERM_PARTY_NAME = CaseSearchTerm.PARTY_NAME;
    public static final Map<String, List<Object>> SEARCH_VALUES = new ConcurrentHashMap<>();
    public static final MultipartFile FILE = new MockMultipartFile("test", (byte[]) null);
    public static final String VALIDATION_ARTEFACT_NOT_MATCH = "Artefacts do not match";
    public static final String ROWID_RETURNS_UUID = "Row ID must match returned UUID";
    public static final String LOCATION_TYPE_MATCH = "Location types should match";
    public static final String DELETION_TRACK_LOG_MESSAGE = "Track: TestValue, Removed %s, at ";
    public static final String NO_COURT_EXISTS_IN_REFERENCE_DATA = "NoMatch1234";
    public static final String VALIDATION_MORE_THAN_PUBLIC = "More than the public artefact has been found";
    public static final String VALIDATION_NOT_THROWN_MESSAGE = "Expected exception has not been thrown";
    public static final String TEST_FILE = "Hello";
    public static final String SUCCESSFUL_TRIGGER = "success - subscription sent";
    public static final String SUCCESS = "Success";

    public static final LocalDateTime CONTENT_DATE = LocalDateTime.now();
    public static final LocalDateTime START_OF_TODAY_CONTENT_DATE = LocalDateTime.now().toLocalDate().atStartOfDay();

    public static final String LOCATION_VENUE = LocationType.VENUE.name();

    public static final String LOCATION_OWNING_HEARING_LOCATION = LocationType.OWNING_HEARING_LOCATION.name();

    public static Artefact buildArtefact() {
        return Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();
    }

    public static Artefact buildArtefactWithPayloadUrl() {
        return Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .locationId(LOCATION_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();
    }

    public static Artefact buildArtefactWithIdAndPayloadUrl() {
        return Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .locationId(LOCATION_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();
    }

    public static Artefact buildSjpPublicArtefact() {
        return Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.SJP_PUBLIC_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .displayFrom(LocalDateTime.now().plusDays(1))
            .displayTo(LocalDateTime.now().plusDays(2))
            .expiryDate(LocalDateTime.now())
            .build();
    }

    public static Artefact buildSjpPressArtefact() {
        return Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(START_OF_TODAY_CONTENT_DATE)
            .listType(ListType.SJP_PRESS_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .displayFrom(LocalDateTime.now().plusDays(1))
            .displayTo(LocalDateTime.now().plusDays(2))
            .expiryDate(LocalDateTime.now())
            .build();
    }

    public static Artefact buildClassifiedPayloads() {
        return Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .locationId(PROVENANCE_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .build();
    }

    public static Artefact buildArtefactWithPayloadUrlClassified() {
        return Artefact.builder()
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now())
            .displayTo(null)
            .locationId(LOCATION_ID)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.CLASSIFIED)
            .build();
    }

    public static Artefact buildArtefactFromThePast() {
        return Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now().minusDays(1))
            .displayTo(LocalDateTime.now().plusDays(1))
            .build();
    }

    public static Artefact buildArtefactFromNow() {
        return Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now())
            .displayTo(LocalDateTime.now().plusHours(3))
            .build();
    }

    public static Artefact buildArtefactWithNullDateTo() {
        return Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now())
            .displayTo(null)
            .build();
    }

    public static Artefact buildArtefactWithSameDateFromAndTo() {
        return Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now())
            .displayTo(LocalDateTime.now())
            .build();
    }

    public static Artefact buildArtefactInTheFuture() {
        return Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .displayFrom(LocalDateTime.now().plusDays(1))
            .displayTo(LocalDateTime.now().plusDays(2))
            .build();
    }

    public static Artefact buildNoMatchArtefact() {
        return Artefact.builder()
            .artefactId(ARTEFACT_ID)
            .sourceArtefactId(SOURCE_ARTEFACT_ID)
            .provenance(PROVENANCE)
            .payload(PAYLOAD_URL)
            .search(SEARCH_VALUES)
            .locationId(NO_COURT_EXISTS_IN_REFERENCE_DATA)
            .contentDate(CONTENT_DATE)
            .listType(ListType.CIVIL_DAILY_CAUSE_LIST)
            .language(Language.ENGLISH)
            .sensitivity(Sensitivity.PUBLIC)
            .build();
    }

    public static Location initialiseCourts() {
        LocationCsv locationCsvFirstExample = new LocationCsv();
        locationCsvFirstExample.setLocationName("Court Name First Example");
        locationCsvFirstExample.setProvenanceLocationType("venue");
        Location location = new Location(locationCsvFirstExample);
        location.setLocationId(1234);
        return location;
    }

    private ArtefactConstantTestHelper() {

    }
}
