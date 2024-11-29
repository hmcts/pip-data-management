package uk.gov.hmcts.reform.pip.data.management.database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.model.location.LocationType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-jpa")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocationRepositoryTest {
    private static final String COURT_NAME1 = "Slough County Court and Family Court";
    private static final String COURT_NAME2 = "Leeds Social Security and Child Support";
    private static final String COURT_NAME3 = "Reading County Court and Family Court";
    private static final String COURT_NAME4 = "Cardiff Social Security and Child Support";
    private static final String COURT_NAME5 = "Milton Keynes County and Family Court";
    private static final String SOUTH_EAST_REGION = "South East";
    private static final String SOUTH_EAST_REGION_WELSH = "De-ddwyrain Lloegr";
    private static final String YORKSHIRE_REGION = "Yorkshire";
    private static final String YORKSHIRE_REGION_WELSH = "Swydd Efrog Lloegr";
    private static final String WALES_REGION = "Wales";
    private static final String WALES_REGION_WELSH = "Cymru";
    private static final String FAMILY_JURISDICTION = "Family";
    private static final String FAMILY_JURISDICTION_WELSH = "Llys Teulu";
    private static final String CIVIL_JURISDICTION = "Civil";
    private static final String CIVIL_JURISDICTION_WELSH = "Llys Sifil";
    private static final String SSCS_JURISDICTION = "Social Security and Child Support";
    private static final String SSCS_JURISDICTION_WELSH = "Tribiwnlys Nawdd Cymdeithasol a Chynnal Plant";
    private static final String PROVENANCE = "List Assist";
    private static final String LOCATION_MATCHED_MESSAGE = "Location does not match";
    private static final String LOCATION_EMPTY_MESSAGE = "Location is not empty";

    @Autowired
    LocationRepository locationRepository;

    @BeforeAll
    void setup() {
        Location location1 = new Location();
        location1.setLocationId(1);
        location1.setName(COURT_NAME1);
        location1.setLocationType(LocationType.VENUE);
        location1.setRegion(List.of(SOUTH_EAST_REGION));
        location1.setWelshRegion(List.of(SOUTH_EAST_REGION_WELSH));
        location1.setJurisdiction(List.of(FAMILY_JURISDICTION, CIVIL_JURISDICTION));
        location1.setWelshJurisdiction(List.of(FAMILY_JURISDICTION_WELSH, CIVIL_JURISDICTION_WELSH));
        location1.setLocationReferenceList(List.of(new LocationReference(PROVENANCE, "1", LocationType.VENUE)));

        Location location2 = new Location();
        location2.setLocationId(2);
        location2.setName(COURT_NAME2);
        location2.setLocationType(LocationType.OWNING_HEARING_LOCATION);
        location2.setRegion(List.of(YORKSHIRE_REGION));
        location2.setWelshRegion(List.of(YORKSHIRE_REGION_WELSH));
        location2.setJurisdiction(List.of(SSCS_JURISDICTION));
        location2.setWelshJurisdiction(List.of(SSCS_JURISDICTION_WELSH));
        location2.setLocationReferenceList(List.of(
            new LocationReference(PROVENANCE, "2", LocationType.OWNING_HEARING_LOCATION)
        ));

        Location location3 = new Location();
        location3.setLocationId(3);
        location3.setName(COURT_NAME3);
        location3.setLocationType(LocationType.VENUE);
        location3.setRegion(List.of(SOUTH_EAST_REGION));
        location3.setWelshRegion(List.of(SOUTH_EAST_REGION_WELSH));
        location3.setJurisdiction(List.of(FAMILY_JURISDICTION, CIVIL_JURISDICTION));
        location3.setWelshJurisdiction(List.of(FAMILY_JURISDICTION_WELSH, CIVIL_JURISDICTION_WELSH));
        location3.setLocationReferenceList(List.of(new LocationReference(PROVENANCE, "3", LocationType.VENUE)));

        Location location4 = new Location();
        location4.setLocationId(4);
        location4.setName(COURT_NAME4);
        location4.setLocationType(LocationType.OWNING_HEARING_LOCATION);
        location4.setRegion(List.of(WALES_REGION));
        location4.setWelshRegion(List.of(WALES_REGION_WELSH));
        location4.setJurisdiction(List.of(SSCS_JURISDICTION));
        location4.setWelshJurisdiction(List.of(SSCS_JURISDICTION_WELSH));
        location4.setLocationReferenceList(List.of(
            new LocationReference(PROVENANCE, "4", LocationType.OWNING_HEARING_LOCATION)
        ));

        Location location5 = new Location();
        location5.setLocationId(5);
        location5.setName(COURT_NAME5);
        location5.setLocationType(LocationType.VENUE);
        location5.setRegion(List.of(SOUTH_EAST_REGION));
        location5.setWelshRegion(List.of(SOUTH_EAST_REGION_WELSH));
        location5.setJurisdiction(List.of(FAMILY_JURISDICTION, CIVIL_JURISDICTION));
        location5.setWelshJurisdiction(List.of(FAMILY_JURISDICTION_WELSH, CIVIL_JURISDICTION_WELSH));
        location5.setLocationReferenceList(List.of(new LocationReference(PROVENANCE, "5", LocationType.VENUE)));

        locationRepository.saveAll(List.of(location1, location2, location3, location4, location5));
    }

    @AfterAll
    void shutdown() {
        locationRepository.deleteAll();
    }

    @Test
    void shouldFindLocationByRegionAndJurisdictionOrderByName() {
        assertThat(locationRepository.findByRegionAndJurisdictionOrderByName(SOUTH_EAST_REGION, CIVIL_JURISDICTION))
            .as(LOCATION_MATCHED_MESSAGE)
            .hasSize(3)
            .extracting(Location::getName)
            .containsExactly(COURT_NAME5, COURT_NAME3, COURT_NAME1);
    }

    @Test
    void shouldFindLocationByRegionOnlyOrderByName() {
        assertThat(locationRepository.findByRegionAndJurisdictionOrderByName(YORKSHIRE_REGION, ""))
            .as(LOCATION_MATCHED_MESSAGE)
            .hasSize(1)
            .extracting(Location::getName)
            .containsExactly(COURT_NAME2);
    }

    @Test
    void shouldFindLocationByJurisdictionOnlyOrderByName() {
        assertThat(locationRepository.findByRegionAndJurisdictionOrderByName("", SSCS_JURISDICTION))
            .as(LOCATION_MATCHED_MESSAGE)
            .hasSize(2)
            .extracting(Location::getName)
            .containsExactly(COURT_NAME4, COURT_NAME2);
    }

    @Test
    void shouldNotFindLocationIfEitherRegionOrJurisdictionUnmatched() {
        assertThat(locationRepository.findByRegionAndJurisdictionOrderByName(SOUTH_EAST_REGION, SSCS_JURISDICTION))
            .as(LOCATION_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldFindLocationByWelshRegionAndWelshJurisdictionOrderByName() {
        assertThat(locationRepository.findByWelshRegionAndJurisdictionOrderByName(SOUTH_EAST_REGION_WELSH,
                                                                                  FAMILY_JURISDICTION_WELSH))
            .as(LOCATION_MATCHED_MESSAGE)
            .hasSize(3)
            .extracting(Location::getName)
            .containsExactly(COURT_NAME5, COURT_NAME3, COURT_NAME1);
    }

    @Test
    void shouldFindLocationByWelshRegionOnlyOrderByName() {
        assertThat(locationRepository.findByWelshRegionAndJurisdictionOrderByName(WALES_REGION_WELSH, ""))
            .as(LOCATION_MATCHED_MESSAGE)
            .hasSize(1)
            .extracting(Location::getName)
            .containsExactly(COURT_NAME4);
    }

    @Test
    void shouldFindLocationByWelshJurisdictionOnlyOrderByName() {
        assertThat(locationRepository.findByWelshRegionAndJurisdictionOrderByName("", SSCS_JURISDICTION_WELSH))
            .as(LOCATION_MATCHED_MESSAGE)
            .hasSize(2)
            .extracting(Location::getName)
            .containsExactly(COURT_NAME4, COURT_NAME2);
    }

    @Test
    void shouldNotFindLocationIfEitherWelshRegionOrWelshJurisdictionUnmatched() {
        assertThat(locationRepository.findByWelshRegionAndJurisdictionOrderByName(
            YORKSHIRE_REGION_WELSH, CIVIL_JURISDICTION_WELSH))
            .as(LOCATION_EMPTY_MESSAGE)
            .isEmpty();
    }

    @Test
    void shouldFindByLocationIdByProvenance() {
        assertThat(locationRepository.findByLocationIdByProvenance(PROVENANCE, "1", LocationType.VENUE.toString()))
            .as(LOCATION_MATCHED_MESSAGE)
            .isPresent()
            .hasValueSatisfying(l -> COURT_NAME1.equals(l.getName()));
    }

    @Test
    void shouldNotFindByLocationIdByProvenanceIfNotAllValuesMatched() {
        assertThat(locationRepository.findByLocationIdByProvenance(PROVENANCE, "2", LocationType.VENUE.toString()))
            .as(LOCATION_EMPTY_MESSAGE)
            .isEmpty();

        assertThat(locationRepository.findByLocationIdByProvenance(PROVENANCE, "1",
                                                                   LocationType.OWNING_HEARING_LOCATION.toString()))
            .as(LOCATION_EMPTY_MESSAGE)
            .isEmpty();
    }
}
