package uk.gov.hmcts.reform.pip.data.management.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.azure.storage.common.implementation.StorageImplUtils.assertNotNull;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integration-jpa")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocationMetadataRepositoryTest {

    private static final Integer LOCATION_ID = 1;

    @Autowired
    LocationMetadataRepository locationMetadataRepository;

    private UUID savedLocationMetadataId;

    @BeforeEach
    void setUp() {
        LocationMetadata locationMetadata = createTestLocationMetadata(LOCATION_ID);

        LocationMetadata saved = locationMetadataRepository.save(locationMetadata);
        savedLocationMetadataId = saved.getLocationMetadataId();
    }

    @AfterEach
    void tearDown() {
        locationMetadataRepository.deleteAll();
    }

    @Test
    void shouldFindByLocationId() {
        Optional<LocationMetadata> locationMetadata = locationMetadataRepository.findByLocationId(LOCATION_ID);

        assertAll(
            () -> assertTrue(locationMetadata.isPresent(), "Should find location by locationId"),
            () -> assertEquals(savedLocationMetadataId, locationMetadata.get().getLocationMetadataId(),
                               "Found location should have correct ID"),
            () -> assertEquals(LOCATION_ID, locationMetadata.get().getLocationId(),
                               "Found location should have correct locationId")
        );
    }

    @Test
    void shouldNotFindByNonExistentLocationId() {
        Optional<LocationMetadata> locationMetadata = locationMetadataRepository.findByLocationId(99);
        assertFalse(locationMetadata.isPresent(), "Should not find location for non-existent locationId");
    }

    @Test
    void shouldSaveNewLocationMetadata() {
        Integer locationId = 2;
        LocationMetadata newLocationMetadata = createTestLocationMetadata(locationId);
        LocationMetadata locationMetadata = locationMetadataRepository.save(newLocationMetadata);

        assertAll(
            () -> assertNotNull(locationMetadata.getLocationMetadataId().toString(),
                                "Saved entity should have generated ID"),
            () -> assertEquals(locationId, locationMetadata.getLocationId(),
                               "Saved entity should have correct locationId"),
            () -> assertTrue(locationMetadataRepository.findById(locationMetadata.getLocationMetadataId()).isPresent(),
                             "Saved entity should be retrievable from database")
        );
    }

    @Test
    void shouldFindById() {
        Optional<LocationMetadata> locationMetadata =
            locationMetadataRepository.findById(savedLocationMetadataId);

        assertAll(
            () -> assertTrue(locationMetadata.isPresent(), "Should find location by ID"),
            () -> assertEquals(savedLocationMetadataId, locationMetadata.get().getLocationMetadataId(),
                               "Found location should have correct ID"),
            () -> assertEquals(LOCATION_ID, locationMetadata.get().getLocationId(),
                               "Found location should have correct locationId")
        );
    }

    @Test
    void shouldNotFindByNonExistentId() {
        Optional<LocationMetadata> locationMetadata = locationMetadataRepository.findById(UUID.randomUUID());
        assertFalse(locationMetadata.isPresent(), "Should not find location for non-existent ID");
    }

    @Test
    void shouldDeleteLocationMetadata() {
        locationMetadataRepository.deleteById(savedLocationMetadataId);

        Optional<LocationMetadata> locationMetadata = locationMetadataRepository.findById(savedLocationMetadataId);
        assertFalse(locationMetadata.isPresent(), "Deleted location should no longer exist in database");
    }

    @Test
    void shouldUpdateLocationMetadata() {
        Integer locationId = 3;
        LocationMetadata locationMetadata = locationMetadataRepository.findById(savedLocationMetadataId).get();
        locationMetadata.setLocationId(locationId);

        locationMetadataRepository.save(locationMetadata);

        LocationMetadata updated = locationMetadataRepository.findById(savedLocationMetadataId).get();
        assertEquals(locationId, updated.getLocationId(), "LocationId should be updated");
    }

    @Test
    void testDeleteByLocationIdInShouldDeleteMultipleRecords() {
        LocationMetadata locationMetadata1 = createTestLocationMetadata(2);
        LocationMetadata locationMetadata2 = createTestLocationMetadata(3);
        LocationMetadata locationMetadata3 = createTestLocationMetadata(4);

        locationMetadataRepository.saveAll(Arrays.asList(locationMetadata1, locationMetadata2, locationMetadata3));

        assertThat(locationMetadataRepository.findAll()).hasSize(4);

        List<Integer> locationIdsToDelete = Arrays.asList(1, 2, 3);
        locationMetadataRepository.deleteByLocationIdIn(locationIdsToDelete);

        List<LocationMetadata> remainingLocationMetadata = locationMetadataRepository.findAll();
        assertThat(remainingLocationMetadata)
            .hasSize(1)
            .extracting(LocationMetadata::getLocationId)
            .containsExactly(4);
    }

    @Test
    void testDeleteByLocationIdInWithNonExistingIdsShouldNotDeleteAnything() {
        locationMetadataRepository.deleteByLocationIdIn(List.of(9999));

        assertThat(locationMetadataRepository.findAll()).hasSize(1);
    }

    private LocationMetadata createTestLocationMetadata(int locationId) {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setLocationId(locationId);
        locationMetadata.setCautionMessage("Test Caution");
        locationMetadata.setWelshCautionMessage("Test Welsh Caution");
        locationMetadata.setNoListMessage("Test No List");
        locationMetadata.setWelshNoListMessage("Test Welsh No List");
        return locationMetadata;
    }
}
