package mu.server.rest.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import mu.server.service.dto.auth.AuthenticationRequest
import mu.server.service.dto.auth.AuthenticationResponse
import mu.server.service.dto.user.UserRequest
import mu.server.service.service.AuthenticationService
import mu.server.service.service.LogoutService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(version = "1.0", value = ["/api/v1/auth"], produces = ["application/json"])
class AuthenticationController(
    private val authenticationService: AuthenticationService,
    private val logoutService: LogoutService
) {

    @PostMapping(version = "1.0", value = ["/register"], produces = ["application/json"])
    fun register(@Valid @RequestBody userRequest: UserRequest): ResponseEntity<AuthenticationResponse> =
        ResponseEntity.ok(authenticationService.register(userRequest))

    @PostMapping(version = "1.0", value = ["/authenticate"], produces = ["application/json"])
    fun authenticate(@Valid @RequestBody authenticationRequest: AuthenticationRequest): ResponseEntity<AuthenticationResponse> =
        ResponseEntity.ok(authenticationService.authenticate(authenticationRequest))

    @PostMapping(version = "1.0", value = ["/refresh-token"], produces = ["application/json"])
    fun refreshToken(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<AuthenticationResponse> {
        authenticationService.refreshToken(request, response)
        return ResponseEntity.ok().build()
    }

    @PostMapping(version = "1.0", value = ["/logout"], produces = ["application/json"])
    fun logout(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Void> {
        logoutService.logout(request, response, SecurityContextHolder.getContext().authentication)
        return ResponseEntity.ok().build()
    }
}