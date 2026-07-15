package mu.server.rest.controller

import mu.server.service.dto.Result
import mu.server.service.dto.user.UserResponse
import mu.server.service.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(version = "1.0", value = ["/api/v1/mono/admin"], produces = ["application/json"])
class AdminController(
    private val userService: UserService,
) {
    @PreAuthorize(value = "hasAuthority('admin:read')")
    @GetMapping(value = ["/{id}"], version = "1.0", produces = ["application/json"])
    fun findUserById(
        @PathVariable id: Long,
    ): ResponseEntity<UserResponse>? {
        val result: Result<UserResponse>? = userService.findUserById(id)
        return result?.let { user ->
            if (user.success.not()) {
                ResponseEntity.notFound().build()
            } else {
                ResponseEntity.ok(
                    user.result,
                )
            }
        }
    }
}
