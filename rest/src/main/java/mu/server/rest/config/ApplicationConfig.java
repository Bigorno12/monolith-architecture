package mu.server.rest.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@ComponentScan(basePackages = "mu.server")
@EntityScan(basePackages = "mu.server.persistence.entity")
@EnableJpaRepositories(basePackages = "mu.server.persistence.repository")
public class ApplicationConfig {
}
