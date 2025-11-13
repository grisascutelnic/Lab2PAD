package labs.partea2;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyServer {

    private final List<String> backends;
    private final HttpClient client;
    private final RedisCacheManager cache;
    private final AtomicInteger rr = new AtomicInteger(0);
    private final int cacheTTL = 10;

    public ProxyServer(List<String> backends) {
        this.backends = backends;
        this.client = HttpClient.newHttpClient();
        this.cache = new RedisCacheManager();
    }

    private String nextBackend() {
        return backends.get(Math.abs(rr.getAndIncrement() % backends.size()));
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleRequest);
        server.setExecutor(Executors.newCachedThreadPool());

        System.out.println("Proxy running on http://localhost:" + port);
        server.start();
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().toString();
        String cacheKey = method + ":" + path;

        System.out.println("[REQ] " + method + " " + path);

        try {
            // CACHE CHECK
            if (method.equalsIgnoreCase("GET")) {
                String cached = cache.get(cacheKey);
                if (cached != null) {
                    System.out.println("[CACHE HIT]");
                    byte[] body = cached.getBytes();
                    exchange.sendResponseHeaders(200, body.length);
                    exchange.getResponseBody().write(body);
                    exchange.close();
                    return;
                }
            }

            // LOAD BALANCER
            String backend = nextBackend();
            String targetUrl = backend + path;

            System.out.println("[FORWARD] → " + targetUrl);

            // BODY FOR POST/PUT
            byte[] reqBody = exchange.getRequestBody().readAllBytes();
            HttpRequest.BodyPublisher publisher =
                    reqBody.length > 0 ?
                            HttpRequest.BodyPublishers.ofByteArray(reqBody) :
                            HttpRequest.BodyPublishers.noBody();

            HttpRequest forwardReq = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .method(method, publisher)
                    .build();

            HttpResponse<byte[]> resp =
                    client.send(forwardReq, HttpResponse.BodyHandlers.ofByteArray());

            byte[] respBytes = resp.body();

            // COPY HEADERS (except forbidden)
            resp.headers().map().forEach((key, vals) -> {
                if (!key.equalsIgnoreCase("transfer-encoding") &&
                        !key.equalsIgnoreCase("content-length") &&
                        !key.equalsIgnoreCase("connection")) {
                    vals.forEach(v -> exchange.getResponseHeaders().add(key, v));
                }
            });

            exchange.sendResponseHeaders(resp.statusCode(), respBytes.length);
            exchange.getResponseBody().write(respBytes);

            if (method.equalsIgnoreCase("GET") && resp.statusCode() == 200) {
                cache.set(cacheKey, new String(respBytes), cacheTTL);
            } else {
                cache.invalidateAll();
            }

        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        } finally {
            exchange.close();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080; // default
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        List<String> nodes = List.of(
                "http://localhost:8081",
                "http://localhost:8082"
        );

        new ProxyServer(nodes).start(port);
    }

}
