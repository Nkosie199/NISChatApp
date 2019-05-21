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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Server{
    static ServerSocket MyService = null; //stream socket to listen in for clients requests (TCP)
    static Socket clientSocket = null; //socket sent from client to server
    static int users = 20; // The server can accept up to maxUser connections at a time.
    static clientThreads[] threads = new clientThreads[users];
    public static ArrayList<PubKey> pubkeys = new ArrayList<>();
    
    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);
        String response;
        int portNumber = 4444; //server will use this port number for listening for new client connections
        String port = "";
        String line = "\n-------------------------------------------------------------------------------------------------------------------------\n";
        while(true){
            System.out.println("Port number = " +portNumber +"\nEnter 'yes' to continue or 'no' to  change the default port number:");
            response = sc.nextLine();
            if (response.equalsIgnoreCase("no")){
                System.out.println("Enter the new port number:");
                try{
                    port = sc.nextLine();
                    portNumber = Integer.parseInt(port);
                    break;
                }
                catch (NumberFormatException e){
                    System.err.println("Please enter a port number with digits only. You entered: "+port);
                }
            }    
            else if(response.equalsIgnoreCase("yes")){
                break;
            }
            else {
                System.out.println("Please enter 'yes' or 'no'. You entered: "+response);
            }
        }
        getServerIP(); //prints out full details of inet ip address
        
        //create socket called MyService on given port number
        try {
            MyService = new ServerSocket(portNumber);
            System.out.println("Server is now listening for clients...");
        } catch (IOException e) {
            System.out.println(e);
        }
        
        //new client thread created for each client connected to server.
        while (true){
            try{
                clientSocket = MyService.accept();
                int i;
                for (i = 0; i< threads.length; i++){
                    if(threads.length == 0){
                      (threads[i] =  new clientThreads(clientSocket, threads)).start();
                      break;  
                    }
                    else if(threads[i] == null){
                        (threads[i] =  new clientThreads(clientSocket, threads)).start();
                        break;
                    }
                }                
                // Once max number of clients are connected to server, server prevents other potential clients until a connected client disconnects.
                if (i == users){
                    try (PrintStream output = new PrintStream(clientSocket.getOutputStream())) {
                        output.println(line+"Please try to connect again later. Server has reached its maximum number of clients.");
                    }
                    clientSocket.close(); 
                }
            }
            catch (IOException e){
                System.out.println(e);
            }
        }
    }
    
    public static void getServerIP() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    //String fullCredentials = iface.getDisplayName() + " " + ip;
                    System.out.println(ip);
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    } 
}


//this class is handles the threads responsible for communicating with clientts
class clientThreads extends Thread{
    private String client = null;
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;
    private BufferedReader input = null;
    private PrintStream output = null;
    private Socket clientSocket = null;
    private clientThreads[] threads = null;
    private final int users;
    String line = "\n----------------------------------------------------------------------------------------------------------------------\n";
    String help = line+"To leave enter '/exit' in a new line.\nTo send private messages enter user name with '@' sign in front of name, a space and the message e.g. @Bob Hey Bob\nTo display these intructions again enter '/help'"+line;
    private static final AES aes = new AES("some random stri");
    private static Cipher ecipher, dcipher; //Required for DES
    
    public clientThreads(Socket clientSocket, clientThreads[] threads){
        this.clientSocket = clientSocket;
        this.threads = threads;
        this.users = threads.length;
    }
    
