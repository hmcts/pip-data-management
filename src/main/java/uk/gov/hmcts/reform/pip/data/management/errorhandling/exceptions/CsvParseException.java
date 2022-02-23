package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

/**
 * This exception class handles any issues when parsing the CSV file.
 */
public class CsvParseException extends RuntimeException {

    private static final long serialVersionUID = -1699520570197288202L;

    public CsvParseException(String message) {
        super("Failed to parse CSV File due to: " + message);
    }

}
