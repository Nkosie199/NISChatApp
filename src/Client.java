
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;import java.io.InputStream;
;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * @author gmdnko003
 */
public class Client {
    static Socket MyClient; //stream socket (TCP)
    //String machineName = args[0];
    static String machineName; //specifies machine name in IP address form
    static String userName; //specifies client chosen user-name
    //int portNumber = Integer.parseInt(args[1]);
    static int portNumber; //server open port used to send requests to server < 1023 < 65536
    static DataInputStream input; //stores server responses
    static PrintStream output; //stores message to be sent to server
    static Scanner sc;
    public final static int FILE_SIZE = 6022386; // file size temporary hard coded

    public static void main(String[] args) throws IOException{
        System.out.println("Please enter server domain name/ IP address: ");
        //System.out.println(getClientIP());
        sc = new Scanner(System.in);
        machineName = sc.nextLine();
        //machineName = "localhost";       
        System.out.println("Please enter your user name: ");
        userName = sc.nextLine();      
        portNumber = 4444;        
        //initializing...
        setup();       
        System.out.println("******* Welcome to Chat APP! *******");
        System.out.println("");
        run();
        //upon exiting...
        exit();
        closeSockets();
        //FIN!
        System.out.println("Test Complete!");
    }
    
    //initializing...
    public static void setup(){
        try{
            MyClient = new Socket(machineName, portNumber);
            System.out.println("Client socket setup complete!");
        }
        catch (IOException e){
            System.out.println("ERROR: Client setup method says: "+e);
        }    
    }

    //method to make program run via command line until exit command is supplied...
    public static void run() throws IOException{
        //initializing server and client sockets input and output streams respectively...   
        DataInputStream clientInputStream = dataInputStream(); //messages sent to client (from server)
        PrintStream clientOutputStream = dataOutputStream(); //messages sent from client (to server)
        //
        String fileDir;
        Scanner clientMsgIn = new Scanner(clientInputStream); //used to store incoming messages from server
        //ArrayList<String> log = new ArrayList();
        String command = userName+" has entered the conversation"; //app prompts client to enter a command
        clientOutputStream.println(command); //send entry message to server
        //System.out.println(clientMsgIn.nextLine()); //prints to console message sent from server to client
        formatLogEntry(clientMsgIn.nextLine()); 
        //sc.nextLine(); //to get rid of the blank 1st message
        while (!command.equals(":exit")){
            if (command.equals(":sendfile")){
                command = "";
                System.out.println("Please ensure that the file is in the Client directory and enter the name of the file: ");
                System.out.print(">>>");
                try{                  
                    fileDir = sc.nextLine(); //app prompts user to enter a image name (redundant at this point)
                    clientOutputStream.println(fileDir); //sends server file name/ directory :OUTPUT
                    //System.out.println(clientMsgIn.nextLine()); //prints to console message sent from server to client
                    formatLogEntry(clientMsgIn.nextLine()); //INPUT
                    //
                    System.out.println("Sending file: "+fileDir+"...");
                    clientFileSend(fileDir); //sends file to server
                    System.out.println("SENT!");
                    //
                    clientOutputStream.println("("+System.currentTimeMillis()+") "+userName+": "+"Client has just shared a file: "+ fileDir +". Type :getfile to receive..."); 
                    //System.out.println(clientMsgIn.nextLine()); //prints to console message sent from server to client
                    formatLogEntry(clientMsgIn.nextLine()); //input             
                    //
                }
                catch(Exception e){
                    //System.out.println("ERROR: Failed to read or write file");
                    System.out.println("Client run method said "+e);
                    clientOutputStream.println("Client has failed to send file");
                }  
            }
            else if (command.equals(":getfile")){
                command = "";
                System.out.println("Please enter the name of the file you would like to get: ");
                System.out.print(">>>");
                fileDir = sc.nextLine(); //app prompts user to enter a file name (redundant at this point)
                System.out.println("Receiving file: "+fileDir+"...");  
                //
                clientOutputStream.println(fileDir); //send the server the name of the file you wish to receive
                if (clientMsgIn.hasNextLine()){
                    //System.out.println(clientMsgIn.nextLine()); //prints to console message sent from server to client
                    formatLogEntry(clientMsgIn.nextLine()); //input
                }  
                try{
                    clientFileReceive(fileDir);
                    //
                    System.out.println("Client has recieved file: "+fileDir+"!!! ");
                }
                catch(Exception e){
                    //System.out.println("ERROR: Failed to read or write file");
                    System.out.println("Client run method said "+e);
                    clientOutputStream.println("Client has failed to get file");
                }  
            }
            else{
                // now we will continuously send messages to the server and print out the servers response...
                System.out.print(">>>");
                command = sc.nextLine(); //app prompts user to enter a command
                clientOutputStream.println("("+System.currentTimeMillis()+") "+userName+": "+command); //output
                if (clientMsgIn.hasNextLine()){
                    //System.out.println(clientMsgIn.nextLine()); //prints to console message sent from server to client
                    formatLogEntry(clientMsgIn.nextLine()); //input
                }         
            }
            //ensure input/output streams are kept constant
        }
        command = userName+" has left the conversation"; //app prompts client to enter a command
        clientOutputStream.println(command); //send entry message to server
        if (clientMsgIn.hasNextLine()){
            //System.out.println(clientMsgIn.nextLine()); //prints to console message sent from server to client
            formatLogEntry(clientMsgIn.nextLine()); 
        } 
        System.out.println("");
    }
    
