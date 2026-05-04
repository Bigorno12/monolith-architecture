package mu.server.service.dto.auth

data class TokenResponse(val accessToken: String?, val refreshToken: String?) {
    init {
        requireNotNull(accessToken) { "accessToken must not be null" }
        requireNotNull(refreshToken) { "refreshToken must not be null" }
    }
}
