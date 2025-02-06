package com.mynger.mychatapp.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mynger.mychatapp.model.ClientThreads;
import com.mynger.mychatapp.model.Server;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ServerService {
  private ServerSocket serverSocket = null;
  private Socket clientSocket = null; // socket sent from client to server
  private int maxUsers = 20; // The server can accept up to maxUser connections at a time.
  private ExecutorService threadPool = Executors.newFixedThreadPool(10); // Allow up to 10 clients
  private boolean running = false;
  private ArrayList<String> logMessages = new ArrayList<>();

  public String startServer(int port) {
    if (running) {
      return "Server is already running on port " + serverSocket.getLocalPort();
    }

    try {
      serverSocket = new ServerSocket(port);
      running = true;
      return "Server started on port " + port + "\n" + Server.getServerIP();
    } catch (IOException e) {
      return "Error starting server: " + e.getMessage();
    }
  }

  public void runServer(int portNumber) {
    ClientThreads[] threads = new ClientThreads[maxUsers];
    String reconnectMessage = "Please try to connect again later. Server has reached its maximum number of clients.";
    String line = "\n-------------------------------------------------------------------------------------------------------------------------\n";

    while (running && serverSocket != null) {
      try {
        clientSocket = serverSocket.accept();
        int i;
        // new client thread created for each client connected to server.
        for (i = 0; i < threads.length; i++) {
          if (threads.length == 0) {
            (threads[i] = new ClientThreads(clientSocket, threads)).start();
            break;
          } else if (threads[i] == null) {
            (threads[i] = new ClientThreads(clientSocket, threads)).start();
            break;
          }
        }

        // Once max number of clients are connected to server, server prevents other
        // potential clients until a connected client disconnects.
        if (i == maxUsers) {
          try (
              PrintStream output = new PrintStream(clientSocket.getOutputStream())) {
            output.println(
                line + reconnectMessage);
          }
          addLogMessage(reconnectMessage);
          clientSocket.close();
        }
      } catch (IOException e) {
        addLogMessage(e.getMessage());
      }
    }

    addLogMessage(stopServer());
  }

  public String stopServer() {
    if (!running) {
      return "Server is not running.";
    }

    running = false;
    threadPool.shutdownNow();

    try {
      serverSocket.close();
      return "Server stopped.";
    } catch (IOException e) {
      return "Error stopping server: " + e.getMessage();
    }
  }

  public void addLogMessage(String message) {
    log.info("Server | " + message);
    logMessages.add(message);
  }

  public ArrayList<String> getLogMessages() {
    return logMessages;
  }
}
