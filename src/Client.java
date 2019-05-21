import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client implements Runnable{
    static Socket clientSocket = null; //uses TCP
    static Scanner s;
    private static ObjectOutputStream oos = null;
    private static ObjectInputStream ois = null;
    private static BufferedReader serverInput = null;
    private static BufferedReader clientInput = null;
    private static PrintStream output = null;
    private static boolean socketClosed = false;
    private static Cipher ecipher, dcipher; //Required for DES
    private static String user;
    private static String currentMsg;
    private static PublicKey senderpubKey;
    private static PrivateKey senderprivateKey;
    private static PublicKey receiverpubKey;

    public static void main(String[] args) throws IOException, Exception{
        String machineName = "localhost"; //specifies machine name in IP address form
        int portNumber = 4444;
        String res;
        String port = "";
        String line = "\n----------------------------------------------------------------------------------------------------------------------\n";
        s = new Scanner(System.in);
        while(true){
            System.out.println(line+"Host = " +machineName+", port number = " +portNumber +"\nEnter 'yes' to continue or enter 'no' to change the default settings:");
            res = s.nextLine();
            if (res.equalsIgnoreCase("no")){
                System.out.println(line+"Please enter a host to connect to: ");
                machineName = s.nextLine();
                System.out.println(line+"Enter port number to connect to: ");
                try{
                    port = s.nextLine();
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
            clientSocket = new Socket(machineName, portNumber);
            clientInput = new BufferedReader(new InputStreamReader(System.in));
            output = new PrintStream(clientSocket.getOutputStream());
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());
            serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } 
        catch (UnknownHostException e){
            System.err.println(line+"This host is unknown: "+machineName);
        }
        catch (IOException e){
            System.err.println(e);
        }
        
        //main run loop contained here...
        if (clientSocket != null && output != null && oos != null) {
            try{
                String serverMessage = serverInput.readLine(); //server will ask for username
                System.out.println(serverMessage);
                //the first msg sent to the server is the username...
                user = clientInput.readLine().trim();
                setKeys(user); //generate and send server public and private keys...
                output.println(user); //send username to server...
                
                //Threads read server input
                new Thread(new Client()).start(); 
                
                while (!socketClosed){
                    //1) Writes to server; PGP hashing and encryption should occur here...
                    System.out.print(">>>");
                    String msg2server = clientInput.readLine().trim();
                    if (msg2server.startsWith("@")){ //if client sends DM, keep the message until its encrypted
                        String[] msg = msg2server.split("\\s", 2);
                        String name = msg[0].trim(); //username
                        currentMsg = msg[1].trim(); //message
                        output.println(name); //send just the name
                    }else{
                        output.println(msg2server); //send the whole msg
                        //previous implementation used senderside method with pgp implementation...
                        //senderside(msg2server, senderpubKey, senderprivateKey, receiverpubKey, receiverprivateKey);
                    }          
                }
                
                oos.close();
                ois.close();
                output.close();
                serverInput.close();
                clientSocket.close();
            }
            catch (IOException e){
                System.err.println(line+"IOException: " +e);
            }
        }
    }
    
    @Override    
    public void run(){  
        String serverMessage;
        String line = "\n-----------------------------------------------------------------------------------------------------------------------\n";
        try{
            while(true){
                //first check, for input lines
                if ((serverMessage = serverInput.readLine()) != null ){
                    System.out.println(serverMessage);
                    //expectations...
                    if (serverMessage.contains("******* Welcome to ChatAPP, ")){
                        sendPubKey();
                    }
                    if (serverMessage.equals("Server is sending you their public key...")){
                        receivePubKey();
                    }
                    if (serverMessage.contains("Secret message you would like to send to ")){
                        sendEncryptedMsg();
                    }
                    if (serverMessage.contains("*** Encrypted message from ")){
                        readDecryptMsg();
                    }
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
        catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //expected responses... 
    public static void sendPubKey() throws IOException{     
        oos.writeObject(senderpubKey); //send public key
        //System.out.println("sent senderpubKey: "+senderpubKey);
    }
    
    public static void receivePubKey() throws IOException, ClassNotFoundException{
        receiverpubKey = (PublicKey) ois.readObject(); //receive public key
        System.out.println("receiverpubKey: "+receiverpubKey);
    }
    
    public static void sendEncryptedMsg() throws Exception{
        String enc = encrypt2(currentMsg); //we must encrypt message using B's public key...
        oos.writeObject(enc); //we must send the encrypted message to B...
        System.out.println("message: "+currentMsg+", encrypted: "+enc);
    }
    
    public static void readDecryptMsg() throws IOException, ClassNotFoundException{
        ///This part!!!
        serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ois = new ObjectInputStream(clientSocket.getInputStream());
        System.out.println("awaiting object...");
        //maybe we should send something...
        output.println("client is awating object..."); //send status to server...
        String msg = (String) ois.readObject(); //encryted msg
        System.out.println("this is the encrypted message: "+msg);
        String decryptedmsg = decrypt3(msg); //decrypt msg with sender's private key
        System.out.println("decrypted msg: "+decryptedmsg+" ***");
        
        oos.writeObject("thanks"); //confirmation to server allows loop to continue
    }
    
    //extras...
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
    
    public String receiverside(String messagetoreceiver[], PublicKey senderpubKey, PrivateKey senderprivateKey, PublicKey receiverpubKey, PrivateKey receiverprivateKey) throws Exception {
        System.out.println("INCOMING...");
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
            System.out.println("\nReceived Hash == Calculated Hash\nThus, Confidentiality and Authentication both are achieved\nSuccessful PGP Simulation\n");
	}
        return unzipstring[0];
    }

    public static void senderside(String msg, PublicKey senderpubKey, PrivateKey senderprivateKey, PublicKey receiverpubKey, PrivateKey receiverprivateKey) throws Exception {		
	System.out.println("\nSender Side: Msg being sent = "+msg);
        //2) Generating SHA-512 hash of original message
        String hashedMsg2Server = sha512(msg); 
        System.out.println("\nSender Side: Hash of Message = "+hashedMsg2Server);
        //3) Encrypt the message hash with sender private keys -> Digital Signature
        String encryptedprivhash = encrypt(senderpubKey, senderprivateKey, hashedMsg2Server, 0);
        System.out.println("\nSender Side: Hash Encrypted with Sender Private Key (Digital Signature) = "+ encryptedprivhash); 
        //4) Append original message and encrypted hash
        String beforezipstring[] = {msg, encryptedprivhash};
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
        System.out.println("\nSender Side: to receiver = \n"+messagetoreceiver+"\n\n\n");
        oos.writeObject(messagetoreceiver);
        oos.writeObject(senderpubKey);
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
    
    //n: 0->encryptwithprivatekey 1->encryptwithpublickey
    public static String encrypt2(String st) throws Exception {
	Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, receiverpubKey);  
        byte[] utf8 = cipher.doFinal(st.getBytes("UTF-8"));
        return new sun.misc.BASE64Encoder().encode(utf8);
        
    }

    //n: 0->decryptwithpublickey 1->decryptwithprivatekey
    public static String decrypt2(String st, PrivateKey privateKey) throws Exception {
	Cipher cipher = Cipher.getInstance("RSA");
	byte[] encrypted = new sun.misc.BASE64Decoder().decodeBuffer(st);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] utf8 = cipher.doFinal(encrypted);
        return new String(utf8, "UTF8");
    }
    
    public static String decrypt3(String st) throws UnsupportedEncodingException {
    //DECRYPTING msg with private key
    byte[] msgBytes = null;
    try {
      Cipher cipher = Cipher.getInstance("RSA");
      byte[] encMsgBytes = new sun.misc.BASE64Decoder().decodeBuffer(st);
      cipher.init(Cipher.DECRYPT_MODE, senderprivateKey);
      msgBytes = cipher.doFinal(encMsgBytes);
      //DATA ENCRYPTED
    } catch (Exception e) {
      e.printStackTrace();
    }
    String s = new String(msgBytes, "UTF8");
    return s;
  }
    
    
}