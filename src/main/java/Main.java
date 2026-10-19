import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = 8080;

        HttpServer server = HttpServer.create(
            new InetSocketAddress(port), 0
        );

        Path publicFolder = Paths.get("src/main/resources/public")
                                 .toAbsolutePath()
                                 .normalize();

        server.createContext("/", exchange -> {
            try {
                String requestPath = exchange.getRequestURI().getPath();

                if (requestPath.equals("/")) {
                    requestPath = "/index.html";
                }

                Path file = publicFolder.resolve(
                    requestPath.substring(1)
                ).normalize();

                if (!file.startsWith(publicFolder)
                        || !Files.isRegularFile(file)) {
                    sendResponse(exchange, 404, "Page not found");
                    return;
                }

                String contentType = Files.probeContentType(file);

                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                if (file.toString().endsWith(".css")) {
                    contentType = "text/css; charset=UTF-8";
                } else if (file.toString().endsWith(".js")) {
                    contentType = "application/javascript; charset=UTF-8";
                } else if (file.toString().endsWith(".html")) {
                    contentType = "text/html; charset=UTF-8";
                }

                byte[] content = Files.readAllBytes(file);

                exchange.getResponseHeaders().set(
                    "Content-Type", contentType
                );

                exchange.sendResponseHeaders(200, content.length);
                exchange.getResponseBody().write(content);
                exchange.close();

            } catch (Exception e) {
                sendResponse(exchange, 500, "Server error");
                e.printStackTrace();
            }
        });

        server.start();

        System.out.println("MashTech Studio is running!");
        System.out.println("Open: http://localhost:" + port);
    }

    private static void sendResponse(
            HttpExchange exchange, int status, String message
    ) throws IOException {
        byte[] content = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(status, content.length);
        exchange.getResponseBody().write(content);
        exchange.close();
    }
}
