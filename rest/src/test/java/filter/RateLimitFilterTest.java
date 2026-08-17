package filter;

import jakarta.servlet.ServletException;
import mu.server.rest.filter.RateLimitFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RateLimitFilterTest {

    private static final int API_CAPACITY = 100;
    private static final int AUTH_CAPACITY = 10;

    private static final int OK = 200;
    private static final int TOO_MANY_REQUESTS = 429;


    private final RateLimitFilter filter = new RateLimitFilter();

    @Test
    @DisplayName("one caller exhausting its allowance does not block another caller")
    void buckets_are_per_caller_test() throws ServletException, IOException {
        for (int i = 0; i < API_CAPACITY; i++) {
            assertEquals(OK, send("/api/v1/mono/user/view-profile/test", "10.0.0.1"));
        }

        assertEquals(TOO_MANY_REQUESTS, send("/api/v1/mono/user/view-profile/test", "10.0.0.1"));
        assertEquals(OK, send("/api/v1/mono/user/view-profile/test", "10.0.0.2"));
    }

    @Test
    @DisplayName("the public auth endpoints get a tighter allowance than the rest of the API")
    void auth_endpoints_are_limited_separately_test() throws ServletException, IOException {
        for (int i = 0; i < AUTH_CAPACITY; i++) {
            assertEquals(OK, send("/api/v2/auth/login", "10.0.0.3"));
        }

        MockHttpServletResponse blocked = call("/api/v2/auth/login", "10.0.0.3");
        assertEquals(TOO_MANY_REQUESTS, blocked.getStatus());
        assertNotNull(blocked.getHeader("Retry-After"));

        // the auth allowance is spent, the API allowance for the same caller is untouched
        assertEquals(OK, send("/api/v1/mono/todo/all-todos/test", "10.0.0.3"));
    }

    @Test
    @DisplayName("probe traffic is never rate limited")
    void actuator_is_not_limited_test() throws ServletException, IOException {
        for (int i = 0; i < API_CAPACITY + 1; i++) {
            assertEquals(OK, send("/actuator/health/liveness", "10.0.0.4"));
        }
    }

    private int send(String uri, String remoteAddress) throws ServletException, IOException {
        return call(uri, remoteAddress).getStatus();
    }

    private MockHttpServletResponse call(String uri, String remoteAddress) throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRemoteAddr(remoteAddress);

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());

        return response;
    }
}
