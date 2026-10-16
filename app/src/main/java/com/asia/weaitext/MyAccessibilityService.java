package com.asia.weaitext;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MyAccessibilityService extends AccessibilityService {
    private List<Rule> rules = new ArrayList<>();

    static class Rule {
        String from, to;
        Rule(String from, String to) { this.from = from; this.to = to; }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;
        traverseNode(rootNode);
    }

    private void traverseNode(AccessibilityNodeInfo node) {
        if (node == null) return;
        if (node.getChildCount() > 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                traverseNode(node.getChild(i));
            }
        } else {
            CharSequence text = node.getText();
            if (text != null) {
                String original = text.toString();
                String modified = original;
                for (Rule rule : rules) {
                    modified = modified.replace(rule.from, rule.to);
                }
                if (!original.equals(modified)) {
                    // 尝试替换（注意：无障碍服务直接修改输入框会导致光标乱跳）
                    // 这里只对非输入框的 TextView 进行尝试
                    if (!node.isEditable()) {
                        android.os.Bundle arguments = new android.os.Bundle();
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, modified);
                        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                    }
                }
            }
        }
    }

    @Override protected void onServiceConnected() { super.onServiceConnected(); loadRules(); }
    @Override public void onInterrupt() {}

    public void loadRules() {
        // 从 SharedPreferences 中读取 WebUI 保存的配置
        String cfg = getSharedPreferences("WeaiText", MODE_PRIVATE).getString("config", "");
        if (cfg.isEmpty()) return;
        try {
            JSONObject json = new JSONObject(cfg);
            JSONArray arr = json.getJSONArray("rules");
            rules.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                rules.add(new Rule(obj.getString("from"), obj.getString("to")));
            }
        } catch (Exception e) {}
    }
}
