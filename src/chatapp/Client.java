package chatapp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 * @author gmdnko003
 */
public class Client {
    Socket MyClient; //stream socket (TCP)
    //String machineName = args[0];
    public String machineName; //specifies machine name in IP address form
    //int portNumber = Integer.parseInt(args[1]);
    int portNumber; //server open port used to send requests to server < 1023 < 65536
    DataInputStream input; //stores server responses
    PrintStream output; //stores message to be sent to server
    
    //initializing...
    public void setup(String machineName, int portNumber){
        this.machineName = machineName;
        this.portNumber = portNumber;
        
        try{
            MyClient = new Socket(machineName, portNumber);
            System.out.println("Client socket setup complete!");
        }
        catch (IOException e){
            System.out.println("ERROR: Client setup method says: "+e);
        }
        
    }
    
    //client processing responses from the server...
    public void dataInputStream(){
        try{
            input = new DataInputStream(MyClient.getInputStream());
            System.out.println("Client dataInputStream method says: input = "+input);
        }
        catch(Exception e){
            System.out.println("ERROR: Client dataInputStream method says: "+e);
        }
    }
    
    //client output stream to send data to the server
    public void dataOutputStream(){
        try{
            output = new PrintStream(MyClient.getOutputStream());
            System.out.println("Client dataOutputStream method says: output = "+output);
        }
        catch(Exception e){
            System.out.println("ERROR: Client dataOutputStream method says: "+e);
        }
    }
    
    //closing server sockets
    public void closeSockets(){
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
    
}
