package jp.sutemag.aichoukan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView web;
    private boolean pageReady = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Notifier.ensureChannels(this);
        Scheduler.scheduleAll(this);
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setSupportMultipleWindows(false);

        web.addJavascriptInterface(new Bridge(), "Native");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if ("http".equals(scheme) || "https".equals(scheme)) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (ActivityNotFoundException ignored) {
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pageReady = true;
                applyTheme();
            }
        });

        applyTheme();
        String feed = feedFrom(getIntent());
        web.loadUrl("file:///android_asset/index.html" + (feed == null ? "" : "#" + feed));
    }

    /** The paper a notification asked to open, or null. */
    private static String feedFrom(Intent intent) {
        if (intent == null) return null;
        String feed = intent.getStringExtra(Scheduler.EXTRA_FEED);
        return feed == null ? null : Feed.byKey(feed).key;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String feed = feedFrom(intent);
        if (feed != null && web != null && pageReady) {
            web.evaluateJavascript("window.__openFeed && window.__openFeed('" + feed + "')", null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) {
            web.onResume();
            if (pageReady) web.evaluateJavascript("window.__refresh && window.__refresh()", null);
        }
    }

    @Override
    protected void onPause() {
        if (web != null) web.onPause();
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyTheme();
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    private boolean isNight() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    @SuppressWarnings("deprecation")
    private void applyTheme() {
        boolean night = isNight();
        int bg = night ? 0xFF0F141E : 0xFFF5F6F3;
        Window w = getWindow();
        w.setStatusBarColor(bg);
        w.setNavigationBarColor(bg);
        View decor = w.getDecorView();
        int lightFlags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        int flags = decor.getSystemUiVisibility();
        decor.setSystemUiVisibility(night ? (flags & ~lightFlags) : (flags | lightFlags));
        if (web != null) {
            web.setBackgroundColor(bg);
            web.evaluateJavascript("document.documentElement.setAttribute('data-theme','"
                    + (night ? "dark" : "light") + "')", null);
        }
    }

    /** Lets the page ask for edition files; the answer comes back through window.__nativeDone. */
    private class Bridge {
        @JavascriptInterface
        public void request(final String id, final String path) {
            new Thread(() -> {
                String body = Net.fetchWithCache(getApplicationContext(), path);
                final String js = "window.__nativeDone && window.__nativeDone(" + JSONObject.quote(id) + ","
                        + (body == null ? "null" : JSONObject.quote(body)) + ")";
                runOnUiThread(() -> {
                    if (web != null) web.evaluateJavascript(js, null);
                });
            }).start();
        }

        @JavascriptInterface
        public String theme() {
            return isNight() ? "dark" : "light";
        }
    }
}
