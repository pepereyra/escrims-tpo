package escrims.service;

import escrims.domain.model.Usuario;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long DEFAULT_TTL_SECONDS = 60L * 60L * 4L;

    private final String secret;

    public JwtService(String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 caracteres.");
        }
        this.secret = secret;
    }

    public String emitirToken(Usuario usuario) {
        long exp = Instant.now().getEpochSecond() + DEFAULT_TTL_SECONDS;
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{"
                + "\"sub\":\"" + escape(usuario.getUsername()) + "\","
                + "\"uid\":\"" + usuario.getId() + "\","
                + "\"role\":\"" + escape(usuario.getRolSistema()) + "\","
                + "\"exp\":" + exp
                + "}";

        String headerPayload = base64Url(header.getBytes(StandardCharsets.UTF_8))
                + "."
                + base64Url(payload.getBytes(StandardCharsets.UTF_8));

        return headerPayload + "." + firmar(headerPayload);
    }

    public AuthPrincipal validar(String token) {
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Token JWT invalido.");
        }

        String headerPayload = parts[0] + "." + parts[1];
        String expectedSignature = firmar(headerPayload);
        if (!MessageDigestCompat.equals(expectedSignature, parts[2])) {
            throw new IllegalArgumentException("Firma JWT invalida.");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        long exp = Long.parseLong(extraer(payload, "exp"));
        if (Instant.now().getEpochSecond() > exp) {
            throw new IllegalArgumentException("Token JWT expirado.");
        }

        return new AuthPrincipal(
                UUID.fromString(extraer(payload, "uid")),
                extraer(payload, "sub"),
                extraer(payload, "role")
        );
    }

    private String firmar(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return base64Url(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo firmar el JWT.", e);
        }
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String extraer(String json, String field) {
        Pattern quoted = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher quotedMatcher = quoted.matcher(json);
        if (quotedMatcher.find()) {
            return quotedMatcher.group(1);
        }

        Pattern number = Pattern.compile("\"" + field + "\"\\s*:\\s*(\\d+)");
        Matcher numberMatcher = number.matcher(json);
        if (numberMatcher.find()) {
            return numberMatcher.group(1);
        }

        throw new IllegalArgumentException("Token JWT sin campo " + field + ".");
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class MessageDigestCompat {
        private static boolean equals(String a, String b) {
            return java.security.MessageDigest.isEqual(
                    a.getBytes(StandardCharsets.UTF_8),
                    b.getBytes(StandardCharsets.UTF_8)
            );
        }
    }
}
