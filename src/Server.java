import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Scanner;

public class Server{
    static ServerSocket MyService = null; //stream socket to listen in for clients requests (TCP)
    static Socket clientSocket = null; //socket sent from client to server
    static int users = 20; // The server can accept up to maxUser connections at a time.
    static clientThreads[] threads = new clientThreads[users];
    
    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);
        String response;
        int portNumber = 4444; //server will use this port number for listening for new client connections
        String port = "";
        String line = "\n-------------------------------------------------------------------------------------------------------------------------\n";
        while(true){
            System.out.println("Port number = " +portNumber +"\nEnter 'yes' to continue or 'no' to  change the default port number:");
            response = sc.nextLine();
            if (response.equalsIgnoreCase("no")){
                System.out.println("Enter the new port number:");
                try{
                    port = sc.nextLine();
                    portNumber = Integer.parseInt(port);
                    break;
                }
                catch (NumberFormatException e){
                    System.err.println("Please enter a port number with digits only. You entered: "+port);
                }
            }    
            else if(response.equalsIgnoreCase("yes")){
                break;
            }
            else {
                System.out.println("Please enter 'yes' or 'no'. You entered: "+response);
            }
        }
        getServerIP(); //prints out full details of inet ip address
        
        //create socket called MyService on given port number
        try {
            MyService = new ServerSocket(portNumber);
            System.out.println("Server is now listening for clients...");
        } catch (IOException e) {
            System.out.println(e);
        }
        
        //new client thread created for each client connected to server.
        while (true){
            try{
                clientSocket = MyService.accept();
                int i;
                for (i = 0; i< threads.length; i++){
                    if(threads.length == 0){
                      (threads[i] =  new clientThreads(clientSocket, threads)).start();
                      break;  
                    }
                    else if(threads[i] == null){
                        (threads[i] =  new clientThreads(clientSocket, threads)).start();
                        break;
                    }
                }
                
                // Once max number of clients are connected to server, server prevents other potential clients until a connected client disconnects.
                if (i == users){
                    try (PrintStream output = new PrintStream(clientSocket.getOutputStream())) {
                        output.println(line+"Please try to connect again later. Server has reached its maximum number of clients.");
                    }
                    clientSocket.close(); 
                }
            }
            catch (IOException e){
                System.out.println(e);
            }
        }
    }
    
    public static void getServerIP() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    //String fullCredentials = iface.getDisplayName() + " " + ip;
                    System.out.println(ip);
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
  
}


//this class is handles the threads responsible for communicating with clientts
class clientThreads extends Thread{
    private  String client = null;
    private  BufferedReader input = null;
    private  PrintStream output = null;
    private  Socket clientSocket = null;
    private  clientThreads[] threads = null;
    private final  int users;
    String line = "\n-------------------------------------------------------------------------------------------------------------------------\n";
    String help = line+"To leave enter '/exit' in a new line.\nTo send private messages enter user name with '@' sign in front of name, a space and the message e.g. @Bob 'Hey Bob'\nTo display these intructions again enter '/help'"+line;
    
    public clientThreads(Socket clientSocket, clientThreads[] threads){
        this.clientSocket = clientSocket;
        this.threads = threads;
        users = threads.length;
    }
    
    @Override
    public void run(){
        try{
            // input and output streams created for each client thread
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintStream(clientSocket.getOutputStream());
            String user;
            while (true){
                output.println("Enter your name to display in chat: ");
                user = input.readLine().trim();
                if (!user.contains("@")){
                    break;
                }
                else{
                    output.println("The name should not contain '@' character.");
                }
            }
            
            //Opening messages for clients.
            output.println(line+"******* Welcome to ChatAPP, " +user + "! *******\n");
            output.println(help);
            synchronized(this){
                for (clientThreads thread : threads) {
                    if (thread != null && thread == this) {
                        client = "@"+user;
                        break;
                    }
                }
                for (clientThreads thread : threads) {
                    if (thread != null && thread != this) {
                        thread.output.println("***New user: " +user+" has entered the chat room***");
                    }
                }                       
            }
            
            //Handling communication between clients.
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            
            while(true){
                String nxtMsg = input.readLine();
                Date date = new Date();
                if (nxtMsg.startsWith("/exit")){
                    break;
                }
                // Privated messages directed to intended user
                if (nxtMsg.startsWith("@")){
                    String[] msg = nxtMsg.split("\\s", 2);
                    if (msg.length > 1 && msg[1] != null){
                        msg[1] = msg[1].trim();
                        if (!msg[1].isEmpty()){
                            synchronized(this){
                                for (clientThreads thread : threads) {
                                    if (thread != null && thread != this && thread.client != null && thread.client.equals(msg[0])) {
                                        thread.output.println("<"+dateFormat.format(date)+"> "+user+": "+msg[1]);
                                        this.output.println("<"+dateFormat.format(date)+"> "+user+": " + msg[1]);
                                        break;
                                    }
                                }    
                            }
                        }
                    }
                }
                else {
                    // Public messages intended to all users
                    synchronized(this){
                        if (nxtMsg.equals("/help")){
                            this.output.println(help);
                        }
                        else{
                            for (clientThreads thread : threads) {
                                if (thread != null && thread.client != null) {
                                    thread.output.println("<"+dateFormat.format(date)+"> "+user+": "+nxtMsg);
                                }
                            }
                    }
                    }
                }
            }
            //when user exits, breaks out of loop and displays following message to all users
            synchronized(this){
                for (clientThreads thread : threads) {
                    if (thread != null && thread != this && thread.client != null) {
                        thread.output.println("***User: "+user+" has left the chat room***");
                    }
                }
            }
            output.println("***"+user+" You've logged out***");
        
            //Create space in threads list for another client
            synchronized(this){
                for (int i =0; i<threads.length;i++){
                    if (threads[i] == this){
                        threads[i] = null;
                    }
                }
            }
            //Close socket, input and output streams.
            input.close();
            output.close();
            clientSocket.close();
        }
        catch (IOException e){
            System.out.println(e);
        }
    }

}