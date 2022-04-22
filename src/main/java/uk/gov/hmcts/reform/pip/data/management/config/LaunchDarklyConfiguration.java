package uk.gov.hmcts.reform.pip.data.management.config;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class LaunchDarklyConfiguration {

    /**
     * Builds the client for Launch Darkly.
     *
     * @param sdkKey      sdk key to connect to launchdarkly.
     * @param offlineMode true to use launchdarkly offline mode.
     * @return Launch Darkly Client.
     */
    @Bean
    public LDClient ldClient(@Value("${launch-darkly.sdk-key}") String sdkKey,
                             @Value("${launch-darkly.offline-mode:false}") Boolean offlineMode) {
        LDConfig.Builder builder = new LDConfig.Builder().offline(offlineMode);
        return new LDClient(sdkKey, builder.build());
    }
}
