package clientServer.cipher;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class CipherString {
    private static final byte[] key = {53, -57, 54, -102, 4, 47, -111, -33, -43, -120, 120, -121, 31, -89, -85, -127};
    private static final Key symmetricKey = new SecretKeySpec(key, "AES");
    private Cipher encodingCypher;
    private Cipher decodingCypher;

    public CipherString() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.encodingCypher = Cipher.getInstance("AES");
        encodingCypher.init(Cipher.ENCRYPT_MODE, symmetricKey);
        this.decodingCypher = Cipher.getInstance("AES");
        decodingCypher.init(Cipher.DECRYPT_MODE, symmetricKey);
    }

    public byte[] encrypt(String message) throws IllegalBlockSizeException, BadPaddingException {
        var plainMessage = message.getBytes(StandardCharsets.UTF_8);
        return this.encodingCypher.doFinal(plainMessage);
    }

    public String decrypt(byte[] message) throws IllegalBlockSizeException, BadPaddingException {
        var plainMessage = this.decodingCypher.doFinal(message);
        return new String(plainMessage, StandardCharsets.UTF_8);
    }



}
