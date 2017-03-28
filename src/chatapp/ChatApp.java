
package chatapp;

import java.io.IOException;
import java.net.Socket;
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
    static int portNumber;
    
    //app is run from command line with arguments: <machineName> and <portNumber> Eg. java ChatApp client 1234
    public static void main(String[] args) {
        sc = new Scanner(System.in);
        chatServer = new Server();
        chatClient = new Client();
        machineName = args[0];
        portNumber = Integer.parseInt(args[1]);
        
        setup();  
        System.out.println("******* Welcome to Chat APP! *******");
        run();
        //
        exit();
        //
        System.out.println("Test Complete!");
    
    }
  
    //initialization method to setup server 1st and then client...
    public static void setup(){
        chatServer.setup();
        chatClient.setup(machineName, portNumber);
    }
    
    //method to make program run via command line until exit command is supplied...
    public static void run(){
        String command = sc.nextLine(); //app prompts user to enter a command
        while (!command.equals(":exit")){
            System.out.println(command);
            command = sc.nextLine(); //app prompts user to enter a command
        }
        
    }
    
    public static void exit(){
        System.out.println("Thank you for using Chat APP. Goodbye!");
    }
}