    public static synchronized void clientFileSend(String fileToSend) throws FileNotFoundException, IOException{
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;   
        try {
            // send file
            File myFile = new File (fileToSend); //creates new instance of file
            byte [] mybytearray  = new byte [(int)myFile.length()];
            fis = new FileInputStream(myFile);
            bis = new BufferedInputStream(fis);
            bis.read(mybytearray,0,mybytearray.length);
            os = output;
            System.out.println("Sending " + fileToSend + "(" + mybytearray.length + " bytes)");
            os.write(mybytearray,0,mybytearray.length);
//            os.flush();
            System.out.println("Done."); 
        }
        finally {
//            if (bis != null) bis.close();
//            if (os != null) os.close();
//            if (fis != null) fis.close();            
            if (MyClient!=null){
//                System.out.println("WARNING: Closing client socket!");
//                MyClient.close();
                System.out.println("WARNING: Client socket is not null! Might have to reset connection");
                System.out.println("Socket connected? "+MyClient.isConnected()); //
                //setup(); //reset socket connection
                //MyClient=null;
                    System.out.println(MyClient.getInputStream());
                    System.out.println(MyClient.getOutputStream());
//                    clientOutputStream = dataOutputStream();
//                    clientMsgIn = new Scanner(clientInputStream); 
            }
        }
    }
    
    public static synchronized void clientFileReceive(String fileToRecieve) throws FileNotFoundException, IOException {
        int bytesRead;
        int current = 0;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        //Socket sock = null;
        InputStream is = null;
        try {
            // receive file
            byte[] mybytearray = new byte[FILE_SIZE];
            is = input;
            fos = new FileOutputStream(fileToRecieve);
            bos = new BufferedOutputStream(fos);
            bytesRead = is.read(mybytearray, 0, mybytearray.length);
            current = bytesRead;
            do {
                bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
                if (bytesRead >= 0) {
                    current += bytesRead;
                }
            } while (bytesRead > -1);
            bos.write(mybytearray, 0, current);
            bos.flush();
            fos.flush();
            System.out.println("File " + fileToRecieve + " downloaded (" + current + " bytes read)");
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (bos != null) {
                bos.close();
            }
            if (MyClient != null) {
                MyClient.close();
            }
        }
    }
    
    //adequately prints/ displays to returned server log to console 
    public static void formatLogEntry(String inMsg){
        String out = inMsg.substring(1, inMsg.length()-1); //remove brackets
        //System.out.println("out = "+out);
        String[] out2 = out.split(", ");
        for (String s: out2){
            System.out.println(s);
        }     
    }
    
    //client processing responses from the server...
    public static DataInputStream dataInputStream(){
        try{
            input = new DataInputStream(MyClient.getInputStream());
            //System.out.println("Client dataInputStream method says: input = "+input);
        }
        catch(Exception e){
            System.out.println("ERROR: Client dataInputStream method says: "+e);
        }
        return input;
    }
    
    //sets and gets client output stream to send data to the server
    public static PrintStream dataOutputStream(){
        try{
            output = new PrintStream(MyClient.getOutputStream());
            //System.out.println("Client dataOutputStream method says: output = "+output);
        }
        catch(Exception e){
            System.out.println("ERROR: Client dataOutputStream method says: "+e);
        }
        return output;
    }
    
    //closing server sockets
    public static void closeSockets(){
        try{
            output.close();
            input.close();
            MyClient.close();
            System.out.println("Client closeSockets method says: All sockets closed successfully!");
        }
        catch(IOException e){
            System.out.println("ERROR: Client closeSockets method says: "+e);
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
    
    public static void exit(){
        System.out.println("*** Thank you for using Chat APP. Goodbye! ***");
    }
    
}
