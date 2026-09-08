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

/**
 * Finacle Desk — offline POSB counter reference.
 *
 * The whole reference lives in assets/finacledesk.html. The app makes no
 * network calls of its own; new SB Orders and rate revisions ship as a new
 * APK from the repository's Releases page. Links inside the page (to order
 * PDFs and circulars) are handed to the phone's browser.
 */
public class MainActivity extends Activity {

    private WebView web;

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
                // Show the installed version in the page so users can tell
                // whether they have the newest release.
                String version;
                try {
                    version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                } catch (Exception e) {
                    version = "";
                }
                view.evaluateJavascript(
                        "window.setAppVersion && window.setAppVersion(" + JSONObject.quote(version) + ");",
                        null);
            }
        });

        setContentView(web);
        web.loadUrl("file:///android_asset/finacledesk.html");
    }

    @Override
    public void onBackPressed() {
        // The page routes with location.hash, so WebView history mirrors
        // in-app navigation (and the step-by-step overlay pushes a state).
        if (web != null && web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
