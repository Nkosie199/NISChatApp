
package chatapp;

import java.io.IOException;
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
    public static void main(String[] args) {
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
    public static void run(){
        chatServer.dataInputStream();
        chatServer.dataOutputStream();
        chatClient.dataInputStream();
        chatClient.dataOutputStream();
        
        String command = sc.nextLine(); //app prompts user to enter a command
        while (!command.equals(":exit")){
            System.out.println(command);
            
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
