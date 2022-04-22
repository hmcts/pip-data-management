package uk.gov.hmcts.reform.pip.data.management.service;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class FeatureToggleService {

    private final LDClientInterface internalClient;
    private final String environment;

    @Autowired
    public FeatureToggleService(LDClientInterface internalClient, @Value("${launch-darkly.env}") String environment) {
        this.internalClient = internalClient;
        this.environment = environment;
    }

    public boolean isFeatureEnabled(String feature) {
        return internalClient.boolVariation(feature, createLdUser().build(), false);
    }

    public LDUser.Builder createLdUser() {
        return new LDUser.Builder("pip-user")
            .custom("timestamp", String.valueOf(System.currentTimeMillis()))
            .custom("environment", environment);
    }
}
