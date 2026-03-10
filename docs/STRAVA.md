# Strava integration

This document explains how to configure and use the Strava integration in **Bikedump** to import cycling workouts (Rides) as **GPX** files and load them into the app.

## What you can do

- Connect Bikedump to Strava using **OAuth** (browser login + local callback).
- Import **only cycling activities** (`type == Ride`).
- Download workouts from Strava as **GPX**.
- Store downloaded exports in your local Bikedump home directory.
- Run imports **manually** or via **periodic auto-sync** (while the app is running).

---

## 1) Get Strava Client ID & Client Secret

You must create (or use) a **Strava API Application** to obtain:

- **Client ID**
- **Client Secret**

These are available in Strava’s developer portal on the page usually called **"My API Application"** (or similar).

### Redirect URI requirement

Because Bikedump uses a **local callback** (OAuth option A), you must configure the Redirect URI in your Strava App settings to match your Bikedump configuration exactly.

Default Redirect URI used by Bikedump:

- `http://127.0.0.1:8765/strava/callback`

If you change host/port in the app (see next section), you must update the Redirect URI in Strava accordingly.

> Note: The path must remain `/strava/callback`.

---

## 2) Configure Strava settings in Bikedump (Swing UI)

Open:

- **Options → Strava**

Configure:

### A) Strava App Credentials
- **Client ID**
- **Client Secret**  
  The secret is stored locally in `~/.bikedump/preferences/strava.pref` with **basic obfuscation** (not real encryption).

### B) OAuth Callback (Local)
- **Redirect host** (default: `127.0.0.1`)
- **Callback port** (default: `8765`)

The app will display the full Redirect URI, e.g.:

- `http://127.0.0.1:8765/strava/callback`

### C) Auto Sync
- Enable/disable auto-sync
- Choose the interval (e.g. 30 minutes, 1h, 6h, 24h)

> Important: Auto-sync runs **only while the application is open** (it is not an OS background service).

---

## 3) Connect to Strava (OAuth flow – local callback)

Open the Strava dialog (e.g. menu item **“Import from Strava…”**) and click:

- **Connect to Strava (browser)**

What happens:

1. The app starts a small local HTTP server on your configured host/port (example: `127.0.0.1:8765`)
2. The app opens your browser to Strava’s authorization URL.
3. You log in and approve permissions.
4. Strava redirects the browser to:
    - `http://127.0.0.1:8765/strava/callback?code=...&state=...`
5. The local server receives the callback and extracts the `code`.
6. Bikedump exchanges the `code` for:
    - `access_token`
    - `refresh_token`
    - `expires_at`
7. Tokens are saved to:
    - `~/.bikedump/preferences/strava.pref`

After this, Bikedump is “Connected”.

---

## 4) Manual import

Click:

- **Import rides**

What happens:

1. If `access_token` is expired, Bikedump refreshes it using `refresh_token`.
2. Bikedump calls Strava API `GET /athlete/activities` and filters only:
    - `type == "Ride"`
3. Only activities **between dates interval** are imported using:
4. For each activity:
    - download **GPX** 
5. Files are saved locally under:
    - `~/.bikedump/strava/exports/`
    - e.g. `strava_<activityId>.tcx` or `strava_<activityId>.gpx`
6. Bikedump loads the track using existing parsers
7. Bikedump updates the **time marker** after a successful import:
    - `lastSyncAfterEpochSeconds = now`

This prevents importing the same activities repeatedly.

---

## 5) Auto-sync (periodic import)

If enabled in **Options → Strava**, Bikedump runs a periodic background import with the same logic as manual import:

- list activities after `lastSyncAfterEpochSeconds`
- filter only `Ride`
- download GPX
- save under `~/.bikedump/strava/exports/`
- update marker after success

### Notes
- Auto-sync runs only while Bikedump is open.
- If you need an always-on sync, you would need an external scheduler/service.

---

## 6) Data marker (“after” time marker)

The integration uses a **time-based marker**:

- `lastSyncAfterEpochSeconds`

This marker is stored in:

- `~/.bikedump/preferences/strava.pref`

It is used as the Strava API `after` parameter so the app only imports activities newer than the last successful sync.

---

## Troubleshooting

### Browser login works, but callback fails
- Verify your configured Redirect URI in Strava matches Bikedump exactly (host + port + path).
- Ensure nothing else is using the configured port.
- Prefer `127.0.0.1` instead of `localhost` to avoid DNS/proxy oddities.

### Import returns 0 rides
- You may have no new rides since the last marker time.
- Reset the marker by clearing Strava prefs (or disconnect/reconnect).

---

## Security note (Client Secret)
The Strava Client Secret is stored locally using **simple obfuscation** (not encryption).
It protects against casual inspection but does not provide strong security.
If you need stronger protection, consider OS keychain integration.