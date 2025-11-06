package labs.partea2;

import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import labs.partea2.RedisCacheManager;

import com.sun.net.httpserver.HttpServer;

public class ProxyServer {
    private final List<String> backends;
    private final HttpClient client;
    private final RedisCacheManager cache;
    private final AtomicInteger rr = new AtomicInteger(0);
    private final int cacheTTL = 10; // secunde

    public ProxyServer(List<String> backends) {
        this.backends = backends;
        this.client = HttpClient.newBuilder().executor(Executors.newCachedThreadPool()).build();
        this.cache = new RedisCacheManager();
    }

    private String nextBackend() {
        int i = Math.abs(rr.getAndIncrement() % backends.size());
        return backends.get(i);
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().toString();
            String cacheKey = method + ":" + path;

            if ("GET".equalsIgnoreCase(method)) {
                String cached = cache.get(cacheKey);
                if (cached != null) {
                    byte[] body = cached.getBytes();
                    exchange.sendResponseHeaders(200, body.length);
                    exchange.getResponseBody().write(body);
                    exchange.close();
                    return;
                }
            }

            String backend = nextBackend();
            String target = backend + path;
            HttpRequest req = HttpRequest.newBuilder(URI.create(target))
                    .method(method, HttpRequest.BodyPublishers.noBody()).build();

            client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .whenComplete((resp, err) -> {
                        try {
                            if (err != null) {
                                exchange.sendResponseHeaders(500, 0);
                                exchange.close();
                                return;
                            }
                            String body = resp.body();
                            byte[] bytes = body.getBytes();
                            exchange.sendResponseHeaders(resp.statusCode(), bytes.length);
                            exchange.getResponseBody().write(bytes);
                            if ("GET".equalsIgnoreCase(method) && resp.statusCode() == 200) {
                                cache.set(cacheKey, body, cacheTTL);
                            }
                            exchange.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Proxy running at http://localhost:" + port);
    }

    public static void main(String[] args) throws Exception {
        List<String> nodes = List.of("http://localhost:8081", "http://localhost:8082");
        new ProxyServer(nodes).start(8080);
    }
}
