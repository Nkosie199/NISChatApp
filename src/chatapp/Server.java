package chatapp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author gmdnko003
 */
public class Server {
    ServerSocket MyService; //stream socket to listen in for clients requests (TCP)
    Socket serviceSocket = null; //socket sent from client to server
    int portNumber; // server will use this port number for listening
    DataInputStream input; //used to store client messages for prosessing
    PrintStream output; //used to send messages back to client
    
    //initialization...
    public void setup(int portNumber){
        this.portNumber = portNumber;
        try{
            MyService = new ServerSocket(portNumber);
            System.out.println("Server socket setup complete!");
        }
        catch(IOException e){
                System.out.println("ERROR: Server setup method says: "+e);
        }
        
    }
    
    //capturing client socket for processing...
    public void setupClientSocket(){
        try{
            serviceSocket = MyService.accept() ;
            System.out.println("Server-client socket setup complete!");
        }
        catch(IOException e){
            System.out.println("ERROR: Server setup method says: "+e) ;
        }
    }
    
    //server processing requests from the client...
    public void dataInputStream(){
        try{
            input = new DataInputStream(serviceSocket.getInputStream());
            System.out.println("Server dataInputStream method says: input = "+input);
        }
        catch(IOException e){
            System.out.println("ERROR: Server dataInputStream method says: "+e);
        }
    }
    
    //client output stream to send data to the server
    public void dataOutputStream(){
        try{
            output = new PrintStream(serviceSocket.getOutputStream());
            System.out.println("Server dataOutputStream method says: output = "+output);
        }
        catch(IOException e){
            System.out.println("ERROR: Server dataOutputStream method says: "+e);
        }
    }
    
    //closing server sockets
    public void closeSockets(){
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
    
}
