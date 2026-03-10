package org.hifly.bikedump.integration.strava;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hifly.bikedump.domain.StravaPref;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Strava HTTP client using HttpURLConnection + Jackson for JSON parsing.
 *
 * Supports:
 * - list activities with optional after/before
 * - streams for latlng/altitude/time (to generate GPX)
 */
public class StravaClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String clientId;
    private final String clientSecret;

    public StravaClient(String clientId, String clientSecret) {
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        this.clientSecret = Objects.requireNonNull(clientSecret, "clientSecret");
    }

    // ---------------- OAuth ----------------

    public String buildAuthorizeUrl(String redirectUri, String state) throws Exception {
        return "https://www.strava.com/oauth/authorize"
                + "?client_id=" + enc(clientId)
                + "&redirect_uri=" + enc(redirectUri)
                + "&response_type=code"
                + "&approval_prompt=auto"
                + "&scope=" + enc("read,activity:read_all")
                + "&state=" + enc(state);
    }

    public void exchangeCodeForToken(StravaPref pref, String code) throws Exception {
        String body = "client_id=" + enc(clientId)
                + "&client_secret=" + enc(clientSecret)
                + "&code=" + enc(code)
                + "&grant_type=authorization_code";

        String json = postForm("https://www.strava.com/oauth/token", body);
        applyTokenJson(pref, json);
    }

    public void refreshIfNeeded(StravaPref pref) throws Exception {
        if (pref == null || !pref.isConnected()) return;
        if (!pref.isAccessTokenExpired()) return;

        String body = "client_id=" + enc(clientId)
                + "&client_secret=" + enc(clientSecret)
                + "&refresh_token=" + enc(pref.getRefreshToken())
                + "&grant_type=refresh_token";

        String json = postForm("https://www.strava.com/oauth/token", body);
        applyTokenJson(pref, json);
    }

    // ---------------- Activities ----------------

    /**
     * List activities with optional after/before (epoch seconds).
     */
    public List<StravaActivity> listActivities(StravaPref pref, int perPage, int page, Long afterEpochSeconds, Long beforeEpochSeconds) throws Exception {
        String url = "https://www.strava.com/api/v3/athlete/activities"
                + "?per_page=" + perPage
                + "&page=" + page;

        if (afterEpochSeconds != null && afterEpochSeconds > 0) {
            url += "&after=" + afterEpochSeconds;
        }
        if (beforeEpochSeconds != null && beforeEpochSeconds > 0) {
            url += "&before=" + beforeEpochSeconds;
        }

        String json = getJson(url, pref.getAccessToken());
        return parseActivities(json);
    }

    // Backward compatible overload
    public List<StravaActivity> listActivities(StravaPref pref, int perPage, int page, long afterEpochSeconds) throws Exception {
        return listActivities(pref, perPage, page, afterEpochSeconds > 0 ? afterEpochSeconds : null, null);
    }

    /**
     * Fetch activity streams. key_by_type=true returns object with keys: latlng, altitude, time.
     */
    public Streams getActivityStreams(StravaPref pref, long activityId) throws Exception {
        String url = "https://www.strava.com/api/v3/activities/" + activityId
                + "/streams?keys=latlng,altitude,time&key_by_type=true";

        String json = getJson(url, pref.getAccessToken());
        return parseStreams(json);
    }

    // ---------------- HTTP helpers ----------------

    private static HttpURLConnection open(String url, String method, String accessToken) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setInstanceFollowRedirects(true);

        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "Bikedump");
        if (accessToken != null && !accessToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        }
        return conn;
    }

    private static String postForm(String url, String body) throws Exception {
        HttpURLConnection conn = open(url, "POST", null);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.getOutputStream().write(bytes);
        conn.getOutputStream().close();

        int code = conn.getResponseCode();
        String resp = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
        if (code < 200 || code >= 300) {
            throw new IOException("Strava token endpoint error HTTP " + code + ": " + resp);
        }
        return resp;
    }

    private static String getJson(String url, String accessToken) throws Exception {
        HttpURLConnection conn = open(url, "GET", accessToken);

        int code = conn.getResponseCode();
        String resp = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
        if (code < 200 || code >= 300) {
            throw new IOException("Strava API error HTTP " + code + ": " + resp);
        }
        return resp;
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static String enc(String s) throws Exception {
        return URLEncoder.encode(s, "UTF-8");
    }

    // ---------------- JSON parsing ----------------

    private static void applyTokenJson(StravaPref pref, String json) throws Exception {
        JsonNode root = MAPPER.readTree(json);

        String accessToken = root.path("access_token").asText(null);
        String refreshToken = root.path("refresh_token").asText(null);
        long expiresAt = root.path("expires_at").asLong(0L);

        pref.setAccessToken(accessToken);
        pref.setRefreshToken(refreshToken);
        if (expiresAt > 0) pref.setExpiresAtEpochSeconds(expiresAt);
    }

    private static List<StravaActivity> parseActivities(String json) throws Exception {
        List<StravaActivity> out = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return out;

        JsonNode root = MAPPER.readTree(json);
        if (!root.isArray()) return out;

        for (JsonNode n : root) {
            long id = n.path("id").asLong(0L);
            if (id <= 0) continue;

            StravaActivity a = new StravaActivity();
            a.id = id;
            a.type = n.path("type").asText(null);
            a.name = n.path("name").asText(null);
            a.startDate = n.path("start_date").asText(null);

            out.add(a);
        }
        return out;
    }

    private static Streams parseStreams(String json) throws Exception {
        Streams s = new Streams();
        if (json == null || json.trim().isEmpty()) return s;

        JsonNode root = MAPPER.readTree(json);

        JsonNode latlng = root.path("latlng").path("data");
        if (latlng.isArray()) {
            s.latlng = new ArrayList<>();
            for (JsonNode p : latlng) {
                if (p.isArray() && p.size() >= 2) {
                    s.latlng.add(new double[]{p.get(0).asDouble(), p.get(1).asDouble()});
                }
            }
        }

        JsonNode alt = root.path("altitude").path("data");
        if (alt.isArray()) {
            s.altitude = new ArrayList<>();
            for (JsonNode a : alt) s.altitude.add(a.asDouble());
        }

        JsonNode time = root.path("time").path("data");
        if (time.isArray()) {
            s.time = new ArrayList<>();
            for (JsonNode t : time) s.time.add(t.asInt());
        }

        return s;
    }

    public static class StravaActivity {
        public long id;
        public String type;
        public String name;
        public String startDate; // ISO8601 UTC
    }

    public static class Streams {
        public List<double[]> latlng;
        public List<Double> altitude;
        public List<Integer> time;
    }
}