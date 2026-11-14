package pe.edu.unsch.pnihce.consent.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class DniHasher {

    private DniHasher() {}

    public static String hash(String dni) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(dni.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
