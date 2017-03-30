

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author gmdnko003
 */
public class Server {
    static ServerSocket MyService; //stream socket to listen in for clients requests (TCP)
    static Socket serviceSocket = null; //socket sent from client to server
    static int portNumber; // server will use this port number for listening
    static DataInputStream input; //used to store client messages for prosessing
    static PrintStream output; //used to send messages back to client
    static Scanner sc;
    static ArrayList<String> log;
    
    public static void main(String[] args){
        System.out.println("Please enter your server port number: ");
        sc = new Scanner(System.in);
        portNumber = sc.nextInt();
        //
        setup();
        setupClientSocket();
        //
        run();
        //
        closeSockets();
        exit();
    }
    
    //initialization...
    public static void setup(){
        
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
            System.out.println("ERROR: Server setup method says: "+e) ;
        }
    }
    
    //method to make program run via command line until exit command is supplied...
    public static void run(){
        DataInputStream serverInputStream = dataInputStream(); //messages sent to the server (from client)
        PrintStream serverOutputStream = dataOutputStream(); //messages sent from the server (to client)
        //
        Scanner serverMsgIn = new Scanner(serverInputStream); //used to store incoming messages from client
        
        log = new ArrayList();
        log.add("Server started"); //add first message to server log
        String nextMsg; //buffer to store incoming messages from client
        //ObjectOutputStream out = new ObjectOutputStream(serviceSocket.getOutputStream()); 
        
        while (!log.isEmpty()){ //while the log is not empty
            nextMsg = serverMsgIn.nextLine(); //is the next message incoming from client
            System.out.println(nextMsg); //print to console incoming messages from client
            log.add(nextMsg); //add the clients message to the log
                         
            serverOutputStream.println(log); //send the client the whole log
            //OR ...
            //out.writeObject(log); //sent the client the whole log
            
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
    
    public static void exit(){
        System.out.println("Server closed...");
    }
    
}
