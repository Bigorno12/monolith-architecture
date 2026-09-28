package mu.server.rest.config;

import mu.server.rest.config.properties.ApiConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.resilience.annotation.EnableResilientMethods;

@Configuration
@EnableResilientMethods
@ComponentScan(basePackages = "mu.server")
@EntityScan(basePackages = "mu.server.persistence.entity")
@EnableJpaRepositories(basePackages = "mu.server.persistence.repository")
@EnableConfigurationProperties(value = {ApiConfiguration.class})
public class ApplicationConfig {
}
