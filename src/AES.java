/**
 * AES
 */
import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {
  private static String ALGO = "AES";
  private byte[] keyValue;

  public AES(String key) {
    keyValue = key.getBytes();
  }

  public String encrypt(String msg) throws Exception {
    Key key = new SecretKeySpec(keyValue, ALGO);
    Cipher cipher = Cipher.getInstance(ALGO);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] enc_msg = cipher.doFinal(msg.getBytes());

    Base64.Encoder encoder = Base64.getEncoder();
    String encoded_enc_msg = encoder.encodeToString(enc_msg);
    return encoded_enc_msg;
  }

  public String decrypt(String encoded_enc_msg) throws Exception {
    Key key = new SecretKeySpec(keyValue, ALGO);
    Cipher cipher = Cipher.getInstance(ALGO);
    cipher.init(Cipher.DECRYPT_MODE, key);
    byte[] enc_msg = Base64.getDecoder().decode(encoded_enc_msg);
    byte[] msg = cipher.doFinal(enc_msg);
    return new String(msg);
  }

  /**
   * @return the keyValue
   */
  public byte[] getKeyValue() {
    return keyValue;
  }

  public static void main(String[] args) throws Exception{
    AES aes = new AES("some random stri");
    String msg_to_enc = "This message is top secret"; 
    System.out.println("Message to enc: " + msg_to_enc);
    String enc_msg = aes.encrypt(msg_to_enc);
    System.out.println("Encrypted message: " + enc_msg);
    String dec_msg = aes.decrypt(enc_msg);
    System.out.println("Decrypted message: " + dec_msg);
  }
}