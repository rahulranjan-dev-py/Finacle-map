package com.finacledesk.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Finacle Desk — offline-first POSB counter reference.
 *
 * The whole reference lives in assets/finacledesk.html and works with no
 * network at all. On top of that, "What changed" entries and interest rates
 * come from an updates.json with a version number. On every launch the app
 * applies the newest of:
 *   1. the updates.json bundled in this APK,
 *   2. the last successfully downloaded copy cached on the phone,
 *   3. a freshly downloaded copy from UPDATES_URL (if reachable).
 * So pushing a new updates.json to the repo reaches every phone that has
 * network access, and a rebuilt APK carries it for phones that do not.
 */
public class MainActivity extends Activity {

    // Served from this repository. Works once the repo (or just this file's
    // hosting location) is publicly reachable; until then the app silently
    // falls back to the bundled + cached data.
    private static final String UPDATES_URL =
            "https://raw.githubusercontent.com/rahulranjan-dev-py/Finacle-map/main/app/src/main/assets/updates.json";

    private static final String CACHE_FILE = "updates-cache.json";

    private WebView web;
    private long appliedVersion = -1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setBuiltInZoomControls(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if ("http".equals(scheme) || "https".equals(scheme)) {
                    // Links to SB Orders / India Post site open in the browser.
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (Exception ignored) {
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                applyLocalUpdates();
                fetchRemoteUpdates();
            }
        });

        setContentView(web);
        web.loadUrl("file:///android_asset/finacledesk.html");
    }

    /** Apply the newer of the bundled and the cached updates.json. */
    private void applyLocalUpdates() {
        String bundled = readAsset("updates.json");
        String cached = readCache();
        long bv = versionOf(bundled);
        long cv = versionOf(cached);
        if (cv > bv) {
            applyToPage(cached, cv);
        } else if (bv >= 0) {
            applyToPage(bundled, bv);
        }
    }

    /** Download updates.json in the background; apply and cache it if newer. */
    private void fetchRemoteUpdates() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(UPDATES_URL).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("Accept", "application/json");
                if (conn.getResponseCode() != 200) return;
                String body = readAll(conn.getInputStream());
                final long v = versionOf(body);
                if (v < 0) return; // not valid updates JSON
                writeCache(body);
                runOnUiThread(() -> {
                    if (v > appliedVersion) applyToPage(body, v);
                });
            } catch (Exception ignored) {
                // Offline or unreachable — the bundled/cached data stands.
            } finally {
                if (conn != null) conn.disconnect();
            }
        }, "updates-fetch").start();
    }

    private void applyToPage(String json, long version) {
        if (json == null) return;
        appliedVersion = version;
        web.evaluateJavascript("window.applyRemoteUpdates(" + json + ");", null);
    }

    /** Returns the "version" field, or -1 if the JSON is missing/invalid. */
    private static long versionOf(String json) {
        if (json == null) return -1;
        try {
            return new JSONObject(json).optLong("version", -1);
        } catch (Exception e) {
            return -1;
        }
    }

    private String readAsset(String name) {
        try (InputStream in = getAssets().open(name)) {
            return readAll(in);
        } catch (Exception e) {
            return null;
        }
    }

    private String readCache() {
        File f = new File(getFilesDir(), CACHE_FILE);
        if (!f.exists()) return null;
        try (InputStream in = new java.io.FileInputStream(f)) {
            return readAll(in);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeCache(String body) {
        File tmp = new File(getFilesDir(), CACHE_FILE + ".tmp");
        File dst = new File(getFilesDir(), CACHE_FILE);
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        } catch (Exception e) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        tmp.renameTo(dst);
    }

    private static String readAll(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            char[] buf = new char[8192];
            int n;
            while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
        }
        return sb.toString();
    }
}
