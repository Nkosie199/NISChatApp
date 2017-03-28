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
    int portNumber = 9999; // server will use this port number for listening
    DataInputStream input; //used to store client messages for prosessing
    PrintStream output; //used to send messages back to client
    
    //initialization...
    public void setup(){
        try{
            MyService = new ServerSocket(portNumber);
        }
        catch(IOException e){
                System.out.println(e);
        }
    }
    
    //capturing client socket for processing...
    public void setupClientSocket(){
        try{
            serviceSocket = MyService.accept() ;
        }
        catch(IOException e){
            System.out.println("Server setup method says: "+e) ;
        }
    }
    
    //server processing requests from the client...
    public void dataInputStream(){
        try{
            input = new DataInputStream(serviceSocket.getInputStream());
        }
        catch(IOException e){
            System.out.println("Server dataInputStream method says: "+e);
        }
    }
    
    //client output stream to send data to the server
    public void dataOutputStream(){
        try{
            output = new PrintStream(serviceSocket.getOutputStream());
        }
        catch(IOException e){
            System.out.println("Server dataOutputStream method says: "+e);
        }
    }
    
    //closing server sockets
    public void closeSockets(){
        try{
            output.close();
            input.close();
            serviceSocket.close();
            MyService.close();
        }
        catch(IOException e){
            System.out.println("Server closeSockets method says: "+e);
        }
    }
    
}
