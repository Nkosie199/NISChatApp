import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class PubKeys {
    public String user;
    private static PublicKey senderpubKey;
    private static PrivateKey senderprivateKey;
    private static PublicKey receiverpubKey;
    private static PrivateKey receiverprivateKey;
    
    public PubKeys(String username){
        user = username;
        try {
            //Generating sender keys
            KeyPair senderkeyPair = buildKeyPair();
            senderpubKey = senderkeyPair.getPublic();
            senderprivateKey = senderkeyPair.getPrivate();
            //Generating receiver keys
            KeyPair receiverkeyPair = buildKeyPair();
            receiverpubKey = receiverkeyPair.getPublic();
            receiverprivateKey = receiverkeyPair.getPrivate();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
    
    public PublicKey getSenderPubKey(){
        return senderpubKey;
    }
    
    public PrivateKey getSenderPrivateKey(){
        return senderprivateKey;
    }
    
    public PublicKey getReceiverPubKey(){
        return receiverpubKey;
    }
    
    public PrivateKey getReceiverPrivateKey(){
        return receiverprivateKey;
    }
    
    public KeyPair buildKeyPair() throws NoSuchAlgorithmException {
	final int keySize = 2048;
	KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	keyPairGenerator.initialize(keySize);      
	return keyPairGenerator.genKeyPair();
    }
}
