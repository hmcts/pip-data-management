package uk.gov.hmcts.reform.pip.data.management.config;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import lombok.extern.slf4j.Slf4j;
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
     * @param flagFiles   (optional) a list of paths to json or yaml files containing flags for launchdarkly.
     *                    If there are duplicate keys, the first files have precedence.
     * @return Launch Darkly Client.
     */
    @Bean
    public LDClient ldClient() {
        LDConfig.Builder builder = new LDConfig.Builder().offline(false);
        return new LDClient("", builder.build());
    }
}
