import java.security.PublicKey;

public class PubKey {
    public String user;
    private PublicKey pubKey;
    
    public PubKey(String u, PublicKey p){
        user = u;
        pubKey = p;
    }
    
    public PublicKey getPubKey(){
        return pubKey;
    }
    
}
