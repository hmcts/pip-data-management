package uk.gov.hmcts.reform.pip.data.management.helpers;

import com.google.common.base.CaseFormat;
import org.apache.commons.text.WordUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoMatchArtefactHelperTest {
    private static final String STANDARD_LOCATION_ID = "123";
    private static final String NO_MATCH_LOCATION_ID = "NoMatch" + STANDARD_LOCATION_ID;

    @Test
    void shouldBuildNoMatchLocationId() {
        String formattedFieldNames = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "insolvency&CompaniesCourt").replaceAll("_", " ");
        String formattedFieldName = WordUtils.capitalize("patentsCourt",    ',');
        assertThat(NoMatchArtefactHelper.buildNoMatchLocationId(STANDARD_LOCATION_ID)).isEqualTo(NO_MATCH_LOCATION_ID);
    }

    @Test
    void shouldGetLocationIdForNoMatch() {
        assertThat(NoMatchArtefactHelper.getLocationIdForNoMatch(NO_MATCH_LOCATION_ID)).isEqualTo(STANDARD_LOCATION_ID);
    }

    @Test
    void shouldReturnTrueIfLocationIdStartsWithNoMatch() {
        assertThat(NoMatchArtefactHelper.isNoMatchLocationId(NO_MATCH_LOCATION_ID)).isTrue();
    }

    @Test
    void shouldReturnFalseIfLocationIdStartsWithNoMatch() {
        assertThat(NoMatchArtefactHelper.isNoMatchLocationId(STANDARD_LOCATION_ID)).isFalse();
    }
}
