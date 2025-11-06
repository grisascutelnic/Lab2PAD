package labs.partea1;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import labs.partea1.MySQLConnector;

import java.net.URI;

public class DWServer {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8081;
        MySQLConnector.connect();

        ResourceConfig rc = new ResourceConfig().packages("ro.example.dw.controllers");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create("http://0.0.0.0:" + port + "/"), rc);

        System.out.println("DW running at http://localhost:" + port + "/");
    }
}
