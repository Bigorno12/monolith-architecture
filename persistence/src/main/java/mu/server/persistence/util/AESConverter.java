package mu.server.persistence.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AESConverter {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_SIZE = 12;

    private final SecretKey secretKey;

    public AESConverter(@Value("${application.aes.secret-key}") String secret) {
        this.secretKey = new SecretKeySpec(Arrays.copyOf(secret.getBytes(), 16), "AES");
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, iv);
            byte[] cipherText = cipher.doFinal(plainText.getBytes());

            byte[] encrypted = new byte[IV_SIZE + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, IV_SIZE);
            System.arraycopy(cipherText, 0, encrypted, IV_SIZE, cipherText.length);

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] iv = Arrays.copyOf(decoded, IV_SIZE);

            Cipher cipher = initCipher(Cipher.DECRYPT_MODE, iv);
            byte[] plainText = cipher.doFinal(decoded, IV_SIZE, decoded.length - IV_SIZE);

            return new String(plainText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Cipher initCipher(int mode, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(mode, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher;
    }
}