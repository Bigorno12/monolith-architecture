package mu.server.service.dto

data class Result<T>(val result: T? = null, val error: String? = null, val success: Boolean) {
    companion object {
        fun <T> ok(value: T): Result<T> = Result(value, null, true)

        fun <T> failure(error: String): Result<T> = Result(null, error, false)
    }
}
