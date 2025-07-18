package uk.gov.hmcts.reform.pip.data.management.helpers;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationReference;
import uk.gov.hmcts.reform.pip.model.location.LocationType;

class TestingSupportLocationHelperTest {
    private static final Integer TEST_LOCATION_ID = 1234;
    private static final String TEST_LOCATION_NAME = "1234_Court";

    @Test
    void testCreateLocation() {
        Location result = TestingSupportLocationHelper.createLocation(TEST_LOCATION_ID, TEST_LOCATION_NAME);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getLocationId())
            .as("Location ID does not match")
            .isEqualTo(TEST_LOCATION_ID);

        softly.assertThat(result.getName())
            .as("Location name does not match")
            .isEqualTo(TEST_LOCATION_NAME);

        softly.assertThat(result.getWelshName())
            .as("Welsh location name does not match")
            .isEqualTo(TEST_LOCATION_NAME);

        softly.assertThat(result.getRegion())
            .as("Region does not match")
            .hasSize(1)
            .first()
            .isEqualTo("South East");

        softly.assertThat(result.getJurisdiction())
            .as("Jurisdiction does not match")
            .hasSize(4)
            .containsExactly("Civil",
                             "Family",
                             "Crime",
                             "Tribunal");

        softly.assertThat(result.getJurisdictionType())
            .as("Jurisdiction type does not match")
            .hasSize(4)
            .containsExactly("Civil Court",
                             "Family Court",
                             "Magistrates Court",
                             "Employment Tribunal");

        softly.assertThat(result.getLocationType())
            .as("Location type does not match")
            .isEqualTo(LocationType.VENUE);

        softly.assertThat(result.getLocationReferenceList())
            .as("Location reference list does not match")
            .hasSize(1)
            .first()
            .isEqualTo(new LocationReference("ListAssist", "3482", LocationType.VENUE));

        softly.assertAll();
    }
}
