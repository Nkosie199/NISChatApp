import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Scanner;

public class Client implements Runnable{
    static Socket MyClient = null; //uses TCP
    static Scanner s;
    private static BufferedReader serverInput = null;
    private static BufferedReader clientInput = null;
    private static PrintStream output = null;
    private static boolean socketClosed = false;

    public static void main(String[] args) throws IOException{
        String machineName = "localhost"; //specifies machine name in IP address form
        int portNumber = 4444;
        String res;
        String port = "";
        String line = "\n----------------------------------------------------------------------------------------------------------------------\n";
        s = new Scanner(System.in);
        while(true){
            System.out.println(line+"Host = " +machineName+", port number = " +portNumber +"\nEnter 'yes' to continue or enter 'no' to change the default settings:");
            res = s.nextLine();
            if (res.equalsIgnoreCase("no")){
                System.out.println(line+"Please enter a host to connect to: ");
                machineName = s.next();
                System.out.println(line+"Enter port number to connect to: ");
                try{
                    port = s.next();
                    portNumber = Integer.valueOf(port).intValue();
                    break;
                }
                catch (NumberFormatException e){
                    System.err.println(line+"Please enter a port number with no letters or special characters(digits only). You entered: "+port);
                }
            }    
            else if(res.equalsIgnoreCase("yes")){
                break;
            }
            else {
                System.out.println(line+"Please enter just 'yes' or 'no'. You entered: "+res);
            }
        }      
        
        //setup connection...
        try{
            MyClient = new Socket(machineName, portNumber);
            clientInput = new BufferedReader(new InputStreamReader(System.in));
            output = new PrintStream(MyClient.getOutputStream());
            serverInput = new BufferedReader(new InputStreamReader(MyClient.getInputStream()));
        } 
        catch (UnknownHostException e){
            System.err.println(line+"This host is unknown: " +machineName);
        }
        catch (IOException e){
            System.err.println(line+"Unable to get Input/output connection of host: "+machineName);
        }
        
        //main run loop contained here...
        if (MyClient != null && output != null && serverInput != null) {
            try{
                //Threads read server input
                new Thread(new Client()).start();
                //Writes to server 
                while (!socketClosed){
                    output.println(clientInput.readLine().trim());
                }
                output.close();
                serverInput.close();
                MyClient.close();
            }
            catch (IOException e){
                System.err.println(line+"IOException: " +e);
            }
        }
        
    }
    
    @Override    
    public void run(){
        String serverMessage;
        int i = 0;
        String line = "\n-----------------------------------------------------------------------------------------------------------------------\n";
        String data ="";
        try{
            while((serverMessage = serverInput.readLine()) != null){
                if (!data.contains(serverMessage)){
                    System.out.println(serverMessage);
                    if (serverMessage.contains(line+"You've logged out"+line)){
                        break;
                    }
                }
            }
            socketClosed = true;
        }
        catch (IOException e){
            System.err.println(line+"IOException: " +e);
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
    
}