package mu.server.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JsonMapperConfig {

    @Bean
    public JsonMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }
}
