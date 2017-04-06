

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * @author gmdnko003
 */
public class Server implements Runnable {

    private Thread t;
    private static String threadName;
    static ServerSocket MyService; //stream socket to listen in for clients requests (TCP)
    static Socket serviceSocket = null; //socket sent from client to server
    static int portNumber, portNumber2; // server will use this port number for listening
    static DataInputStream input; //used to store client messages for prosessing
    static PrintStream output; //used to send messages back to client
    static Scanner sc;
    static ArrayList<String> log;
    static boolean quit = false;
    static boolean threadSwitch = true;
    public final static int FILE_SIZE = 6022386; // file size temporary hard coded

    Server(String name) {
        threadName = name;
        //System.out.println("Creating " + threadName);
    }

    public void run() {
        System.out.println("Running " + threadName);
        System.out.println(threadName);
        int i = 0;
        while (quit == false) {
            if (i == 0) {
                i++;
                portNumber = 4444;
                portNumber2 = 8888;
                System.out.println(getServerIP());
                setup();
                setupClientSocket();
            } else if (i == 1) {
                //running...
                run2();
            }
        }
        //exiting...
        closeSockets();
        exit();
    }

    public void start() {
        //System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    //initialization...
    public static void setup() {
        try {
            MyService = new ServerSocket(portNumber);
            System.out.println("Server socket setup complete!");
        } catch (IOException e) {
            System.out.println("ERROR: Server setup method says: " + e);
        }
    }

    //capturing client socket for processing...
    public static void setupClientSocket() {
        try {
            System.out.println("Server waiting on Clients to connect");
            serviceSocket = MyService.accept();
            System.out.println("Server-client socket setup complete!");
        } catch (IOException e) {
            System.out.println("ERROR: Server-client setup method says: " + e);
        }
    }

    //method to make program run via command line until exit command is supplied...
    public static void run2() {
        DataInputStream serverInputStream = dataInputStream(); //messages sent to the server (from client)
        PrintStream serverOutputStream = dataOutputStream(); //messages sent from the server (to client)
        Scanner serverMsgIn = new Scanner(serverInputStream, "UTF-8"); //used to store incoming messages from client 
        log = new ArrayList();
        log.add("Chat started"); //add first message to server log
        String nextMsg; //buffer to store incoming messages from client
        int fileSendSwitch = 0;
        int fileReceiveSwitch = 0;
        //int extension = 1;
        while (!log.isEmpty()) { //while the log is not empty
            //perhaps don't qualify threads    
            try {
                if (!serverMsgIn.hasNextLine()) { //if scanner does not have next line                   
                    //setupClientSocket();
                }
                else { //Scanner has next line  
                    //System.out.println("Waiting for client command...");
                    nextMsg = serverMsgIn.nextLine(); //is the next message incoming from client
                    if (nextMsg.contains(":sendfile") && fileReceiveSwitch == 0) { //in the case that a client sends an image                     
                        System.out.println("Client is attempting to send file...");
                        log.add("Client is attempting to send file...");
                        //serverOutputStream.println(log.get(log.size()-1)); //sends client last message in the log, ideally the whole log
                        serverOutputStream.println(log); //sends client last message in the log, ideally the whole log: OUTPUT
                        //
                        System.out.println("Waiting for file name:");
                        String fileName = serverMsgIn.nextLine(); //INPUT
                        System.out.println("File name: "+fileName);
                        serverFileRecieve(fileName); //key method that implements, recieves file: INPUT
                        System.out.println("File has been recieved!!!");
                        //progression...
                        while (serverMsgIn.hasNext()){
                            System.out.println(serverMsgIn.nextLine()); //CLEARING REMNANTS OF THE IMAGE
                        }
                        log.add("File "+fileName+" has successfully been recieved!!");
                        serverOutputStream.println(log); //sends client last message in the log, ideally the whole log
                        //
                        fileReceiveSwitch = 1;  
                    } 
                    else if (nextMsg.contains(":getfile") && fileSendSwitch == 0) { //in the case that a client sends an image                     
                        System.out.println("Client is attempting to get file...");
                        log.add("Client is attempting to get file...");
                        //serverOutputStream.println(log.get(log.size()-1)); //sends client last message in the log, ideally the whole log
                        serverOutputStream.println(log); //sends client last message in the log, ideally the whole log
                        //
                        System.out.println("Waiting for client to send file name...");
                        //
                        String fileName = serverMsgIn.nextLine(); //takes in name of file 
                        System.out.println("File name: "+fileName);
                        log.add("File name :"+fileName);
                        serverOutputStream.println(log); //sends client last message in the log, ideally the whole log
                        //
                        serverFileSend(fileName); //key implementing method, sends file to client
                        System.out.println("File has been sent!!!");
                        //Output stream might have been closed
                        //progression...
                        fileSendSwitch = 1;
                    }
                    else { //in the case that a simple text message is sent
                        System.out.println(nextMsg); //print to console incoming messages from client
                        log.add(nextMsg); //add the clients message to the log
                        //serverOutputStream.println(log.get(log.size()-1)); //sends client last message in the log, ideally the whole log
                        serverOutputStream.println(log); //sends client last message in the log, ideally the whole log
                        fileSendSwitch = 0;
                        fileReceiveSwitch = 0;
                    }
                }
            } catch (Exception e) {
                System.out.println("Server run method exception says: " + e);
            }
        }
    }

    public static void serverFileRecieve(String fileToReceive) throws FileNotFoundException, IOException {
        int bytesRead;
        int current = 0;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ServerSocket servsock = null;
        Socket sock = null;
        try {
            servsock = new ServerSocket(portNumber2);
            System.out.println("Connecting...");
            // receive file
            byte [] mybytearray  = new byte [FILE_SIZE];
            InputStream is = input;
            fos = new FileOutputStream(fileToReceive);
            bos = new BufferedOutputStream(fos);
            bytesRead = is.read(mybytearray,0,mybytearray.length);
            current = bytesRead;
            do {
               bytesRead =
                  is.read(mybytearray, current, (mybytearray.length-current));
               if(bytesRead >= 0) current += bytesRead;
            } while(bytesRead > -1);
            bos.write(mybytearray, 0 , current);
            bos.flush();
            is.close();
            System.out.println("File " + fileToReceive + " downloaded (" + current + " bytes read)");
        }
        finally {
            if (fos != null) fos.close();
            if (bos != null) bos.close();
            if (servsock != null) servsock.close();
        } 
    }
    
    public static void serverFileSend(String fileToSend) throws FileNotFoundException, IOException{
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            OutputStream os = null;
            ServerSocket servsock = null;
            Socket sock = null;
            try {
                servsock = new ServerSocket(portNumber2);       
                System.out.println("Waiting...");
                try {
                    sock = servsock.accept();
                    System.out.println("Accepted connection : " + sock);
                    // send file
                    File myFile = new File (fileToSend);
                    byte [] mybytearray  = new byte [(int)myFile.length()];
                    fis = new FileInputStream(myFile);
                    bis = new BufferedInputStream(fis);
                    bis.read(mybytearray,0,mybytearray.length);
                    os = sock.getOutputStream();
                    System.out.println("Sending " + fileToSend + "(" + mybytearray.length + " bytes)");
                    os.write(mybytearray,0,mybytearray.length);
                    os.flush();
                    System.out.println("Done.");
                }
                finally {
                    if (bis != null) bis.close();
                    if (os != null) os.close();
                    if (sock!=null) sock.close();
                }        
        }
        finally {
            if (servsock != null) servsock.close();
        }
    }

    //server processing requests from the client...
    public static DataInputStream dataInputStream() {
        try {
            input = new DataInputStream(serviceSocket.getInputStream());
            //System.out.println("Server dataInputStream method says: input = "+input);
        } catch (IOException e) {
            System.out.println("ERROR: Server dataInputStream method says: " + e);
        }
        return input;
    }

    //client output stream to send data to the server
    public static PrintStream dataOutputStream() {
        try {
            output = new PrintStream(serviceSocket.getOutputStream());
            //System.out.println("Server dataOutputStream method says: output = "+output);
        } catch (IOException e) {
            System.out.println("ERROR: Server dataOutputStream method says: " + e);
        }
        return output;
    }

    //closing server sockets
    public static void closeSockets() {
        try {
            output.close();
            input.close();
            serviceSocket.close();
            MyService.close();
            System.out.println("Server closeSockets method says: All sockets closed successfully!");
        } catch (IOException e) {
            System.out.println("ERROR: Server closeSockets method says: " + e);
        }
    }

    public static String getServerIP() {
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

        return ip;
    }

    public boolean isDead() {
        if (t.isAlive()) {
            return false;
        } else {
            return true;
        }
    }

    public static void exit() {
        System.out.println("Server closed.");
    }

}
