package mu.server.service.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@UtilityClass
public class FingerprintUtil {
    public String generateFingerprint(HttpServletRequest request) {
        String data = Objects.toString(request.getHeader("User-Agent"), "") +
                request.getRemoteAddr() +
                Objects.toString(request.getHeader("Accept-Language"), "") +
                Objects.toString(request.getHeader("Accept"), "") +
                Objects.toString(request.getHeader("Sec-ch-Ua"), "");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
