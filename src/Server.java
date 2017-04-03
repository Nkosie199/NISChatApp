

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

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
                    i=0;
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
        //System.out.println("Please enter your server port number: ");
        //sc = new Scanner(System.in);
        //portNumber = sc.nextInt();
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
            serviceSocket = MyService.accept() ;
            System.out.println("Server-client socket setup complete!");
        }
        catch(IOException e){
            System.out.println("ERROR: Server-client setup method says: "+e) ;
        }
    }
    
    //method to make program run via command line until exit command is supplied...
    public static void run2(){
        DataInputStream serverInputStream = dataInputStream(); //messages sent to the server (from client)
        PrintStream serverOutputStream = dataOutputStream(); //messages sent from the server (to client)
        //
        Scanner serverMsgIn = new Scanner(serverInputStream); //used to store incoming messages from client
        
        log = new ArrayList();
        log.add("Server started"); //add first message to server log
        String nextMsg; //buffer to store incoming messages from client
        //ObjectOutputStream out = new ObjectOutputStream(serviceSocket.getOutputStream()); 
        
        while (!log.isEmpty()){ //while the log is not empty
            //perhaps don't qualify
            //if (threadName.equals("Thread-1")){
                try{  
                    nextMsg = serverMsgIn.nextLine(); //is the next message incoming from client
                    System.out.println(nextMsg); //print to console incoming messages from client
                    log.add(nextMsg); //add the clients message to the log

                    //for (int i=0; i<log.size(); i++){
                        serverOutputStream.println(log.get(log.size()-1)); //sends client last message in the log, ideally the whole log
                    //}         
                    //OR ...
                    //out.writeObject(log); //sent the client the whole log
                }catch(Exception e){
                    
                    System.out.println("Server run method exception says: "+e);
                    break;
                }
            //}

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
        quit = true;
        System.out.println("Server closed.");
    }
    
}
