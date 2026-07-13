package mu.server.rest.config

import mu.server.service.dto.auth.TokenResponse
import mu.server.service.service.KeycloakTokenProvider
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
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
    @Value($$"${application.keycloak.server-url}") val serverUrl: String,
    @Value($$"${application.keycloak.realm}") val realm: String,
    @Value($$"${application.keycloak.client-id}") val clientId: String,
    @Value($$"${application.keycloak.client-secret}") val clientSecret: String,
) : KeycloakTokenProvider {
    @Bean
    fun adminKeycloak(): Keycloak =
        KeycloakBuilder
            .builder()
            .serverUrl(serverUrl)
            .realm(realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .resteasyClient(ResteasyClientBuilder.newBuilder().build())
            .build()

    @Bean
    fun realmResource(keycloak: Keycloak): RealmResource = keycloak.realm(realm)

    @Bean
    fun usersResource(realmResource: RealmResource): UsersResource = realmResource.users()

    override fun getToken(
        username: String,
        password: String,
    ): TokenResponse {
        val keycloakClient =
            KeycloakBuilder
                .builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .resteasyClient(ResteasyClientBuilder.newBuilder().build())
                .build()

        keycloakClient.use { client ->
            val tokenPayload = client.tokenManager().accessToken
            return TokenResponse(tokenPayload.token, tokenPayload.refreshToken)
        }
    }
}
