import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client implements Runnable{
    static Socket MyClient = null; //uses TCP
    static Scanner s;
    private static ObjectOutputStream oos = null;
    private static ObjectInputStream ois = null;
    private static BufferedReader serverInput = null;
    private static BufferedReader clientInput = null;
    private static PrintStream output = null;
    private static boolean socketClosed = false;
    private static final MYRSA myrsa = new MYRSA();
    private static RSA2 rsa2;
    private static Cipher ecipher, dcipher; //Required for DES
    private static final AES aes = new AES("some random stri");
    public static String user;
    private static PublicKey senderpubKey;
    private static PrivateKey senderprivateKey;
    private static PublicKey receiverpubKey;
    private static PrivateKey receiverprivateKey;

    public static void main(String[] args) throws IOException, Exception{
        String machineName = "localhost"; //specifies machine name in IP address form
        int portNumber = 4444;
        String res;
        String port = "";
        String line = "\n----------------------------------------------------------------------------------------------------------------------\n";
        String line2 = "----------------------------------------------------------------------------------------------------------------------";
        s = new Scanner(System.in);
        while(true){
            System.out.println(line+"Host = " +machineName+", port number = " +portNumber +"\nEnter 'yes' to continue or enter 'no' to change the default settings:");
            res = s.nextLine();
            if (res.equalsIgnoreCase("no")){
                System.out.println(line+"Please enter a host to connect to: ");
                machineName = s.next();
                System.out.println(line+"Enter port number to connect to: ");
                try{
                    port = s.next();
                    portNumber = Integer.valueOf(port).intValue();
                    break;
                }
                catch (NumberFormatException e){
                    System.err.println(line+"Please enter a port number with no letters or special characters(digits only). You entered: "+port);
                }
            }    
            else if(res.equalsIgnoreCase("yes")){
                break;
            }
            else {
                System.out.println(line+"Please enter just 'yes' or 'no'. You entered: "+res);
            }
        }      
        
        //setup connection...
        try{
            MyClient = new Socket(machineName, portNumber);
            clientInput = new BufferedReader(new InputStreamReader(System.in));
            output = new PrintStream(MyClient.getOutputStream());
            oos = new ObjectOutputStream(MyClient.getOutputStream());
            serverInput = new BufferedReader(new InputStreamReader(MyClient.getInputStream()));
        } 
        catch (UnknownHostException e){
            System.err.println(line+"This host is unknown: " +machineName);
        }
        catch (IOException e){
            System.err.println(line+"Unable to get Input/output connection of host: "+machineName);
        }
        
        //main run loop contained here...
        if (MyClient != null && output != null && serverInput != null) {
            try{
                //Threads read server input
                new Thread(new Client()).start();
                //the first msg sent to the server is the username...
                user = clientInput.readLine().trim();
                setKeys(user); //generate and send server public and private keys...
                output.println(user); //send msg to server...
                
                while (!socketClosed){
                    //1) Writes to server; PGP hashing and encryption should occur here...
                    String msg2server = clientInput.readLine().trim();
                    //2) Generating SHA-512 hash of original message
                    String hashedMsg2Server = sha512(msg2server); 
                    System.out.println("\nSender Side: Hash of Message = "+hashedMsg2Server);
                    //3) Encrypt the message hash with sender private keys -> Digital Signature
                    String encryptedprivhash = encrypt(senderpubKey, senderprivateKey, hashedMsg2Server, 0);
                    System.out.println("\nSender Side: Hash Encrypted with Sender Private Key (Digital Signature) = "+ encryptedprivhash); 
                    //4) Append original message and encrypted hash
                    String beforezipstring[] = {msg2server, encryptedprivhash};
                    System.out.println("\nSender Side: Message before Compression=\n"+beforezipstring[0]+beforezipstring[1]);
                    //5) Apply zip to beforezipbytes[][]
                    String afterzipstring[] = new String[beforezipstring.length];
                    System.out.println("\nSender Side: Message after Compression=");
                    for (int i=0;i<beforezipstring.length;i++) {
                        afterzipstring[i] = compress(beforezipstring[i]);
                        System.out.println(afterzipstring[i]);
                    }
                    //6) Encrypt zipstring with DES
                    SecretKey key = KeyGenerator.getInstance("DES").generateKey();
                    System.out.println("\nSender Side: SecretKey DES = \n"+key.toString());
                    String afterzipstringDES[] = new String[afterzipstring.length+1];
                    System.out.println("\nSender Side: Compressed Message Encrypted with SecretKey = ");
                    for (int i=0;i<afterzipstring.length;i++) {
                        afterzipstringDES[i] = encryptDES(afterzipstring[i], key);
                        System.out.println(afterzipstringDES[i]);
                    }
                    //7) Encrypt DES key with Receiver Public Key using RSA
                    String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded()); //SecretKey is base64 encoded since direct string encryption gives key in string format during decryption which cant be converted to SecretKey Format
                    String keyencryptedwithreceiverpub = encrypt(receiverpubKey, receiverprivateKey, encodedKey, 1);
                    System.out.println("\nSender Side: DES SecretKey Encrypted with Receiver Public Key = \n"+keyencryptedwithreceiverpub);            
                    afterzipstringDES[2] = keyencryptedwithreceiverpub; //Decrypting DES key with Receiver Private Key using RSA
                    String messagetoreceiver[] = afterzipstringDES;
                    System.out.println("\nFinal Message to receiver = \n"+messagetoreceiver);
                    oos.writeObject(messagetoreceiver);
                    oos.writeObject(senderpubKey);
                    oos.writeObject(senderprivateKey);
                    oos.writeObject(receiverpubKey);
                    oos.writeObject(receiverprivateKey);
                }
                output.close();
                serverInput.close();
                MyClient.close();
            }
            catch (IOException e){
                System.err.println(line+"IOException: " +e);
            }
        }
    }
    
    @Override    
    public void run(){
        String serverMessage;
        int i = 0;
        String line = "\n-----------------------------------------------------------------------------------------------------------------------\n";
        String data ="";
        
        try{
            while((serverMessage = serverInput.readLine()) != null){
                if (!data.contains(serverMessage)){
                    //this is where the decryption of the server message should occur...
                    System.out.println(serverMessage);
                    if (serverMessage.contains(line+"You've logged out"+line)){
                        break;
                    }
                }
            }
            socketClosed = true;
        }
        catch (IOException e){
            System.err.println(line+"IOException: " +e);
        }
    }
    
    public static String getClientIP(){
        String ip = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    //String fullCredentials = iface.getDisplayName() + " " + ip;
                    System.out.println(ip);
                }
            }
        } 
        catch (SocketException e) {
            throw new RuntimeException(e);
        }        
        return ip;
    }
    
    public static void setKeys(String username){
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
    
    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
	final int keySize = 2048;
	KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	keyPairGenerator.initialize(keySize);      
	return keyPairGenerator.genKeyPair();
    }
    
    public static PublicKey getSenderPubKey(){
        return senderpubKey;
    }
    
    public static PrivateKey getSenderPrivateKey(){
        return senderprivateKey;
    }
    
    public static PublicKey getReceiverPubKey(){
        return receiverpubKey;
    }
    
    public static PrivateKey getReceiverPrivateKey(){
        return receiverprivateKey;
    }
    
    public static void receiverside(String messagetoreceiver[], PublicKey senderpubKey, PrivateKey senderprivateKey, PublicKey receiverpubKey, PrivateKey receiverprivateKey) throws Exception {
	//Receiver receives the message messagetoreceiver[] with messagetoreceiver[2] as secret key encrypted with receiver pub key
	//Receiver decrypts the messagetoreceiver[2] with his/her privatekey
	String receiverencodedsecretkey = decrypt(receiverpubKey, receiverprivateKey, messagetoreceiver[2], 1);
	//Key after decryption is in base64 encoded form
	byte[] decodedKey = Base64.getDecoder().decode(receiverencodedsecretkey);
	SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "DES");
	System.out.println("\nReceiver Side: Receiver SecretKey DES after Decryption with his/her Private Key=\n"+originalKey.toString());
	
	//Decrypt the rest of the message in messagetoreceiver with SecretKey originalKey
	String receiverdecryptedmessage[] = new String[messagetoreceiver.length-1];
	System.out.println("\nReceiver Side: Message After Decryption with SecretKey=");
	for (int i=0;i<messagetoreceiver.length-1;i++) {
            messagetoreceiver[i] = decryptDES(messagetoreceiver[i], originalKey);
            System.out.println(messagetoreceiver[i]);
	}
	
	//Unzip this message now i.e. unzip messagetoreceiver
	String unzipstring[] = new String[receiverdecryptedmessage.length];
	System.out.println("\nReceiver Side: UnZipped Message=");
	for (int i=0;i<unzipstring.length;i++) {
            unzipstring[i] = decompress(messagetoreceiver[i]);
            System.out.println(unzipstring[i]);
	}
	
	//Message has been received and is in unzipstring but check the digital signature of the sender i.e. verify the hash with senderpubkey
	//So decrypting the encrypted hash in unzipstring with sender pub key
	String receivedhash = decrypt(senderpubKey, senderprivateKey, unzipstring[1], 0);                                 
	System.out.println("\nReceiver Side: Received Hash=\n"+receivedhash);
	//Calculating SHA512 at receiver side of message
	String calculatedhash = sha512(unzipstring[0]);
	System.out.println("\nReceiver Side: Calculated Hash by Receiver=\n"+calculatedhash);
	if (receivedhash.equalsIgnoreCase(calculatedhash)) {
            System.out.println("\nReceived Hash = Calculated Hash\nThus, Confidentiality and Authentication both are achieved\nSuccessful PGP Simulation\n");
	}
    }

    public static String[] senderside(PublicKey senderpubKey, PrivateKey senderprivateKey, PublicKey receiverpubKey, PrivateKey receiverprivateKey) throws Exception {	
	//Input from user
	System.out.print("\nPGP Simulation:\nSender Side: Input messsage=\n");
	Scanner sc = new Scanner(System.in);
	String rawinput;
	rawinput = sc.nextLine();
	//Generating SHA-512 hash of original message
	String hashout = sha512(rawinput);	
	System.out.println("\nSender Side: Hash of Message=\n"+hashout);
	//Encrypt the message hash with sender private keys -> Digital Signature
	String encryptedprivhash = encrypt(senderpubKey, senderprivateKey, hashout, 0);
	System.out.println("\nSender Side: Hash Encrypted with Sender Private Key (Digital Signature)=\n"+ encryptedprivhash);     
	//Append original message and encrypted hash
	String beforezipstring[] = {rawinput, encryptedprivhash};
	System.out.println("\nSender Side: Message before Compression=\n"+beforezipstring[0]+beforezipstring[1]);
	//Apply zip to beforezipbytes[][]
	String afterzipstring[] = new String[beforezipstring.length];
	System.out.println("\nSender Side: Message after Compression=");
	for (int i=0;i<beforezipstring.length;i++) {
            afterzipstring[i] = compress(beforezipstring[i]);
            System.out.println(afterzipstring[i]);
	}
	//Encrypt zipstring with DES
	SecretKey key = KeyGenerator.getInstance("DES").generateKey();
	System.out.println("\nSender Side: SecretKey DES=\n"+key.toString());
	String afterzipstringDES[] = new String[afterzipstring.length+1];
	System.out.println("\nSender Side: Compressed Message Encrypted with SecretKey=");
	for (int i=0;i<afterzipstring.length;i++) {
            afterzipstringDES[i] = encryptDES(afterzipstring[i], key);
            System.out.println(afterzipstringDES[i]);
	}
	//Encrypt DES key with Receiver Public Key using RSA
	String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
	//SecretKey is base64 encoded since direct string enccryption gives key in string format during decryption which cant be converted to SecretKey Format
	String keyencryptedwithreceiverpub = encrypt(receiverpubKey, receiverprivateKey, encodedKey, 1);
	System.out.println("\nSender Side: DES SecretKey Encrypted with Receiver Public Key=\n"+keyencryptedwithreceiverpub);
	//Decrypting DES key with Receiver Private Key using RSA
	afterzipstringDES[2]=keyencryptedwithreceiverpub;
	String messagetoreceiver[] = afterzipstringDES;
	System.out.println("\nFinal Message to receiver=");
	for (int i=0;i<messagetoreceiver.length;i++) {
            System.out.println(messagetoreceiver[i]);
	}
	return messagetoreceiver;
    }
    
    public static String encryptDES(String str, SecretKey key) throws Exception {
	ecipher = Cipher.getInstance("DES");
	ecipher.init(Cipher.ENCRYPT_MODE, key);
	// Encode the string into bytes using utf-8
	byte[] utf8 = str.getBytes("UTF8");
	// Encrypt
	byte[] enc = ecipher.doFinal(utf8);
	// Encode bytes to base64 to get a string
	return new sun.misc.BASE64Encoder().encode(enc);
    }

    public static String decryptDES(String st, SecretKey key) throws Exception {
	dcipher = Cipher.getInstance("DES");
	dcipher.init(Cipher.DECRYPT_MODE, key);
	// Decode base64 to get bytes
	byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(st);
	byte[] utf8 = dcipher.doFinal(dec);
	// Decode using utf-8
	return new String(utf8, "UTF8");
    }

    public static String decompress(String st) throws IOException {
	byte[] compressed = new sun.misc.BASE64Decoder().decodeBuffer(st);
	ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
	GZIPInputStream gis = new GZIPInputStream(bis);
	BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
	StringBuilder sb = new StringBuilder();
	String line;
	while((line = br.readLine()) != null) {
            sb.append(line);
	}
	br.close();
	gis.close();
	bis.close();
	return sb.toString();
    }

    public static String compress(String data) throws IOException {
	ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
	GZIPOutputStream gzip = new GZIPOutputStream(bos);
	gzip.write(data.getBytes());
	gzip.close();
	byte[] compressed = bos.toByteArray();
	bos.close();
	return new sun.misc.BASE64Encoder().encode(compressed);
    }

    //Takes any string as input and calculates sha 512 bit hash. Output is in 128 bit hex string
    public static String sha512(String rawinput){
	String hashout = "";
	try{
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(rawinput.getBytes("utf8"));
            hashout = String.format("%040x", new BigInteger(1, digest.digest()));
	}
	catch(Exception E){
		System.out.println("Hash Exception");
	}
	return hashout;
    }
    
    //n: 0->encryptwithprivatekey 1->encryptwithpublickey
    public static String encrypt(PublicKey publicKey, PrivateKey privateKey, String message, int ch) throws Exception {
	Cipher cipher = Cipher.getInstance("RSA");
	if (ch == 0) {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);  
            byte[] utf8 = cipher.doFinal(message.getBytes("UTF-8"));
            return new sun.misc.BASE64Encoder().encode(utf8);
	}
	else {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
            byte[] utf8 = cipher.doFinal(message.getBytes("UTF-8"));
            return new sun.misc.BASE64Encoder().encode(utf8);
        }
    }

    //n: 0->decryptwithpublickey 1->decryptwithprivatekey
    public static String decrypt(PublicKey publicKey,PrivateKey privateKey, String st, int ch) throws Exception {
	Cipher cipher = Cipher.getInstance("RSA");
	byte[] encrypted = new sun.misc.BASE64Decoder().decodeBuffer(st);
	if (ch == 0) {
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] utf8 = cipher.doFinal(encrypted);
            return new String(utf8, "UTF8");
	}
	else {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] utf8 = cipher.doFinal(encrypted);
            return new String(utf8, "UTF8");
        }
    }
    
}