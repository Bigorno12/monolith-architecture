package mu.server.rest.config

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UsersResource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakConfig(
    @Value($$"${keycloak.server-url}") val serverUrl: String,
    @Value($$"${keycloak.realm}") val realm: String,
    @Value($$"${keycloak.client-id}") val clientId: String,
    @Value($$"${keycloak.client-secret}") val clientSecret: String,
    @Value($$"${keycloak.username}") val username: String,
    @Value($$"${keycloak.password}") val password: String,
) {

    @Bean
    fun adminKeycloak(): Keycloak = KeycloakBuilder
        .builder()
        .serverUrl(serverUrl)
        .realm(realm)
        .grantType(OAuth2Constants.PASSWORD)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .username(username)
        .password(password)
        .build()

    @Bean
    fun realmResource(keycloak: Keycloak): RealmResource = keycloak.realm(realm)

    @Bean
    fun usersResource(realmResource: RealmResource): UsersResource = realmResource.users()
}