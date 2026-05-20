package mu.server.service.dto.auth

data class AuthenticationRequest(val username: String, val password: String) {
    init {
        require(username.isNotEmpty() || password.isNotEmpty()) {
            "Username or Password must not be empty"
        }
    }
}
