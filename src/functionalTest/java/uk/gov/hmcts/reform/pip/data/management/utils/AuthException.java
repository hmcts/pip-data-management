package uk.gov.hmcts.reform.pip.data.management.utils;

public class AuthException extends RuntimeException {
    private static final long serialVersionUID = -6991745899622330407L;

    public AuthException(String error) {
        super(error);
    }
}
