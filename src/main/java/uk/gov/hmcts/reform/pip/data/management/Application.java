package uk.gov.hmcts.reform.pip.data.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationProperties;
import uk.gov.hmcts.reform.pip.data.management.config.SearchConfiguration;
import uk.gov.hmcts.reform.pip.data.management.config.ValidationConfiguration;

@SpringBootApplication
@EnableConfigurationProperties({
    AzureBlobConfigurationProperties.class,
    SearchConfiguration.class,
    ValidationConfiguration.class
})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
