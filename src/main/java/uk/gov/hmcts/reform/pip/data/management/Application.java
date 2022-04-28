package uk.gov.hmcts.reform.pip.data.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import uk.gov.hmcts.reform.pip.data.management.config.AzureBlobConfigurationProperties;
import uk.gov.hmcts.reform.pip.data.management.config.SearchConfiguration;
import uk.gov.hmcts.reform.pip.data.management.config.ValidationConfiguration;

import java.util.TimeZone;
import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableConfigurationProperties({
    AzureBlobConfigurationProperties.class,
    SearchConfiguration.class,
    ValidationConfiguration.class
})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    /**
     * This method sets the default timezone for the application. In order for date comparisons to work correctly,
     * the timezone for the application needs to be set to account for BST/GMT time.
     */
    @PostConstruct
    public void setupTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
    }

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

