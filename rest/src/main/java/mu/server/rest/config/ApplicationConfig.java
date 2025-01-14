package mu.server.rest.config;

import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "mu.server")
@EntityScan(basePackages = "mu.server.persistence.entity")
@EnableEntityViews(basePackages = "mu.server.persistence.repository")
@EnableJpaRepositories(basePackages = "mu.server.persistence.repository")
public class ApplicationConfig {
}
