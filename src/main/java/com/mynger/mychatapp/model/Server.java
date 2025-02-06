package com.mynger.mychatapp.model;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server {

  static ServerSocket serverSocket = null; //stream socket to listen in for clients requests (TCP)
  static Socket clientSocket = null; //socket sent from client to server
  static int users = 20; // The server can accept up to maxUser connections at a time.
  static ClientThreads[] threads = new ClientThreads[users];

  public static void runServer(int portNumber) {
    //create socket called MyService on given port number
    try {
      serverSocket = new ServerSocket(portNumber);
      log.info("Server is now listening for clients...");
    } catch (IOException e) {
      log.error(e.getMessage());
    }

    String line =
      "\n-------------------------------------------------------------------------------------------------------------------------\n";
    
      while (true) {
        try {
          clientSocket = serverSocket.accept();
          int i;
          //new client thread created for each client connected to server.
        for (i = 0; i < threads.length; i++) {
          if (threads.length == 0) {
            (threads[i] = new ClientThreads(clientSocket, threads)).start();
            break;
          } else if (threads[i] == null) {
            (threads[i] = new ClientThreads(clientSocket, threads)).start();
            break;
          }
        }

        // Once max number of clients are connected to server, server prevents other potential clients until a connected client disconnects.
        if (i == users) {
          try (
            PrintStream output = new PrintStream(clientSocket.getOutputStream())
          ) {
            output.println(
              line +
              "Please try to connect again later. Server has reached its maximum number of clients."
            );
          }
          clientSocket.close();
        }
      } catch (IOException e) {
        log.error(e.getMessage());
      }
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
        String fullCredentials = iface.getDisplayName();
        while (addresses.hasMoreElements()) {
          InetAddress addr = addresses.nextElement();
          ip += addr.getHostAddress() + "\n ";
          log.info(ip);
        }
        fullCredentials += "\n " + ip;
        log.info("Full credentials: " + fullCredentials);
      }
      return ip;
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
  }
}

