package com.actiontech.sqle.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.Disposer;


public class InstStatusBarWidgetFactory extends StatusBarEditorBasedWidgetFactory {
    @Override
    public @NonNls @NotNull String getId() {
        return InstStatusBarWidget.InstNameStatusBarID;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return "inst name";
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new InstStatusBarWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }
}
