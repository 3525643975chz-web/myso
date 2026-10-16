package com.asia.weaitext;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebSettings;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void saveConfig(String cfg) {
                getSharedPreferences("WeaiText", MODE_PRIVATE).edit().putString("config", cfg).apply();
            }
        }, "AndroidBridge");
        webView.loadUrl("file:///android_asset/index.html"); // 只要把 HTML 放在 assets 目录
        setContentView(webView);
    }
}
