package mu.server.service.dto.auth

data class AuthenticationRequest(val username: String, val password: String) {
    init {
        requireNotNull(username) { "username is null" }
        require(username.isNotEmpty() || password.isNotEmpty()) {
            "Username or Password must not be empty"
        }
    }
}
