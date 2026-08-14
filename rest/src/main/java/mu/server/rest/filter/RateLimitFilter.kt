package mu.server.rest.filter

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration

@Component
class RateLimitFilter : OncePerRequestFilter() {
    private val buckets: Cache<String, Bucket> =
        Caffeine
            .newBuilder()
            .maximumSize(MAX_TRACKED_CALLERS)
            .expireAfterAccess(IDLE_BUCKET_TTL)
            .build()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean = request.requestURI.startsWith("/actuator/")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val tier = Tier.of(request)
        val caller = callerKey(request)
        val bucket = buckets.get("${tier.name}|$caller") { tier.newBucket() }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response)
        } else {
            LOG.warn("Rate limit ({}) exceeded by {} on {}", tier.name, caller, request.requestURI)
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.setHeader(HttpHeaders.RETRY_AFTER, tier.window.toSeconds().toString())
            response.writer.write("Too many requests!")
        }
    }

    private fun callerKey(request: HttpServletRequest): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val authenticated =
            authentication != null &&
                authentication.isAuthenticated &&
                authentication !is AnonymousAuthenticationToken

        return if (authenticated) "user:${authentication.name}" else "ip:${request.remoteAddr}"
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(RateLimitFilter::class.java)
        private val IDLE_BUCKET_TTL: Duration = Duration.ofMinutes(10)
        private const val MAX_TRACKED_CALLERS = 100_000L
    }
}

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
