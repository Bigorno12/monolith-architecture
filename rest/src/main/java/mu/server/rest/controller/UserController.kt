package mu.server.rest.controller

import mu.server.service.dto.user.UpdateUserRequest
import mu.server.service.dto.user.ViewUserProfile
import mu.server.service.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(version = "1.0", value = ["/api/v1/mono/user"], produces = ["application/json"])
class UserController(
    private val userService: UserService,
) {
    @PreAuthorize(value = "hasAuthority('user:update') AND #username == authentication.name")
    @PutMapping(
        path = ["/update"],
        produces = ["application/json"],
        consumes = ["application/json"],
        version = "1.0",
    )
    fun updateUser(
        @RequestBody updateUserRequest: UpdateUserRequest,
        @RequestParam(name = "username") username: String,
    ): ResponseEntity<UpdateUserRequest> {
        val updateUser: UpdateUserRequest = userService.updateUser(updateUserRequest, username)
        return ResponseEntity.status(HttpStatus.OK).body(updateUser)
    }

    @PreAuthorize(value = "hasAuthority('user:delete') AND #username == authentication.name")
    @DeleteMapping(
        path = ["/delete/{username}"],
        produces = ["application/json"],
        consumes = ["application/json"],
        version = "1.0",
    )
    fun deleteUserById(
        @PathVariable username: String,
    ): ResponseEntity<Void> {
        userService.deleteUser(username)
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @PreAuthorize(value = "hasAuthority('user:read') AND #username == authentication.name")
    @GetMapping(
        path = ["/view-profile/{username}"],
        produces = ["application/json"],
        consumes = ["application/json"],
        version = "1.0",
    )
    fun viewUserProfile(
        @PathVariable username: String,
    ): ResponseEntity<ViewUserProfile> = ResponseEntity.status(HttpStatus.OK).body(userService.viewUserProfile(username))
}
