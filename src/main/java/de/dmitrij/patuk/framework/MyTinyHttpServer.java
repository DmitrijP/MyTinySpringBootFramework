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
            var uri = exchange.getRequestURI();
            System.out.printf("URI: %s%n", uri);
            var query = uri.getQuery();
            System.out.printf("Query: %s%n", query);
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(response.handle(query).getBytes(StandardCharsets.UTF_8));
            exchange.getResponseBody().close();
            exchange.close();
        });
    }

    public void stop() {
        server.stop(0);
    }

    public interface GetResponse{
        String handle(String query);
    }
}
