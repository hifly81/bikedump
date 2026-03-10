package org.hifly.bikedump.utility;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class ObfuscationUtility {

    private ObfuscationUtility() {}

    public static String obfuscate(String plain, String keyMaterial) {
        if (plain == null) return null;
        if (plain.isEmpty()) return "";
        byte[] data = plain.getBytes(StandardCharsets.UTF_8);
        byte[] key = keyBytes(keyMaterial);
        byte[] x = xor(data, key);
        return Base64.getEncoder().encodeToString(x);
    }

    public static String deobfuscate(String obf, String keyMaterial) {
        if (obf == null) return null;
        if (obf.isEmpty()) return "";
        byte[] data;
        try {
            data = Base64.getDecoder().decode(obf);
        } catch (IllegalArgumentException bad) {
            // not base64 -> treat as plain (backward compatibility)
            return obf;
        }
        byte[] key = keyBytes(keyMaterial);
        byte[] x = xor(data, key);
        return new String(x, StandardCharsets.UTF_8);
    }

    private static byte[] keyBytes(String keyMaterial) {
        if (keyMaterial == null) keyMaterial = "";
        // simple key derivation: UTF-8 bytes repeated
        byte[] key = keyMaterial.getBytes(StandardCharsets.UTF_8);
        if (key.length == 0) key = new byte[]{ 0x5A, 0x33, 0x12, 0x7F }; // fallback
        return key;
    }

    private static byte[] xor(byte[] data, byte[] key) {
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return out;
    }
}