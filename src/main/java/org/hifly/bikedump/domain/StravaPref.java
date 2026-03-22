package org.hifly.bikedump.domain;

import org.hifly.bikedump.utility.Constants;
import org.hifly.bikedump.utility.ObfuscationUtility;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

public class StravaPref implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String accessToken;
    private String refreshToken;
    private long expiresAtEpochSeconds;

    private int callbackPort = 8765;
    private String redirectHost = "127.0.0.1";

    private boolean autoSyncEnabled;
    private long autoSyncIntervalMillis = 6 * 60 * 60 * 1000L;
    private Date lastSuccessfulSyncAt;
    private long lastSyncAfterEpochSeconds = 0;

    private String clientId;
    // store obfuscated, not plain
    private String clientSecretObf;

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    /**
     * Returns de-obfuscated secret.
     * NOTE: this is only obfuscation, not encryption.
     */
    public String getClientSecret() {
        if (clientSecretObf == null) return null;
        return ObfuscationUtility.deobfuscate(clientSecretObf, getLocalKeyMaterial());
    }

    /**
     * Accepts plain secret and stores it obfuscated.
     */
    public void setClientSecret(String clientSecretPlain) {
        if (clientSecretPlain == null) {
            this.clientSecretObf = null;
        } else {
            this.clientSecretObf = ObfuscationUtility.obfuscate(clientSecretPlain, getLocalKeyMaterial());
        }
    }

    private String getLocalKeyMaterial() {
        // deterministic per-user-machine-ish string; NOT a password.
        return System.getProperty("user.home") + "|" + Constants.PROGRAM_NAME;
    }

    public boolean isConnected() { return accessToken != null && !accessToken.isEmpty(); }

    public boolean isAccessTokenExpired() {
        long now = System.currentTimeMillis() / 1000L;
        return expiresAtEpochSeconds > 0 && now >= (expiresAtEpochSeconds - 30);
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public void setExpiresAtEpochSeconds(long expiresAtEpochSeconds) { this.expiresAtEpochSeconds = expiresAtEpochSeconds; }

    public int getCallbackPort() { return callbackPort; }
    public void setCallbackPort(int callbackPort) { this.callbackPort = callbackPort; }

    public String getRedirectHost() { return redirectHost; }
    public void setRedirectHost(String redirectHost) { this.redirectHost = redirectHost; }

    public boolean isAutoSyncEnabled() { return autoSyncEnabled; }
    public void setAutoSyncEnabled(boolean autoSyncEnabled) { this.autoSyncEnabled = autoSyncEnabled; }

    public long getAutoSyncIntervalMillis() { return autoSyncIntervalMillis; }
    public void setAutoSyncIntervalMillis(long autoSyncIntervalMillis) { this.autoSyncIntervalMillis = autoSyncIntervalMillis; }

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