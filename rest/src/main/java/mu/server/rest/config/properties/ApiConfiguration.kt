package mu.server.rest.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "application.keycloak")
data class ApiConfiguration(var serverUrl: String = "", var realm: String = "", var clientId: String = "", var clientSecret: String = "")
