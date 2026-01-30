package mu.server.rest.config;

import mu.server.service.service.http.JsonPlaceHolderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "jsonplaceholder", types = {JsonPlaceHolderService.class})
public class RestClientConfig {

    private static final String URL = "https://jsonplaceholder.typicode.com";

    @Bean
    public RestClientHttpServiceGroupConfigurer configurer() {
        return groups -> groups.filterByName("jsonplaceholder")
                .forEachClient((_, restClient) -> restClient.baseUrl(URL)
                        .apiVersionInserter(ApiVersionInserter.builder().useHeader("X-API-Version").build())
                        .defaultHeader("Content-Type", "application/json")
                        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                );
    }
}
