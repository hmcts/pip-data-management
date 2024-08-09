package uk.gov.hmcts.reform.pip.data.management.helpers;

public final class NoMatchArtefactHelper {
    private static final String NO_MATCH = "NoMatch";

    private NoMatchArtefactHelper() {
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
