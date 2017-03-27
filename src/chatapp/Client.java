package chatapp;

import java.io.IOException;
import java.net.Socket;

/**
 * @author gmdnko003
 */
public class Client {
    
    Socket MyClient;
    //String machineName = args[0];
    String machineName = "my machine";
    //int portNumber = Integer.parseInt(args[1]);
    int portNumber = 1234;
    
    public void setup(){
        try{
            MyClient = new Socket(machineName, portNumber);
        }
        catch (IOException e){
            System.out.println("Client setup methods says: "+e);
        }
    }
    
}
