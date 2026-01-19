package mu.server.rest.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableJpaAuditing
@EnableResilientMethods
@ComponentScan(basePackages = "mu.server")
@EntityScan(basePackages = "mu.server.persistence.entity")
@EnableJpaRepositories(basePackages = "mu.server.persistence.repository")
public class ApplicationConfig {
}
