package config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class TestContainerDBConfiguration {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9.7")
            .withDatabaseName("welldev")
            .withUsername("test")
            .withPassword("password");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("keycloak_db")
            .withUsername("test")
            .withPassword("password");

    @Container
    @ServiceConnection
    static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.7.0")
            .withRealmImportFile("test-realm-export.json");

}
