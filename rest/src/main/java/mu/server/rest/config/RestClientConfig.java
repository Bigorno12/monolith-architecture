package mu.server.rest.config;

import mu.server.service.exception.InvalidCallException;
import mu.server.service.exception.JsonPlaceHolderException;
import mu.server.service.exception.NotFoundException;
import mu.server.service.service.http.JsonPlaceHolderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
                        .defaultStatusHandler(HttpStatusCode::isError, (_, response) -> {
                            switch (response.getStatusCode()) {
                                case HttpStatus.NOT_FOUND -> throw new NotFoundException("Request Not Found");
                                case HttpStatus.BAD_REQUEST -> throw new InvalidCallException("Bad Request");
                                default -> throw new JsonPlaceHolderException("JsonPlace api error");
                            }
                        })
                        .apiVersionInserter(ApiVersionInserter.builder().useHeader("X-API-Version").build())
                        .defaultHeader("Content-Type", "application/json")
                        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                );
    }
}
