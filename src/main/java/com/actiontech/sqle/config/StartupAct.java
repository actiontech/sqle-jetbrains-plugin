package com.actiontech.sqle.config;

import com.actiontech.sqle.from.SQLESettingUI;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;


// 插件启动时,调用此方法,初始化数据源名和数据库名
public class StartupAct implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        if (null == SQLESettingUI.settings) {
            return;
        }

        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (null == statusBar) {
            return;
        }

        StatusBarWidget widget = statusBar.getWidget(InstStatusBarWidget.InstNameStatusBarID);
        if (widget instanceof InstStatusBarWidget) {
            InstStatusBarWidget instStatusBarWidget = (InstStatusBarWidget) widget;
            instStatusBarWidget.update();
        }

        StatusBarWidget schemaWidget = statusBar.getWidget(SchemaStatusBarWidget.schemaNameStatusBarID);
        if (schemaWidget instanceof SchemaStatusBarWidget) {
            SchemaStatusBarWidget schemaStatusBarWidget = (SchemaStatusBarWidget) schemaWidget;
            schemaStatusBarWidget.update();
        }
    }
}