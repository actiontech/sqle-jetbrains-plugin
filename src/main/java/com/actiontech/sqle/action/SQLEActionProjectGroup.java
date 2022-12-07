package com.actiontech.sqle.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;


public class SQLEActionProjectGroup extends DefaultActionGroup {

    @Override
    public void update(@NotNull AnActionEvent event) {
//        // Enable/disable depending on whether user is editing
//        Editor editor = event.getData(CommonDataKeys.EDITOR);
        event.getPresentation().setEnabled(true);
        // Always make visible.
        event.getPresentation().setVisible(true);
    }
}
