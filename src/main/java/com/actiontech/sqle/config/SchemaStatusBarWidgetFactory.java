package com.actiontech.sqle.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SchemaStatusBarWidgetFactory extends StatusBarEditorBasedWidgetFactory {
    @Override
    public @NonNls @NotNull String getId() {
        return SchemaStatusBarWidget.schemaNameStatusBarID;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return "schema name";
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new SchemaStatusBarWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }
}
