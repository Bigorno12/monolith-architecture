package mu.server.rest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private final RestTemplateBuilder restTemplateBuilder;

    @Autowired
    public RestTemplateConfig(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Bean
    public RestTemplate restTemplateJsonPlaceHolder() {
        return restTemplateBuilder
                .rootUri("https://jsonplaceholder.typicode.com")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
