package mu.server.service.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

@Deprecated(" Using keycloak")
interface AuthUserDetailsService : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails
}