package com.actiontech.sqle.action;

import com.actiontech.sqle.config.SQLEAuditResult;
import com.actiontech.sqle.config.SQLESettings;
import com.actiontech.sqle.from.SQLEAuditResultUI;
import com.actiontech.sqle.util.HttpClientUtil;
import com.actiontech.sqle.util.NotifyUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AuditEditor extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        String selectedText = e.getRequiredData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        e.getPresentation().setEnabledAndVisible(!(selectedText == null || selectedText.equals("")));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取鼠标选中的文本
        String[] selectedTexts={""};
        selectedTexts[0] = e.getRequiredData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        Audit.Audit(e,selectedTexts, HttpClientUtil.AuditType.SQL);
    }
}
