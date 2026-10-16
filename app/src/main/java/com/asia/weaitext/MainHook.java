package com.asia.weaitext;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 只对微信生效
        if (!lpparam.packageName.equals("com.tencent.mm")) return;

        // 1. 拦截输入法（修改你打字时的输入框内容）
        try {
            XposedHelpers.findAndHookMethod(
                "android.view.inputmethod.InputConnection",
                lpparam.classLoader,
                "commitText",
                CharSequence.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        CharSequence text = (CharSequence) param.args[0];
                        if (text != null && text.length() > 0) {
                            String modified = text.toString().replace("我", "本喵");
                            param.args[0] = modified;
                        }
                    }
                }
            );
        } catch (Throwable t) {
            // 忽略错误，绝对不让微信崩溃
        }

        // 2. 拦截聊天气泡（修改发送后或收到的消息）
        try {
            XposedHelpers.findAndHookMethod(
                "android.widget.TextView",
                lpparam.classLoader,
                "setText",
                CharSequence.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        CharSequence text = (CharSequence) param.args[0];
                        if (text != null && text.length() > 0) {
                            String modified = text.toString().replace("我", "本喵");
                            param.args[0] = modified;
                        }
                    }
                }
            );
        } catch (Throwable t) {
            // 忽略错误，绝对不让微信崩溃
        }
    }
}
