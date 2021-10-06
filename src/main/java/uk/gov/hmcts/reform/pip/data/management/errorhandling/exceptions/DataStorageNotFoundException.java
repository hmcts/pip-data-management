package uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions;

public class DataStorageNotFoundException extends RuntimeException  {
    private static final long serialVersionUID = -7362402549762812559L;

    /**
     * Constructor for the Exception.
     * @param message The message to return to the end user
     */
    public DataStorageNotFoundException(String message) {
        super(message);
    }
}
