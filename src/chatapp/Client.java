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
    public String machineName = "localhost"; //specifies machine name in IP address form
    //int portNumber = Integer.parseInt(args[1]);
    int portNumber = 8005; //port used to send requests to server < 1023 < 65536
    DataInputStream input; //stores server responses
    PrintStream output; //stores message to be sent to server
    
    //initializing...
    public void setup(String machineName, int portNumber){
        this.machineName = machineName;
        this.portNumber = portNumber;
        
        try{
            MyClient = new Socket(machineName, portNumber);
        }
        catch (IOException e){
            System.out.println("Client setup method says: "+e);
        }
    }
    
    //client processing responses from the server...
    public void dataInputStream(){
        try{
            input = new DataInputStream(MyClient.getInputStream());
        }
        catch(Exception e){
            System.out.println("Client dataInputStream method says: "+e);
        }
    }
    
    //client output stream to send data to the server
    public void dataOutputStream(){
        try{
            output = new PrintStream(MyClient.getOutputStream());
        }
        catch(Exception e){
            System.out.println("Client dataOutputStream method says: "+e);
        }
    }
    
    //closing server sockets
    public void closeSockets(){
        try{
            output.close();
            input.close();
            MyClient.close();
        }
        catch(IOException e){
            System.out.println("Client closeSockets method says: "+e);
        }
    }    
    
}
