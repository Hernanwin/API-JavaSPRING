package com.stockcomerce.stockapihb.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Implementacion RFC 7519 HS256 nativa sobre JDK (javax.crypto.Mac) sin depender de librerias
// externas acopladas a una version especifica de Jackson.
@Component
public class JwtService {

    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Pattern SUB_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern EXP_PATTERN = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");

    private final byte[] secretBytes;
    private final long expirationSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds:28800}") long expirationSeconds) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("security.jwt.secret debe tener al menos 32 bytes");
        }
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String generarToken(Long idUsuario, String username) {
        long now = Instant.now().getEpochSecond();
        long exp = now + expirationSeconds;
        String headerB64 = base64UrlEncode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String payloadJson = "{\"sub\":\"" + escapeJson(username) + "\",\"uid\":" + idUsuario
                + ",\"iat\":" + now + ",\"exp\":" + exp + "}";
        String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = headerB64 + "." + payloadB64;
        String signatureB64 = base64UrlEncode(hmacSha256(signingInput));
        return signingInput + "." + signatureB64;
    }

    // Devuelve el username ("sub") si la firma y la expiracion son validas, o null en caso contrario
    public String validarYExtraerUsername(String token) {
        if (token == null || token.isBlank()) return null;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;
        try {
            String signingInput = parts[0] + "." + parts[1];
            byte[] expectedSig = hmacSha256(signingInput);
            byte[] providedSig = Base64.getUrlDecoder().decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSig, providedSig)) {
                return null;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Matcher expMatcher = EXP_PATTERN.matcher(payload);
            if (!expMatcher.find()) return null;
            long exp = Long.parseLong(expMatcher.group(1));
            if (Instant.now().getEpochSecond() >= exp) {
                return null;
            }
            Matcher subMatcher = SUB_PATTERN.matcher(payload);
            if (!subMatcher.find()) return null;
            return unescapeJson(subMatcher.group(1));
        } catch (Exception ex) {
            return null;
        }
    }

    private byte[] hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo firmar el token JWT", ex);
        }
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescapeJson(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
