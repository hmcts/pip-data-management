
package uk.gov.hmcts.reform.pip.data.management.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Random;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class TestUtil {
    public static final String BEARER = "Bearer ";

    private TestUtil() {
    }

    public static ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    public static String randomLocationId() {
        Random number = new Random(System.currentTimeMillis());
        Integer randomNumber = 10_000 + number.nextInt(20_000);
        return randomNumber.toString();
    }
}
