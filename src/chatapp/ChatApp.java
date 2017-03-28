
package chatapp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

/**
 *
 * @author gmdnko003
 */
public class ChatApp {
    static Server chatServer;
    static Client chatClient;
    static Scanner sc;
    static String machineName;
    static int clientPortNumber;
    static int serverPortNumber = 8006; //assuming server is localhost:8080
    
    //app is run from command line with arguments: <machineName> and <portNumber> Eg. java ChatApp client 1234
    public static void main(String[] args) throws IOException {
        sc = new Scanner(System.in);
        chatServer = new Server();
        chatClient = new Client();
        //machineName = args[0];
        System.out.println("Please enter your machine name: ");
        System.out.println(getClientIP());
        machineName = sc.nextLine();
        //portNumber = Integer.parseInt(args[1]);
        System.out.println("Please specify the client port number you wish to use: ");
        clientPortNumber = sc.nextInt();
        //initializing...
        setup();  
        
        System.out.println("******* Welcome to Chat APP! *******");
        run();
        
        //upon exiting...        
        chatClient.closeSockets();
        chatServer.closeSockets();
        exit();
        //FIN!
        System.out.println("Test Complete!");
    
    }
  
    //initialization method to setup server 1st and then client...
    public static void setup(){
        chatServer.setup(serverPortNumber);
        //chatServer.setupClientSocket();
        chatClient.setup(machineName, clientPortNumber);
        chatServer.setupClientSocket();
    }
    
    //method to make program run via command line until exit command is supplied...
    public static void run() throws IOException{
        //initializing server and client sockets input and output streams respectively...
        DataInputStream serverInputStream = chatServer.dataInputStream(); //messages sent to the server (from client)
        PrintStream serverOutputStream = chatServer.dataOutputStream(); //messages sent from the server (to client)
        DataInputStream clientInputStream = chatClient.dataInputStream(); //messages sent to client (from server)
        PrintStream clientOutputStream = chatClient.dataOutputStream(); //messages sent from client (to server)
        //
        Scanner serverMsg = new Scanner(serverInputStream); 
        Scanner clientMsg = new Scanner(clientInputStream);
        
        System.out.println("");
        String command = sc.nextLine(); //app prompts client to enter a command
        while (!command.equals(":exit")){
            clientOutputStream.println("Client: "+chatClient.machineName+" sent a message at "+System.currentTimeMillis());
            serverOutputStream.println("Server says client said: "+command);
            
            System.out.println(serverMsg.nextLine()); //prints to console messages sent to server from client
            System.out.println(clientMsg.nextLine()); //prints to console message sent from server to client
            
            command = sc.nextLine(); //app prompts user to enter a command
        }
        
    }
    
    public static String getClientIP(){
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
                    String fullCredentials = iface.getDisplayName() + " " + ip;
                    System.out.println(iface.getDisplayName() + " " + ip);
                }
            }
        } 
        catch (SocketException e) {
            throw new RuntimeException(e);
        }
        
        return ip;
    }
    
    public static void exit(){
        System.out.println("*** Thank you for using Chat APP. Goodbye! ***");
    }
}