    @Override
    public void run(){
        try{
            // input and output streams created for each client thread
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            
            String user;
            while (true){
                synchronized(this){
                    output.println("Enter your name to display in chat: ");
                    //the first msg sent to the server is the username...
                    user = input.readLine().trim();
                    if (!user.contains("@")){
                        break;
                    }
                    else{
                        System.out.println("The name should not contain '@' character.");
                    }
                }                
            }
            
            //Opening messages for clients.
            output.println(line+"******* Welcome to ChatAPP, " +user+ "! *******");
            output.println(help);
            PublicKey pubKey;
            pubKey = (PublicKey) ois.readObject(); //new user's public key
            Server.pubkeys.add(new PubKey ("@"+user, pubKey));
            System.out.println("added "+user+"'s public key: "+pubKey+" to the keyring!");
            
            synchronized(this){
                for (clientThreads thread : threads) {
                    if (thread != null && thread == this) {
                        this.client = "@"+user;
                        break;
                    }
                }      
                for (clientThreads thread : threads) {
                    if (thread != null && thread != this) {
                        thread.output.println("***New user: " +user+" has entered the chat room***");
                    }
                }                       
            }
            //Now handling communication between clients.
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            
            while(true){
                System.out.print(">>>");
                String nxtMsg = input.readLine();
                System.out.println(client+": "+nxtMsg);
                Date date = new Date();
                // Privated messages directed to intended user
                if (nxtMsg.startsWith("@")){
                    synchronized(this){
                        String name = nxtMsg;
                        //1) Writes to clients; PGP hashing and encryption should occur here...
                        //Server must send sender recipient's public key
                        boolean userPresent = false;
                        for (PubKey p: Server.pubkeys){
                            if (p.user.equals(name)){
                                userPresent = true;
                                this.output.println("Server is sending you their public key...");
                                this.oos.writeObject(p.getPubKey()); //this = output to self
                                break;
                            }
                        }
                        if (userPresent){ //continue
                            this.output.println("Secret message you would like to send to "+name+":");
                            String res = (String) this.ois.readObject(); //await sender response - message encryted with recipent public key...
                            System.out.println("server recieved this message: "+res);
                            for (clientThreads thread : threads) {
                                if (thread != null && thread != this && thread.client != null && thread.client.equals(name)) {
                                    String msg2clients = "<"+dateFormat.format(date)+"> *** Encrypted message from "+this.client+" to "+thread.client+" ***: ";                  
                                    thread.output.println(msg2clients); //output to recipient, notifying them to expect encrypted message
                                    ///this part!!!
                                    thread.oos.writeObject(res); //send sender response (message) to recipient...
                                    System.out.println("sent, awaiting client "+thread.client+"'s confirmation...");
                                    String confirm = (String) thread.ois.readObject(); //we must be notified that client got something
                                    System.out.println("client has confirmed by saying: "+confirm);
                                    break;
                                }
                            } 
                            System.out.println("Done sending encrypted msg!");
                        }
                        else{
                            this.output.println("Sorry, that user is not here!");
                        }     
                    }
                }
                else if (nxtMsg.equals("/exit")){
                    break;
                }
                else if (nxtMsg.equals("/help")){
                    this.output.println(help);
                }
                else {
                    // Public messages intended to all users, unecrypted
                    synchronized(this){
                        for (clientThreads thread : threads) {
                            if (thread != null && thread.client != null) {
                                String msg2clients = "<"+dateFormat.format(date)+"> "+user+": "+nxtMsg;
                                thread.output.println(msg2clients);
                            }
                        }
                    }
                }
            }           
            //when user exits, breaks out of loop and displays following message to all users
            synchronized(this){
                for (clientThreads thread : threads) {
                    if (thread != null && thread != this && thread.client != null) {
                        thread.output.println("***User: "+user+" has left the chat room***");
                    }
                }
            }
            output.println("***"+user+" You've logged out***");
        
            //Create space in threads list for another client
            synchronized(this){
                for (int i =0; i<threads.length;i++){
                    if (threads[i] == this){
                        threads[i] = null;
                    }
                }
            }
            //Close socket, input and output streams.
            input.close();
            output.close();
            clientSocket.close();
            ois.close();
            oos.close();
        }
        catch (IOException e){
            System.out.println(e);
        } catch (Exception ex) {
            Logger.getLogger(clientThreads.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String receiverside(String messagetoreceiver[], PublicKey senderpubKey, PrivateKey senderprivateKey, PublicKey receiverpubKey, PrivateKey receiverprivateKey) throws Exception {
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
    
    public String[] senderside(String msg, PublicKey senderpubKey, PrivateKey senderprivateKey, PublicKey receiverpubKey, PrivateKey receiverprivateKey) throws Exception {	
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