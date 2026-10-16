package com.asia.weaitext;

import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.File;
import java.io.FileInputStream;

public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 只对微信生效
        if (!lpparam.packageName.equals("com.tencent.mm")) return;

        XposedHelpers.findAndHookMethod(
            TextView.class, "setText", CharSequence.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    CharSequence text = (CharSequence) param.args[0];
                    if (text == null || text.length() == 0) return;
                    
                    String raw = text.toString();
                    // 读取你在 WebUI 保存的配置文件，动态替换
                    String modified = readConfigAndReplace(raw);
                    
                    if (!raw.equals(modified)) {
                        param.args[0] = modified;
                    }
                }
            }
        );
    }

    private String readConfigAndReplace(String input) {
        String result = input;
        try {
            // 直接读取你之前写好的 config.json
            File file = new File("/data/adb/modules/WeaiText/config.json");
            if (!file.exists()) return result;
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes);
            fis.close();
            String json = new String(bytes, "UTF-8");
            
            // 简单粗暴的解析替换（如果在生产环境建议用 Gson 库）
            result = result.replace("我", "本喵").replace("你", "杂鱼"); // 这里你也可以写从 json 解析的代码
        } catch (Exception e) {
            // 忽略错误，直接返回原文本，防止崩溃
        }
        return result;
    }
}
