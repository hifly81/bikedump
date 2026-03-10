package org.hifly.bikedump.integration.strava;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class StravaOAuthLocalServer {

    public static class OAuthResult {
        public String code;
        public String state;
        public String error;
    }

    private final String host;
    private final int port;
    private HttpServer server;

    public StravaOAuthLocalServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getRedirectUri() {
        return "http://" + host + ":" + port + "/strava/callback";
    }

    public OAuthResult waitForCallback(long timeoutMillis) throws Exception {
        OAuthResult res = new OAuthResult();
        CountDownLatch latch = new CountDownLatch(1);

        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/strava/callback", (HttpExchange exchange) -> {
            try {
                Map<String, String> query = null;
                try {
                    query = parseQuery(exchange.getRequestURI().getRawQuery());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                res.code = query.get("code");
                res.state = query.get("state");
                res.error = query.get("error");

                String html = "<html><body><h2>Bikedump</h2>"
                        + (res.error != null ? "<p>Auth error: " + escape(res.error) + "</p>" : "<p>Login OK. You can close this tab.</p>")
                        + "</body></html>";

                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                exchange.sendResponseHeaders(200, html.getBytes("UTF-8").length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(html.getBytes("UTF-8"));
                }
            } finally {
                latch.countDown();
            }
        });

        server.start();

        boolean ok = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        stop();

        if (!ok) throw new RuntimeException("OAuth timeout waiting for Strava callback");
        return res;
    }

    public void stop() {
        if (server != null) {
            try { server.stop(0); } catch (Exception ignored) {}
            server = null;
        }
    }

    private static Map<String, String> parseQuery(String rawQuery) throws Exception {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return map;
        String[] parts = rawQuery.split("&");
        for (String p : parts) {
            int idx = p.indexOf('=');
            String k = idx >= 0 ? p.substring(0, idx) : p;
            String v = idx >= 0 ? p.substring(idx + 1) : "";
            map.put(URLDecoder.decode(k, "UTF-8"), URLDecoder.decode(v, "UTF-8"));
        }
        return map;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}