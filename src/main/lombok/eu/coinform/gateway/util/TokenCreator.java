package eu.coinform.gateway.util;

import lombok.extern.slf4j.Slf4j;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * TokenCreator is a utility class for helping in creating tokens that are based on SecureRandom byte arrays.
 * Two static methods where createSafeUrlToken() creates tokens that are safe to use in URLs and
 * createSessionToken() creates a token for session cookies
 */

@Slf4j
public class TokenCreator {

    /**
     * createSessionToken() uses the Java security SecureRandom class to fill a byte[32] array with random stuff
     * @return the byte[32] array Base64 encoded as a String
     */

    public static String createSessionToken() {
        return Base64.getEncoder().encodeToString(byteArray());
    }

    /**
     * createSafeUrlToken() uses the Java security SecureRandom class to fill a byte[32] array with random stuff
     * @return the byte[32] array Base64 encoded as a Url Safe String
     */
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
