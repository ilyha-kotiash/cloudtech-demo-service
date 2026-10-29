import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final Pattern NAME = Pattern.compile("\"name\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", Main::handle);
        server.start();
        System.out.println("Server started on port " + port);
    }

    private static void handle(HttpExchange ex) throws IOException {
        String name = "World";
        if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Matcher m = NAME.matcher(body);
            if (m.find() && !m.group(1).isEmpty()) {
                name = m.group(1);
            }
        }
        String safe = name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        byte[] resp = ("Hello, " + safe + "!").getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(200, resp.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(resp);
        }
    }
}
