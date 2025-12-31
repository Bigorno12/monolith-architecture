package mu.server.service.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@UtilityClass
public class FingerprintUtil {
    public String generateFingerprint(HttpServletRequest request) {
        String data = request.getHeader("User-Agent") + request.getRemoteAddr() +
                request.getHeader("Accept-Language") + request.getHeader("Accept")
                + request.getHeader("Sec-ch-Ua");
        return DigestUtils.md5DigestAsHex(data.getBytes(StandardCharsets.UTF_8));
    }
}

