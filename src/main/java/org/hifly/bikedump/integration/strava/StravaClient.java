package org.hifly.bikedump.integration.strava;

import org.hifly.bikedump.domain.StravaPref;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StravaClient {

    // TODO:
    private final String clientId;
    private final String clientSecret;

    public StravaClient(String clientId, String clientSecret) {
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        this.clientSecret = Objects.requireNonNull(clientSecret, "clientSecret");
    }

    public String buildAuthorizeUrl(String redirectUri, String state) throws Exception {
        // scope: read + activity:read_all
        return "https://www.strava.com/oauth/authorize"
                + "?client_id=" + URLEncoder.encode(clientId, "UTF-8")
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
                + "&response_type=code"
                + "&approval_prompt=auto"
                + "&scope=" + URLEncoder.encode("read,activity:read_all", "UTF-8")
                + "&state=" + URLEncoder.encode(state, "UTF-8");
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
        if (!pref.isConnected()) return;
        if (!pref.isAccessTokenExpired()) return;

        String body = "client_id=" + enc(clientId)
                + "&client_secret=" + enc(clientSecret)
                + "&refresh_token=" + enc(pref.getRefreshToken())
                + "&grant_type=refresh_token";

        String json = postForm("https://www.strava.com/oauth/token", body);
        applyTokenJson(pref, json);
    }

    public List<StravaActivity> listRideActivities(StravaPref pref, int perPage, int page) throws Exception {
        // filtering: we will filter by type == "Ride" client-side
        String url = "https://www.strava.com/api/v3/athlete/activities?per_page=" + perPage + "&page=" + page;
        String json = getJson(url, pref.getAccessToken());
        return parseActivities(json);
    }

    public List<StravaActivity> listActivities(StravaPref pref, int perPage, int page, long afterEpochSeconds) throws Exception {
        String url = "https://www.strava.com/api/v3/athlete/activities"
                + "?per_page=" + perPage
                + "&page=" + page;

        if (afterEpochSeconds > 0) {
            url += "&after=" + afterEpochSeconds;
        }

        String json = getJson(url, pref.getAccessToken());
        return parseActivities(json);
    }

    public File downloadActivityExport(StravaPref pref, long activityId, String format, File outFile) throws Exception {
        // format: "gpx" or "tcx"
        String url = "https://www.strava.com/api/v3/activities/" + activityId + "/export_" + format;
        downloadBinary(url, pref.getAccessToken(), outFile);
        return outFile;
    }


    private static String enc(String s) throws Exception {
        return URLEncoder.encode(s, "UTF-8");
    }

    private static String postForm(String url, String body) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }
        int code = conn.getResponseCode();
        String resp = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
        if (code < 200 || code >= 300) {
            throw new IOException("Strava token endpoint error HTTP " + code + ": " + resp);
        }
        return resp;
    }

    private static String getJson(String url, String accessToken) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        int code = conn.getResponseCode();
        String resp = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
        if (code < 200 || code >= 300) {
            throw new IOException("Strava API error HTTP " + code + ": " + resp);
        }
        return resp;
    }

    private static void downloadBinary(String url, String accessToken, File outFile) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            String resp = readAll(conn.getErrorStream());
            throw new IOException("Strava export error HTTP " + code + ": " + resp);
        }

        try (InputStream is = conn.getInputStream();
             OutputStream os = new FileOutputStream(outFile)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) {
                os.write(buf, 0, r);
            }
        }
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static void applyTokenJson(StravaPref pref, String json) {
        // minimal JSON extraction without adding deps
        // expects fields: access_token, refresh_token, expires_at
        pref.setAccessToken(extractJsonString(json, "access_token"));
        pref.setRefreshToken(extractJsonString(json, "refresh_token"));
        String expiresAt = extractJsonNumber(json, "expires_at");
        if (expiresAt != null && !expiresAt.isEmpty()) {
            pref.setExpiresAtEpochSeconds(Long.parseLong(expiresAt));
        }
    }

    private static String extractJsonString(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static String extractJsonNumber(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static List<StravaActivity> parseActivities(String json) {
        // Very small parser: find objects in an array by regex.
        // We only need: id, type, name, start_date (optional).
        List<StravaActivity> out = new ArrayList<>();

        // naive split on "},{" boundaries (ok for Strava activities array)
        String trimmed = json.trim();
        if (!trimmed.startsWith("[")) return out;
        if (trimmed.equals("[]")) return out;

        // remove [ and ]
        String body = trimmed.substring(1, trimmed.length() - 1);
        String[] chunks = body.split("\\},\\s*\\{");

        for (String chunk : chunks) {
            String obj = chunk;
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";

            String idStr = extractJsonNumber(obj, "id");
            String type = extractJsonString(obj, "type");
            String name = extractJsonString(obj, "name");

            if (idStr == null) continue;

            StravaActivity a = new StravaActivity();
            a.id = Long.parseLong(idStr);
            a.type = type;
            a.name = name;
            out.add(a);
        }

        return out;
    }

    public static class StravaActivity {
        public long id;
        public String type; // "Ride"
        public String name;
    }
}