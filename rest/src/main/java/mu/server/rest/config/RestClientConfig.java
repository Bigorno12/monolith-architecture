package mu.server.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    private static final String URL = "https://jsonplaceholder.typicode.com";

    @Bean("restClient")
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(URL)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
