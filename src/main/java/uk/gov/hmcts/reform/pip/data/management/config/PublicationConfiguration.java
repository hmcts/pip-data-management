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
    public static final String SEARCH_HEADER = "x-search";
    public static final String DISPLAY_FROM_HEADER = "x-display-from";
    public static final String DISPLAY_TO_HEADER = "x-display-to";

    public static final String ARTIFACT_ID_TABLE = "artefactId";
    public static final String PROVENANCE_TABLE = "provenance";
    public static final String SOURCE_ARTEFACT_ID_TABLE = "sourceArtefactId";
    public static final String TYPE_TABLE = "type";
    public static final String SENSITIVITY_TABLE = "sensitivity";
    public static final String LANGUAGE_TABLE = "language";
    public static final String SEARCH_TABLE = "search";
    public static final String DISPLAY_FROM_TABLE = "displayFrom";
    public static final String DISPLAY_TO_TABLE = "displayTo";
    public static final String PAYLOAD_TABLE = "payload";

    private PublicationConfiguration() {
        //Private constructor
    }

}
