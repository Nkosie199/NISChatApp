package chatapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author gmdnko003
 */
public class Server {

    ServerSocket MyService;
    int portNumber = 9999;
    
    public void setup(){
        try{
            MyService = new ServerSocket(portNumber);
        }
        catch(IOException e){
                System.out.println(e);
        }
    }
    
    public void setupClientSocket(){
        Socket clientSocket = null;
        
        try{
            clientSocket = MyService.accept() ;
        }
        catch(IOException e){
            System.out.println (e) ;
        }
    }
    
    
}
