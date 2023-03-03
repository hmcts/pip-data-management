package uk.gov.hmcts.reform.pip.data.management.helpers;

public final class LocationHelper {
    private static final String NO_MATCH = "NoMatch";

    private LocationHelper() {
    }

    public static String buildNoMatchLocationId(String unmatchedLocationId) {
        return NO_MATCH + unmatchedLocationId;
    }

    public static String getLocationIdForNoMatch(String locationId) {
        return locationId.split(NO_MATCH)[1];
    }

    public static boolean isNoMatchLocationId(String locationId) {
        return locationId.startsWith(NO_MATCH);
    }
}
