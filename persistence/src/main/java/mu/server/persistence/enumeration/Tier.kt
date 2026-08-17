package mu.server.persistence.enumeration

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.http.HttpServletRequest
import java.time.Duration

enum class Tier(private val capacity: Long, val window: Duration) {
    AUTH(10, Duration.ofMinutes(1)),
    API(100, Duration.ofMinutes(1)),
    ;

    fun newBucket(): Bucket = Bucket.builder()
        .addLimit(
            Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, window)
                .build(),
        ).build()

    companion object {
        fun of(request: HttpServletRequest): Tier = if (request.requestURI.contains("/auth/")) AUTH else API
    }
}
