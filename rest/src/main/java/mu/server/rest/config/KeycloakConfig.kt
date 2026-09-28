package mu.server.rest.config

import mu.server.rest.config.properties.ApiConfiguration
import mu.server.service.KeycloakTokenProvider
import mu.server.service.dto.auth.TokenResponse
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UsersResource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakConfig(val apiConfiguration: ApiConfiguration) : KeycloakTokenProvider {
    @Bean
    fun adminKeycloak(): Keycloak = KeycloakBuilder
        .builder()
        .serverUrl(apiConfiguration.serverUrl)
        .realm(apiConfiguration.realm)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(apiConfiguration.clientId)
        .clientSecret(apiConfiguration.clientSecret)
        .resteasyClient(ResteasyClientBuilder.newBuilder().build())
        .build()

    @Bean
    fun realmResource(keycloak: Keycloak): RealmResource = keycloak.realm(apiConfiguration.realm)

    @Bean
    fun usersResource(realmResource: RealmResource): UsersResource = realmResource.users()

    @Bean
    fun apiConfiguration(): ApiConfiguration = ApiConfiguration()

    override fun getToken(
        username: String,
        password: String,
    ): TokenResponse {
        val keycloakClient =
            KeycloakBuilder
                .builder()
                .serverUrl(apiConfiguration.serverUrl)
                .realm(apiConfiguration.realm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(apiConfiguration.clientId)
                .clientSecret(apiConfiguration.clientSecret)
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
