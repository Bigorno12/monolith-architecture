package mu.server.rest.controller

import jakarta.servlet.http.HttpServletRequest
import mu.server.service.dto.auth.AuthenticationRequest
import mu.server.service.dto.auth.TokenResponse
import mu.server.service.dto.user.UserRequest
import mu.server.service.service.KeycloakService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(version = "2.0", value = ["/api/v2/auth"], produces = ["application/json"])
class AuthenticationControllerV2(private val keycloakService: KeycloakService) {

    @PostMapping(version = "2.0", value = ["/register"], produces = ["application/json"])
    fun register(
        @Validated @RequestBody userRequest: UserRequest,
        request: HttpServletRequest
    ): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(keycloakService.register(userRequest, request))

    @PostMapping(version = "2.0", value = ["/login"], produces = ["application/json"])
    fun authenticate(
        @RequestBody auth: AuthenticationRequest,
        request: HttpServletRequest
    ): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(keycloakService.authenticate(auth.username, auth.password, request))
}