package uk.gov.hmcts.reform.pip.data.management.helpers;

public final class ArtefactHelper {

    private ArtefactHelper() {

    }

    public static String getUuidFromUrl(String payloadUrl) {
        return payloadUrl.substring(payloadUrl.lastIndexOf('/') + 1);
    }
}
