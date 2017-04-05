

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 * @author gmdnko003
 */
public class Server implements Runnable {
    private Thread t;
    private static String threadName;
    
    static ServerSocket MyService; //stream socket to listen in for clients requests (TCP)
    static Socket serviceSocket = null; //socket sent from client to server
    static int portNumber; // server will use this port number for listening
    static DataInputStream input; //used to store client messages for prosessing
    static PrintStream output; //used to send messages back to client
    static Scanner sc;
    static ArrayList<String> log;
    static boolean quit = false;
    static boolean threadSwitch = true;
    public final static int FILE_SIZE = 6022386; // file size temporary hard coded
    
   Server( String name) {
      threadName = name;
      System.out.println("Creating " +  threadName );
   }
   
   public void run() {
        System.out.println("Running " +  threadName );
        System.out.println(threadName);   
        int i=0;
        while (quit==false){           
            if (i == 0){
                i++;
                portNumber = 4444;
                System.out.println(getServerIP());        
                setup();
                setupClientSocket(); 
            } 
            else if (i == 1){            
                //running...
                run2();  
            }
        }
        //exiting...
        closeSockets();
        exit();      
   }
   
   public void start () {
      System.out.println("Starting " +  threadName );
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
    
    //initialization...
    public static void setup(){
        portNumber = 4444;       
        try{
            MyService = new ServerSocket(portNumber);
            System.out.println("Server socket setup complete!");
        }
        catch(IOException e){
                System.out.println("ERROR: Server setup method says: "+e);           
        }   
    }
    
    //capturing client socket for processing...
    public static void setupClientSocket(){
        try{
            serviceSocket = MyService.accept();
            System.out.println("Server-client socket setup complete!");
        }
        catch(IOException e){
            System.out.println("ERROR: Server-client setup method says: "+e) ;
        }
    }
    
    public static void reset(){
        try{
            serviceSocket = MyService.accept();
            System.out.println("Server-client socket RESET complete!");
            run2();
        }
        catch(IOException e){
            System.out.println("ERROR: Reset setup method says: "+e) ;
        }
    }
    
    //method to make program run via command line until exit command is supplied...
    public static void run2(){
        DataInputStream serverInputStream = dataInputStream(); //messages sent to the server (from client)
        PrintStream serverOutputStream = dataOutputStream(); //messages sent from the server (to client)
        //
        Scanner serverMsgIn = new Scanner(serverInputStream, "UTF-8"); //used to store incoming messages from client       
        log = new ArrayList();
        log.add("Chat started"); //add first message to server log
        String nextMsg; //buffer to store incoming messages from client
        //ObjectOutputStream out = new ObjectOutputStream(serviceSocket.getOutputStream()); 
        int fileSendSwitch = 0;
        int extension = 1;
        
        while (!log.isEmpty()){ //while the log is not empty
            //perhaps don't qualify threads    
                try{  
                    //System.out.println("Waiting for client command...");
                    if (!serverMsgIn.hasNextLine()){ //if scanner does not have next line
                        setupClientSocket();                         
                    }
                    else{ //Scanner has next line                 
                        nextMsg = serverMsgIn.nextLine(); //is the next message incoming from client
                        if (nextMsg.contains(":sendfile") && fileSendSwitch==0){ //in the case that a client sends an image                     
                            System.out.println("Client is attempting to send image...");
                            log.add("Client is attempting to send image...");
                            //serverOutputStream.println(log.get(log.size()-1)); //sends client last message in the log, ideally the whole log
                            serverOutputStream.println(log); //sends client last message in the log, ideally the whole log
                            //
                            System.out.println("Waiting for client to send image name...");
                            //
                            String fileIncoming = "File"+extension;
                            extension++;
                            serverFileRecieve(fileIncoming);
                            System.out.println("File has been recieved!!!");
                            //serverOutputStream.println("File has been recieved by server!!!");
                            serverOutputStream.println(log); //sends client last message in the log, ideally the whole log
                            //progression...
                            fileSendSwitch = 1;
                        }
                        else{ //in the case that a simple text message is sent
                            System.out.println(nextMsg); //print to console incoming messages from client
                            log.add(nextMsg); //add the clients message to the log
                            //serverOutputStream.println(log.get(log.size()-1)); //sends client last message in the log, ideally the whole log
                            serverOutputStream.println(log); //sends client last message in the log, ideally the whole log
                            fileSendSwitch = 0;
                        }
                    }
                }catch(Exception e){   
                    System.out.println("Server run method exception says: "+e);
                    break;
                }
        }   
    }
    
    public static void serverFileRecieve(String fileToRecieve) throws FileNotFoundException, IOException{
        int bytesRead;
        int current = 0;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        //Socket sock = null;
        InputStream is = null;
        try {
            // receive file
            byte [] mybytearray  = new byte [FILE_SIZE];
            is = input;
            fos = new FileOutputStream(fileToRecieve);
            bos = new BufferedOutputStream(fos);
            bytesRead = is.read(mybytearray,0,mybytearray.length);
            current = bytesRead;
        do {
            bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
            if(bytesRead >= 0) current += bytesRead;
        } while(bytesRead > -1);
            bos.write(mybytearray, 0 , current);
            bos.flush();
            fos.flush();
            System.out.println("File " + fileToRecieve + " downloaded (" + current + " bytes read)");
        }
        finally {
            if (fos != null) fos.close();
            if (bos != null) bos.close();
            if (serviceSocket != null) serviceSocket.close();
        }
    }
    
    //server processing requests from the client...
    public static DataInputStream dataInputStream(){
        try{
            input = new DataInputStream(serviceSocket.getInputStream());
            //System.out.println("Server dataInputStream method says: input = "+input);
        }
        catch(IOException e){
            System.out.println("ERROR: Server dataInputStream method says: "+e);
        }
        return input;
    }
    
    //client output stream to send data to the server
    public static PrintStream dataOutputStream(){
        try{
            output = new PrintStream(serviceSocket.getOutputStream());
            //System.out.println("Server dataOutputStream method says: output = "+output);
        }
        catch(IOException e){
            System.out.println("ERROR: Server dataOutputStream method says: "+e);
        }
        return output;
    }
    
    //closing server sockets
    public static void closeSockets(){
        try{
            output.close();
            input.close();
            serviceSocket.close();
            MyService.close();
            System.out.println("Server closeSockets method says: All sockets closed successfully!");
        }
        catch(IOException e){
            System.out.println("ERROR: Server closeSockets method says: "+e);
        }
    }

    public static String getServerIP(){
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
    
    public boolean isDead(){
        if (t.isAlive()){
            return false;
        }
        else{
            return true;
        }
    }
    
    public static void exit(){
        System.out.println("Server closed.");
    }
    
}