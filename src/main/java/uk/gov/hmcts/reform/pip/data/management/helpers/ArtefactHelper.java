package uk.gov.hmcts.reform.pip.data.management.helpers;

public final class ArtefactHelper {

    private ArtefactHelper() {

    }

    /**
     * This function will find the UUID from the payload Url.
     *
     * @param payloadUrl    Artefact payload url for blob storage.
     * @return UUID
     */
    public static String getUuidFromUrl(String payloadUrl) {
        return payloadUrl.substring(payloadUrl.lastIndexOf('/') + 1);
    }
}
