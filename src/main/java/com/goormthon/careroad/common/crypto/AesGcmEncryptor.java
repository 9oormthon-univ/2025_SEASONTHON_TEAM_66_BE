package com.goormthon.careroad.common.crypto;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesGcmEncryptor {
    private static final String ALG = "AES";
    private static final String TRANS = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final String keyBase64;
    private SecretKey key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmEncryptor(@Value("${app.crypto.key-base64:}") String keyBase64) {
        this.keyBase64 = keyBase64;
    }

    @PostConstruct
    void init() {
        if (keyBase64 == null || keyBase64.isBlank()) {
            throw new IllegalStateException("APP_CRYPTO_KEY not configured");
        }
        byte[] raw = Base64.getDecoder().decode(keyBase64);
        if (raw.length != 32) throw new IllegalStateException("APP_CRYPTO_KEY must be 32 bytes");
        this.key = new SecretKeySpec(raw, ALG);
    }

    public String encrypt(String plain) {
        try {
            if (plain == null) return null;
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher c = Cipher.getInstance(TRANS);
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = c.doFinal(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException("encrypt failed", e);
        }
    }

    public String decrypt(String encoded) {
        try {
            if (encoded == null) return null;
            byte[] all = Base64.getDecoder().decode(encoded);
            byte[] iv = java.util.Arrays.copyOfRange(all, 0, IV_BYTES);
            byte[] ct = java.util.Arrays.copyOfRange(all, IV_BYTES, all.length);
            Cipher c = Cipher.getInstance(TRANS);
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] pt = c.doFinal(ct);
            return new String(pt, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("decrypt failed", e);
        }
    }
}
