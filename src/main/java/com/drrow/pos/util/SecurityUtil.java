package com.drrow.pos.util;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security utility for password hashing, verification, and token handling.
 * Integrates standard BCrypt (OpenBSD BCrypt) with SHA-256 salted fallback.
 */
public class SecurityUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Hash a plain-text password using standard BCrypt ($2y$ / $2a$) if available, or secure salted hash.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Try BouncyCastle OpenBSDBCrypt via reflection
        try {
            Class<?> bcClass = Class.forName("org.bouncycastle.crypto.generators.OpenBSDBCrypt");
            Method genMethod = bcClass.getMethod("generate", char[].class, byte[].class, int.class);
            byte[] salt = new byte[16];
            SECURE_RANDOM.nextBytes(salt);
            return (String) genMethod.invoke(null, plainPassword.toCharArray(), salt, 10);
        } catch (Throwable t) {
            // Fallback to standard secure SHA-256 salted hash
            return hashSha256Salted(plainPassword);
        }
    }

    /**
     * Check whether a plain-text password matches a stored BCrypt or salted hash.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        // 1. If BCrypt format ($2a$, $2b$, $2y$)
        if (hashedPassword.startsWith("$2")) {
            try {
                Class<?> bcClass = Class.forName("org.bouncycastle.crypto.generators.OpenBSDBCrypt");
                Method checkMethod = bcClass.getMethod("checkPassword", String.class, char[].class);
                return (Boolean) checkMethod.invoke(null, hashedPassword, plainPassword.toCharArray());
            } catch (Throwable ignored) {}
        }

        // 2. If SHA-256 Salted format ($sha256$salt$hash)
        if (hashedPassword.startsWith("$sha256$")) {
            return verifySha256Salted(plainPassword, hashedPassword);
        }

        // 3. Fallback check for development passwords if unhashed
        return plainPassword.equals(hashedPassword);
    }

    private static String hashSha256Salted(String password) {
        try {
            byte[] salt = new byte[16];
            SECURE_RANDOM.nextBytes(salt);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return "$sha256$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed: " + e.getMessage(), e);
        }
    }

    private static boolean verifySha256Salted(String password, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4) return false;
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] actualHash = md.digest(password.getBytes(StandardCharsets.UTF_8));

            int diff = expectedHash.length ^ actualHash.length;
            for (int i = 0; i < expectedHash.length && i < actualHash.length; i++) {
                diff |= expectedHash[i] ^ actualHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isPasswordStrong(String password) {
        return password != null && password.length() >= 6;
    }

    public static String generateSecureToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
