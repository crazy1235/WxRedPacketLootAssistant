package com.jacksen.wx.assistant;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class LootAssistantService extends AccessibilityService {

    private static final String RETURN_CONTENT_DESC = "返回";

    /**
     *
     */
    private static final String PAGE_LAUNCHER_UI = "com.tencent.mm.ui.LauncherUI";

    /**
     * 准备抢红包界面
     */
    private static final String PAGE_LUCKY_MONEY_RECEIVE = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";

    /**
     * 抢红包结果展示界面
     */
    private static final String PAGE_LUCKY_MONET_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";

    private static final String CLASS_BUTTON = "android.widget.Button";

    private static final String GET_RED_POCKET = "领取红包";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("LootAssistantService", "service connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        Log.d("LootAssistantService", "eventType:" + eventType);
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> charSequenceList = event.getText();
                Toast.makeText(this, "charSequenceList.size():" + charSequenceList.size(), Toast.LENGTH_SHORT).show();
                if (!charSequenceList.isEmpty()) {
                    for (CharSequence text : charSequenceList) {
                        String content = text.toString();
                        Log.d("LootAssistantService", content);
                        if (content.contains("[微信红包]")) {
                            if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                                Notification notification = (Notification) event.getParcelableData();
                                PendingIntent pendingIntent = notification.contentIntent;
                                try {
                                    pendingIntent.send();
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                Log.d("LootAssistantService", className);
                if (PAGE_LAUNCHER_UI.equals(className)) {//loot
                    startOpen();
                } else if (PAGE_LUCKY_MONEY_RECEIVE.equals(className)) {
                    startLoot();
                } else if (PAGE_LUCKY_MONET_DETAIL.equals(className)) {
                    overLoot();
                }
                break;
        }
    }

    /**
     * 单开抢红包
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void startOpen() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (null != nodeInfo) {
            List<AccessibilityNodeInfo> nodeInfoList = nodeInfo.findAccessibilityNodeInfosByText(GET_RED_POCKET);
            Log.d("LootAssistantService", "nodeInfoList.size():" + nodeInfoList.size());
            /*for (AccessibilityNodeInfo node : nodeInfoList) {
                lookClickableNode(node);
            }*/
            if (nodeInfoList.size() > 0) {
                lookClickableNode(nodeInfoList.get(nodeInfoList.size() - 1));
            }
        }
    }

    /**
     * 向上寻找clickable = true的节点
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void lookClickableNode(AccessibilityNodeInfo nodeInfo) {
        Log.d("LootAssistantService", nodeInfo.getClassName().toString() + "---" + nodeInfo.isClickable());
        if (nodeInfo.isClickable()) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return;
        }

        AccessibilityNodeInfo parentNode = nodeInfo.getParent();
        while (parentNode != null) {
            Log.d("LootAssistantService", parentNode.getClassName().toString() + "---" + parentNode.isClickable());
            if (parentNode.isClickable()) {
                parentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            parentNode = parentNode.getParent();
        }
    }

    /**
     * 开始抢
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void startLoot() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        int childCount = nodeInfo.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Log.d("LootAssistantService", nodeInfo.getChild(i).getClassName().toString());
            if (CLASS_BUTTON.equals(nodeInfo.getChild(i).getClassName())) {
                nodeInfo.getChild(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }


    /**
     * 结束抢红包
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void overLoot() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

        int childCount = nodeInfo.getChildCount();
        Log.d("LootAssistantService", "overLoot -- childCount:" + childCount);
        if (childCount > 0) {
            findReturnNode(nodeInfo);
        }
    }


    /**
     * 遍历查询返回节点
     *
     * @param nodeInfo
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean findReturnNode(AccessibilityNodeInfo nodeInfo) {
        boolean result = false;
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = nodeInfo.getChild(i);
            if (childNode.getContentDescription() != null && RETURN_CONTENT_DESC.equals(childNode.getContentDescription().toString())) {
                if (childNode.isClickable()) {
                    childNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                }
                result = true;
                break;
            } else {
                if (childNode.getChildCount() > 0) {
                    result = findReturnNode(childNode);
                    if (result) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void onInterrupt() {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LootAssistantService", "onDestory");
    }
}
