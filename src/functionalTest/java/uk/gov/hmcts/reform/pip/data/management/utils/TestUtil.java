package uk.gov.hmcts.reform.pip.data.management.utils;

import java.util.Random;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class TestUtil {
    public static final String BEARER = "Bearer ";

    private TestUtil() {
    }

    public static String randomLocationId() {
        Random number = new Random();
        Integer randomNumber = 10_000 + number.nextInt(20_000);
        return randomNumber.toString();
    }
}
