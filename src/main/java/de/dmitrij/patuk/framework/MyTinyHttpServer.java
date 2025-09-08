package de.dmitrij.patuk.framework;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class MyTinyHttpServer {
    private final int port;
    private HttpServer server;

    public MyTinyHttpServer(int port) {
        try {
            this.port = port;
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.printf("Server created on port %d %n", port);
        } catch (IOException e) {
            System.out.printf("Failed to create server on port %d%n",port);
            throw new RuntimeException(e);
        }
    }

    public void start() {
        server.start();
        System.out.printf("Server created on port %d %n", port);
    }

    public void bindContext(String path, GetResponse response) {
        System.out.printf("Binding context for path: %s%n", path);
        server.createContext(path, exchange -> {
            try {
                var uri = exchange.getRequestURI();
                System.out.printf("URI: %s%n", uri);
                var query = uri.getQuery();
                System.out.printf("Query: %s%n", query);
                var responseString = response.handle(query).getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().write(responseString);
                exchange.getResponseBody().close();
            } catch (Exception e) {
                System.out.printf("server error: %s%n", e.getMessage());
                exchange.sendResponseHeaders(500, 0);
                exchange.getResponseBody().write(e.getMessage().getBytes(StandardCharsets.UTF_8));
                exchange.getResponseBody().close();
            }
        });
    }

    public void stop() {
        server.stop(0);
    }

    public interface GetResponse{
        String handle(String query);
    }
}
