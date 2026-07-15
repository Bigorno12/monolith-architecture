package mu.server.rest.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.HeaderParameter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenApi(): OpenAPI = OpenAPI()
        .components(
            Components().addParameters(
                "versionHeader",
                HeaderParameter().name("X-API-Version").required(true).schema(StringSchema()),
            ),
        )
}
