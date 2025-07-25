package org.example;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.*;

import org.example.modles.*;
import org.example.serialization.*;

public class App {
    private final int port;
    private final Path distFolder;
    private final RequestHandler requestHandler;
    private final ResponseGenerator responseGenerator;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public App(int port, String distPath) {
        this.port = port;
        this.distFolder = Paths.get(distPath);
        this.requestHandler = new RequestHandler();
        this.responseGenerator = new ResponseGenerator(distFolder);
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        
        System.out.println("Server started on port " + port);
        System.out.println("Serving files from: " + distFolder.toAbsolutePath());
        
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {
            
            HttpRequest request = requestHandler.parseRequest(in);
            if (request != null) {
                HttpResponse response = responseGenerator.generateResponse(request);
                response.writeTo(out);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        String distPath = args.length > 1 ? args[1] : "../dist";
        
        App server = new App(port, distPath);
        
        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
                System.out.println("Server stopped gracefully");
            } catch (IOException e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
        }));
        
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}