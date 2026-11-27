package uk.gov.hmcts.reform.pip.data.management.models.publication;

/**
 * Whether a file expected for a publication could be retrieved from blob storage.
 */
public enum PublicationFileStatus {
    FOUND,
    NOT_FOUND,
    ERROR
}
