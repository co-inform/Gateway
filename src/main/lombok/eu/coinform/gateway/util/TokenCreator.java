package eu.coinform.gateway.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class TokenCreator {

    public static String createSessionToken() {
        return Base64.getEncoder().encodeToString(byteArray());
    }

    public static String createSafeUrlToken() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(byteArray());
    }

    private static byte[] byteArray(){
        byte[] bytes = new byte[32];

        try {
            SecureRandom.getInstanceStrong().nextBytes(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return bytes;
    }
}
