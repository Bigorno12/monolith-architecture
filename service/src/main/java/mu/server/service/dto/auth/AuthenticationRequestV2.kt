package mu.server.service.dto.auth

data class AuthenticationRequestV2(val username: String, val password: String) {
    init {
        require(username.isNotEmpty() || password.isNotEmpty()) {
            "Username or Password must not be empty"
        }
    }
}
