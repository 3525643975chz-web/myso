package com.asia.weaitext;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    // 配置文件路径（放在 App 私有目录或外部存储）
    private File configFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configFile = new File(getFilesDir(), "weaitext_config.json");

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        // 允许访问文件协议
        webView.getSettings().setAllowFileAccess(true);

        // 注入安卓桥接接口，让 JS 能调用 Java
        webView.addJavascriptInterface(new AndroidBridge(this), "AndroidBridge");

        // 加载你的 HTML 文件（假设放在 assets 目录）
        webView.loadUrl("file:///android_asset/index.html");
    }

    // 这是供前端 JS 调用的原生方法集合
    public class AndroidBridge {
        private Context context;
        public AndroidBridge(Context context) { this.context = context; }

        // 1. 保存配置
        @JavascriptInterface
        public void saveConfig(String json) {
            try {
                FileOutputStream fos = new FileOutputStream(configFile);
                fos.write(json.getBytes());
                fos.close();
                // 通知无障碍服务重新读取配置
                Intent intent = new Intent("com.asia.weaitext.RELOAD_CONFIG");
                context.sendBroadcast(intent);
            } catch (Exception e) { e.printStackTrace(); }
        }

        // 2. 读取配置（WebView 加载时自动调用）
        @JavascriptInterface
        public String loadConfig() {
            try {
                if (!configFile.exists()) return "";
                FileInputStream fis = new FileInputStream(configFile);
                byte[] bytes = new byte[(int) configFile.length()];
                fis.read(bytes);
                fis.close();
                return new String(bytes);
            } catch (Exception e) { return ""; }
        }

        // 3. 跳转到无障碍服务设置
        @JavascriptInterface
        public void openAccessibilitySettings() {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
        }

        // 4. 跳转到电池优化设置
        @JavascriptInterface
        public void openBatterySettings() {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            context.startActivity(intent);
        }

        // 5. 跳转到应用详情页（用于开启自启动、通知权限等）
        @JavascriptInterface
        public void openAutoStartSettings() {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }

        // 6. 跳转到通知设置
        @JavascriptInterface
        public void openNotificationSettings() {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            context.startActivity(intent);
        }
    }
}
