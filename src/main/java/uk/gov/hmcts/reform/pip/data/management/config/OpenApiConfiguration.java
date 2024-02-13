package uk.gov.hmcts.reform.pip.data.management.config;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.HEADER;
import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;

@Configuration
@SecurityScheme(name = "Bearer authentication", type = HTTP, scheme = "bearer", bearerFormat = "JWT", in = HEADER)
public class OpenApiConfiguration {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
            .info(new Info().title("CaTH Data Management Service")
                      .description("Use this service for CRUD operations of court publications and Locations.")
                      .version("1.0.0")
                      .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .externalDocs(new ExternalDocumentation()
                              .description("README")
                              .url("https://github.com/hmcts/pip-data-management"));
    }
}
