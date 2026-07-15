package mu.server.service.dto.user

data class ViewUserProfile(
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val age: Int,
)
