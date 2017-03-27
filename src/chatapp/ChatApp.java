
package chatapp;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author gmdnko003
 */
public class ChatApp {

    public static void main(String[] args) {
        Server chatServer = new Server();
        Client chatClient = new Client();
        
        chatServer.setup();
        chatClient.setup();
        
        System.out.println("Test Complete!");
    }
    
}
