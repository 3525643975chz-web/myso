package com.asia.weaitext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    private WebView webView;
    private File configFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 配置存放路径（App私有目录）
        configFile = new File(getFilesDir(), "weaitext_config.json");

        // 动态创建 WebView，无需 XML 布局文件
        webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);

        // 注入 Android 桥接接口
        webView.addJavascriptInterface(new AndroidBridge(this), "AndroidBridge");

        // 加载 assets 目录下的 index.html
        webView.loadUrl("file:///android_asset/index.html");

        setContentView(webView);
    }

    // 提供给前端 JS 调用的原生方法集合
    public class AndroidBridge {
        private Context context;

        public AndroidBridge(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void saveConfig(String json) {
            try {
                FileOutputStream fos = new FileOutputStream(configFile);
                fos.write(json.getBytes());
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public String loadConfig() {
            try {
                if (!configFile.exists()) return "";
                FileInputStream fis = new FileInputStream(configFile);
                byte[] bytes = new byte[(int) configFile.length()];
                fis.read(bytes);
                fis.close();
                return new String(bytes);
            } catch (Exception e) {
                return "";
            }
        }

        @JavascriptInterface
        public void openAccessibilitySettings() {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
        }

        @JavascriptInterface
        public void openBatterySettings() {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            context.startActivity(intent);
        }

        @JavascriptInterface
        public void openAutoStartSettings() {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }

        @JavascriptInterface
        public void openNotificationSettings() {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            context.startActivity(intent);
        }
    }
}
