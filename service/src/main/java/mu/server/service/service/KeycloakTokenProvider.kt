package mu.server.service.service

import mu.server.service.dto.auth.TokenResponse

interface KeycloakTokenProvider {
    fun getToken(
        username: String,
        password: String,
    ): TokenResponse
}
