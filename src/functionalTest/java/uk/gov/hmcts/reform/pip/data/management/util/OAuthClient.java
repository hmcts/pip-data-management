package uk.gov.hmcts.reform.pip.data.management.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static io.restassured.RestAssured.given;

@Component
public class OAuthClient {

    @Value("${CLIENT_ID_FT}")
    private String clientId;

    @Value("${CLIENT_SECRET_FT}")
    private String clientSecret;

    @Value("${TENANT_ID}")
    private String tenantId;

    @Value("${APP_URI}")
    private String scope;

    public String generateAccessToken() {
        String token = given()
            .relaxedHTTPSValidation()
            .header("content-type", "application/x-www-form-urlencoded")
            .formParam("client_id", clientId)
            .formParam("scope", scope + "/.default")
            .formParam("client_secret", clientSecret)
            .formParam("grant_type", "client_credentials")
            .baseUri("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token")
            .post()
            .body()
            .jsonPath()
            .get("access_token");

        if (token == null) {
            throw new AuthException(
                String.format(
                    "Unable to get token with %s %s %s %s %s", clientId, clientSecret, tenantId, scope
                )
            );
        }
        return token;
    }
}