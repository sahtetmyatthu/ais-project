package com.mdt.aisproject.entry;


import com.mdt.aisproject.service.AisService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
@Component
@RequiredArgsConstructor
@AllArgsConstructor
public class TcpServer {

    @Value("${client-port.port}")
    private Integer port;

    @Autowired
    private AisService aisService;
    @PostConstruct
    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("[TCP] Listening on 0.0.0.0:1280");
                while (!Thread.currentThread().isInterrupted()) {
                    Socket clientSocket = serverSocket.accept();
                    String clientIp = clientSocket.getInetAddress().getHostAddress();
                    System.out.println("[TCP " + clientIp + "] Connected");
                    new Thread(() -> handleClient(clientSocket, clientIp)).start();
                }
            } catch (Exception e) {
                System.out.println("[TCP] Server error: " + e.getMessage());
            }
        }).start();
    }
    private void handleClient(Socket clientSocket, String clientIp) {
        System.out.println("[TCP " + clientIp + "] Received");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {

                    aisService.handleNmeaSentence(line, clientIp);

            }
        } catch (Exception e) {
            System.out.println("[TCP " + clientIp + "] Client handler error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("[TCP " + clientIp + "] Connection closed");
            } catch (Exception e) {
                System.out.println("[TCP " + clientIp + "] Error closing socket: " + e.getMessage());
            }
        }
    }
}