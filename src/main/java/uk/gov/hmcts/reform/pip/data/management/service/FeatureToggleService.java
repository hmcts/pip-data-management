package uk.gov.hmcts.reform.pip.data.management.service;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class FeatureToggleService {

    private final LDClientInterface internalClient;
    private final String environment;

    @Autowired
    public FeatureToggleService(LDClientInterface internalClient) {
        this.internalClient = internalClient;
        this.environment = "Test";
        //this.close();
    }

    public boolean isFeatureEnabled(String feature) {
        return internalClient.boolVariation(feature, createLdUser().build(), false);
    }

    public LDUser.Builder createLdUser() {
        return new LDUser.Builder("pip-user")
            .custom("timestamp", String.valueOf(System.currentTimeMillis()))
            .custom("environment", environment);
    }

    private void close() {
        try {
            internalClient.close();
        } catch (IOException e) {
            log.error("Error in closing the Launchdarkly client::", e);
        }
    }
}
