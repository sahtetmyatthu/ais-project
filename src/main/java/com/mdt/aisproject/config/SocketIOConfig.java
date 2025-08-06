package com.mdt.aisproject.config;

import com.corundumstudio.socketio.Configuration;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketIOConfig {

    @Value("${socket-server.host}")
    private String socketHost;

    @Value("${socket-server.port}")
    private int socketPort;

    private SocketIOServer server;

    @Bean
    public SocketIOServer socketIOServer(){
        Configuration config = new Configuration();
        config.setHostname(socketHost);
        config.setPort(socketPort);
        config.setOrigin("*");

        server = new SocketIOServer(config);
        server.start();

        server.addConnectListener(client -> log.info("Client connected: {}", client.getSessionId()));
        server.addDisconnectListener(client -> log.info("Client disconnected: {}", client.getSessionId()));

        return server;
    }

    @PreDestroy
    public void stopSocketServer(){
        server.stop();
        log.info("Socket.IO stopped");
    }
}
