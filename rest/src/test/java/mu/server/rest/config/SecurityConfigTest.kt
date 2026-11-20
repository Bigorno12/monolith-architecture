package mu.server.rest.config

import mu.server.rest.filter.FingerprintFilter
import mu.server.rest.filter.RateLimitFilter
import mu.server.service.KeycloakService
import mu.server.service.TodoService
import mu.server.service.UserService
import mu.server.service.dto.auth.TokenResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    properties = [
        "WELLDEV.KEYCLOAK.CLIENT-ID=monolith-keycloak-confidential",
        "WELLDEV.KEYCLOAK.CLIENT-SECRET=unused",
        "WELLDEV.KEYCLOAK.ADMIN.CLIENT-ID=unused",
        "WELLDEV.KEYCLOAK.ADMIN.CLIENT-SECRET=unused",
        "WELLDEV.KEYCLOAK.ISSUER-URI=http://keycloak.invalid/realms/test",
        "WELLDEV.KEYCLOAK.REALM=test",
        "WELLDEV.KEYCLOAK.SERVER.URL=http://keycloak.invalid",
        "WELLDEV.RESOURCESERVER.JWT.ISSUER.URI=http://keycloak.invalid/realms/test",
        "WELLDEV.RESOURCESERVER.JWT.SET.URI=http://keycloak.invalid/realms/test/certs",
        "WELLDEV.AES.SECRET.KEY=unused",
    ],
)
@Import(
    SecurityConfig::class,
    KeycloakAuthenticationConverter::class,
    FingerprintFilter::class,
    RateLimitFilter::class,
    SecurityConfigTest.Stubs::class,
)
@MockitoBean(types = [UserService::class, TodoService::class, KeycloakService::class, JwtDecoder::class])
class SecurityConfigTest
@Autowired
constructor(
    private val mockMvc: MockMvc,
    private val keycloakService: KeycloakService,
) {
    @TestConfiguration
    class Stubs {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager("fingerprintCache")

        @Bean
        fun clientRegistrationRepository(): ClientRegistrationRepository = InMemoryClientRegistrationRepository(
            ClientRegistration
                .withRegistrationId("keycloak")
                .clientId("monolith-keycloak-confidential")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("http://keycloak.invalid/auth")
                .tokenUri("http://keycloak.invalid/token")
                .build(),
        )
    }

    @Test
    @DisplayName("login is public and needs no CSRF token")
    fun login_is_public_test() {
        given(keycloakService.authenticate(any(), any(), any())).willReturn(TokenResponse("access", "refresh"))

        mockMvc
            .perform(
                post("/api/auth/login")
                    .version("2.0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"username":"alice","password":"secret"}"""),
            ).andExpect(status().isOk)
    }

    @Test
    @DisplayName("logout keeps CSRF protection")
    fun logout_requires_csrf_test() {
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("the API answers an anonymous call with 401 from the resource-server chain, not a login redirect")
    fun api_rejects_anonymous_with_401_test() {
        mockMvc.perform(get("/api/mono/user/view-profile/alice").version("1.0")).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("a bearer token with the right permission reaches the owner's endpoint")
    fun api_accepts_bearer_token_test() {
        mockMvc
            .perform(get("/api/mono/user/view-profile/alice").version("1.0").with(bearer("alice", "user:read")))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("a user cannot read another user's profile")
    fun api_enforces_ownership_test() {
        mockMvc
            .perform(get("/api/mono/user/view-profile/bob").version("1.0").with(bearer("alice", "user:read")))
            .andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("every user's todos are for admins only")
    fun all_todos_is_admin_only_test() {
        mockMvc
            .perform(get("/api/mono/todo/all-todos").version("1.0").with(bearer("root", "admin:read")))
            .andExpect(status().isOk)
        mockMvc
            .perform(get("/api/mono/todo/all-todos").version("1.0").with(bearer("alice", "user:read")))
            .andExpect(status().isForbidden)
    }

    private fun MockHttpServletRequestBuilder.version(version: String) = header("X-API-Version", version)

    private fun bearer(
        username: String,
        vararg permissions: String,
    ) = jwt()
        .jwt { it.subject(username) }
        .authorities(permissions.map(::SimpleGrantedAuthority))
}
