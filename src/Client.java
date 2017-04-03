
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * @author gmdnko003
 */
public class Client {
    static Server chatServer;
    static Socket MyClient; //stream socket (TCP)
    //String machineName = args[0];
    static String machineName; //specifies machine name in IP address form
    static String userName; //specifies client chosen user-name
    //int portNumber = Integer.parseInt(args[1]);
    static int portNumber; //server open port used to send requests to server < 1023 < 65536
    static DataInputStream input; //stores server responses
    static PrintStream output; //stores message to be sent to server
    static Scanner sc;
    
    public static void main(String[] args) throws IOException{
        System.out.println("Please enter server domain name/ IP address: ");
        //System.out.println(getClientIP());
        sc = new Scanner(System.in);
        machineName = sc.nextLine();
        //machineName = "localhost";
        
        System.out.println("Please enter your user name: ");
        userName = sc.nextLine();
        
        //System.out.println("Please specify the client port number you wish to use: ");
        //portNumber = sc.nextInt();
        portNumber = 4444;
        
        //initializing...
        //chatServer = new Server();
        setup();
        
        System.out.println("******* Welcome to Chat APP! *******");
        System.out.println("");
        run();
        //upon exiting...
        exit();
        closeSockets();
        //FIN!
        System.out.println("Test Complete!");
    }
    
    //initializing...
    public static void setup(){
        
        try{
            MyClient = new Socket(machineName, portNumber);
            System.out.println("Client socket setup complete!");
        }
        catch (IOException e){
            System.out.println("ERROR: Client setup method says: "+e);
        }
        
    }

    //method to make program run via command line until exit command is supplied...
    public static void run() throws IOException{
        //initializing server and client sockets input and output streams respectively...   
        DataInputStream clientInputStream = dataInputStream(); //messages sent to client (from server)
        PrintStream clientOutputStream = dataOutputStream(); //messages sent from client (to server)
        //
        Scanner clientMsgIn = new Scanner(clientInputStream); //used to store incoming messages from server
        //ArrayList<String> log = new ArrayList();

        String command = userName+" has entered the conversation"; //app prompts client to enter a command
        clientOutputStream.println(command); //send entry message to server
        System.out.println(clientMsgIn.nextLine()); //prints to console message sent from server to client
        //sc.nextLine(); //to get rid of the blank 1st message
        while (!command.equals(":exit")){
            // now we will continuously send messages to the server and print out the servers response...
            command = sc.nextLine(); //app prompts user to enter a command
            clientOutputStream.println("("+System.currentTimeMillis()+") "+userName+": "+command);
            
            //log = clientMsgIn.nextLine(); //have to covert this to an ArrayList to manipulate its elements
            
            //while (clientMsgIn.hasNextLine()){
                //System.out.println(clientMsgIn.nextLine()); //prints to console message sent from server to client
                formatLogEntry(clientMsgIn.nextLine());
            //}
            
        }
        command = userName+" has left the conversation"; //app prompts client to enter a command
        clientOutputStream.println(command); //send entry message to server
        System.out.println(clientMsgIn.nextLine()); //prints to console message sent from server to client
        System.out.println("");
    }
    
    //adequately prints/ displays to returned server log to console 
    public static void formatLogEntry(String inMsg){
        String out = inMsg.substring(1, inMsg.length()-1); //remove brackets
        //System.out.println("out = "+out);
        String[] out2 = out.split(", ");
        for (String s: out2){
            System.out.println(s);
        }     
    }
    
    //client processing responses from the server...
    public static DataInputStream dataInputStream(){
        try{
            input = new DataInputStream(MyClient.getInputStream());
            //System.out.println("Client dataInputStream method says: input = "+input);
        }
        catch(Exception e){
            System.out.println("ERROR: Client dataInputStream method says: "+e);
        }
        return input;
    }
    
    //client output stream to send data to the server
    public static PrintStream dataOutputStream(){
        try{
            output = new PrintStream(MyClient.getOutputStream());
            //System.out.println("Client dataOutputStream method says: output = "+output);
        }
        catch(Exception e){
            System.out.println("ERROR: Client dataOutputStream method says: "+e);
        }
        return output;
    }
    
    //closing server sockets
    public static void closeSockets(){
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
                    //String fullCredentials = iface.getDisplayName() + " " + ip;
                    System.out.println(ip);
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
