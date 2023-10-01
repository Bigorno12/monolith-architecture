package mu.server.rest.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "mu.server")
@EnableJpaRepositories(basePackages = "mu.server.persistence.repository")
@EntityScan(basePackages = "mu.server.persistence.entity")
public class ApplicationConfig {
}
