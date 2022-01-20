package uk.gov.hmcts.reform.pip.data.management.config;

/**
 * This class centralises the static configuration for dealing with Publications.
 */
public final class PublicationConfiguration {

    public static final String PROVENANCE_HEADER = "x-provenance";
    public static final String SOURCE_ARTEFACT_ID_HEADER = "x-source-artefact-id";
    public static final String TYPE_HEADER = "x-type";
    public static final String SENSITIVITY_HEADER = "x-sensitivity";
    public static final String LANGUAGE_HEADER = "x-language";
    public static final String DISPLAY_FROM_HEADER = "x-display-from";
    public static final String DISPLAY_TO_HEADER = "x-display-to";
    public static final String LIST_TYPE = "x-list-type";
    public static final String COURT_ID = "x-court-id";
    public static final String CONTENT_DATE = "x-content-date";

    private PublicationConfiguration() {
        //Private constructor
    }

}
