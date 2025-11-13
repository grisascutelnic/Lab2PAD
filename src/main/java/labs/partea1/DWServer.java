package labs.partea1;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class DWServer {

    public static final String BASE_URI = "http://0.0.0.0:%d/";

    public static void main(String[] args) throws Exception {

        int port = (args.length > 0)
                ? Integer.parseInt(args[0])
                : 8081;

        // Încarcă configurarea serverului
        ResourceConfig rc = new ResourceConfig()
                .packages("labs.partea1.controllers", "labs.partea1.model")
                .register(org.glassfish.jersey.jaxb.internal.XmlJaxbElementProvider.App.class)
                .register(org.glassfish.jersey.jaxb.internal.XmlJaxbElementProvider.Text.class)
                .register(org.glassfish.jersey.jaxb.internal.XmlCollectionJaxbProvider.App.class)
                .register(org.glassfish.jersey.jaxb.internal.XmlCollectionJaxbProvider.Text.class);

        // Creează serverul HTTP
        String uri = String.format(BASE_URI, port);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(uri),
                rc
        );

        System.out.println("DW running at: " + uri);
        System.out.println("Available endpoints:");
        System.out.println("   GET     /employees");
        System.out.println("   GET     /employees/{id}");
        System.out.println("   PUT     /employees/{id}");
        System.out.println("   POST    /employees");
        System.out.println("----------------------------------");

        Thread.currentThread().join(); // ține serverul pornit
    }
}
