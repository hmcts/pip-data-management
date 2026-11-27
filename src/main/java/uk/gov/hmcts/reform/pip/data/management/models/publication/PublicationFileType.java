package uk.gov.hmcts.reform.pip.data.management.models.publication;

/**
 * The type of a file held in blob storage for a publication.
 */
public enum PublicationFileType {
    JSON,
    FLAT_FILE,
    PDF,
    ADDITIONAL_PDF,
    EXCEL
}
