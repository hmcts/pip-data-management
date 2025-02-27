package uk.gov.hmcts.reform.pip.data.management.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static uk.gov.hmcts.reform.pip.data.management.utils.TestUtil.BEARER;

@SpringBootTest(classes = {OAuthClient.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FunctionalTestBase {

    protected static final String CONTENT_TYPE_VALUE = "application/json";

    @Autowired
    private OAuthClient authClient;

    protected String accessToken;

    @Value("${test-url}")
    private String testUrl;

    @BeforeAll
    void setUp() {
        RestAssured.baseURI = testUrl;
        accessToken = authClient.generateAccessToken();
    }

    protected Map<String, String> getBaseHeaderMap() {
        Map<String, String> headerMap = new ConcurrentHashMap<>();
        headerMap.put(AUTHORIZATION, BEARER + accessToken);
        return headerMap;
    }

    protected Response doGetRequest(final String path, final Map<String, String> additionalHeaders) {
        return given()
            .relaxedHTTPSValidation()
            .headers(getRequestHeaders(additionalHeaders))
            .when()
            .get(path)
            .thenReturn();
    }

    protected Response doGetRequest(final String path, final Map<String, String> additionalHeaders,
                                    Map<String, Object> queryParams) {
        return given()
            .relaxedHTTPSValidation()
            .headers(getRequestHeaders(additionalHeaders))
            .queryParams(queryParams)
            .when()
            .get(path)
            .thenReturn();
    }

    protected Response doPostRequest(final String path, final Map<String, String> additionalHeaders,
                                     final String body) {
        return given()
            .relaxedHTTPSValidation()
            .headers(getRequestHeaders(additionalHeaders))
            .body(body)
            .when()
            .post(path)
            .thenReturn();
    }

    protected Response doPostRequestMultiPart(final String path, final Map<String, String> additionalHeaders,
                                              String multiPartKey, final File multipartFile) {

        if (multiPartKey.isBlank()) {
            return given()
                .relaxedHTTPSValidation()
                .headers(additionalHeaders)
                .accept("*/*")
                .multiPart(multipartFile)
                .when()
                .post(path)
                .thenReturn();
        } else {
            return given()
                .relaxedHTTPSValidation()
                .headers(additionalHeaders)
                .accept("*/*")
                .multiPart(multiPartKey, multipartFile)
                .when()
                .post(path)
                .thenReturn();
        }
    }

    protected Response doPostRequestMultiPartWithMimeType(final String path, final Map<String,
        String> additionalHeaders, String multiPartKey, final File multipartFile, String mimeType) {
        return given()
            .relaxedHTTPSValidation()
            .headers(additionalHeaders)
            .accept("*/*")
            .multiPart(multiPartKey, multipartFile, mimeType)
            .when()
            .post(path)
            .thenReturn();
    }

    protected Response doDeleteRequest(final String path, final Map<String, String> additionalHeaders) {
        return given()
            .relaxedHTTPSValidation()
            .headers(getRequestHeaders(additionalHeaders))
            .when()
            .delete(path)
            .thenReturn();
    }

    private static Map<String, String> getRequestHeaders(final Map<String, String> additionalHeaders) {
        final Map<String, String> headers = new ConcurrentHashMap<>(Map.of(CONTENT_TYPE, CONTENT_TYPE_VALUE));
        if (!CollectionUtils.isEmpty(additionalHeaders)) {
            headers.putAll(additionalHeaders);
        }
        return headers;
    }
}
