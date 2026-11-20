package mu.server.service.exception

class InvalidCredentialsException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
