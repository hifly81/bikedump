package org.hifly.bikedump.domain;

import java.io.Serializable;
import java.util.Date;

public class StravaPref implements Serializable {
    private static final long serialVersionUID = 1L;

    // OAuth
    private String accessToken;
    private String refreshToken;
    private long expiresAtEpochSeconds;

    // OAuth callback config
    private int callbackPort = 8765;
    private String redirectHost = "127.0.0.1";

    // Sync
    private boolean autoSyncEnabled;
    private long autoSyncIntervalMillis = 6 * 60 * 60 * 1000L; // default 6h
    private Date lastSuccessfulSyncAt;

    // NEW: time marker for Strava "after" filter
    // Strava expects seconds since epoch
    private long lastSyncAfterEpochSeconds = 0;

    public boolean isConnected() {
        return accessToken != null && !accessToken.isEmpty();
    }

    public boolean isAccessTokenExpired() {
        long now = System.currentTimeMillis() / 1000L;
        return expiresAtEpochSeconds > 0 && now >= (expiresAtEpochSeconds - 30);
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public long getExpiresAtEpochSeconds() { return expiresAtEpochSeconds; }
    public void setExpiresAtEpochSeconds(long expiresAtEpochSeconds) { this.expiresAtEpochSeconds = expiresAtEpochSeconds; }

    public int getCallbackPort() { return callbackPort; }
    public void setCallbackPort(int callbackPort) { this.callbackPort = callbackPort; }

    public String getRedirectHost() { return redirectHost; }
    public void setRedirectHost(String redirectHost) { this.redirectHost = redirectHost; }

    public boolean isAutoSyncEnabled() { return autoSyncEnabled; }
    public void setAutoSyncEnabled(boolean autoSyncEnabled) { this.autoSyncEnabled = autoSyncEnabled; }

    public long getAutoSyncIntervalMillis() { return autoSyncIntervalMillis; }
    public void setAutoSyncIntervalMillis(long autoSyncIntervalMillis) { this.autoSyncIntervalMillis = autoSyncIntervalMillis; }

    public Date getLastSuccessfulSyncAt() { return lastSuccessfulSyncAt; }
    public void setLastSuccessfulSyncAt(Date lastSuccessfulSyncAt) { this.lastSuccessfulSyncAt = lastSuccessfulSyncAt; }

    public long getLastSyncAfterEpochSeconds() { return lastSyncAfterEpochSeconds; }
    public void setLastSyncAfterEpochSeconds(long lastSyncAfterEpochSeconds) { this.lastSyncAfterEpochSeconds = lastSyncAfterEpochSeconds; }

    public void disconnect() {
        accessToken = null;
        refreshToken = null;
        expiresAtEpochSeconds = 0;

        lastSuccessfulSyncAt = null;
        lastSyncAfterEpochSeconds = 0;
    }
}