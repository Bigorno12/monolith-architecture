package mu.server.rest.filter


import io.micrometer.tracing.Tracer
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TraceIdFilter(val tracer: Tracer) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        getTraceId().also { response.setHeader("X-Trace-Id", it) }
        filterChain.doFilter(request, response)
    }

    private fun getTraceId(): String? = tracer.currentTraceContext().context()?.traceId()
}