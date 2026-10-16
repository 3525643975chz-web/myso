package com.asia.weaitext;

import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.tencent.mm")) return; // 微信

        XposedHelpers.findAndHookMethod(
            TextView.class, "setText", CharSequence.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    CharSequence text = (CharSequence) param.args[0];
                    if (text == null) return;
                    String modified = text.toString();
                    // 先简单写死测试，跑通后再去读 config.json
                    modified = modified.replace("我", "本喵").replace("你", "杂鱼");
                    param.args[0] = modified;
                }
            }
        );
    }
}
