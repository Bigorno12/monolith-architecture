package mu.server.service.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

interface AuthUserDetailsService : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails
}