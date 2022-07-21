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
        e.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取鼠标选中的文本
        String selectedText = e.getRequiredData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        if (selectedText == null || selectedText.equals("")) {
            NotifyUtil.showErrorMessageDialog("SQLE", "Please select the text before reviewing");
            return;
        }

        SQLESettings settings = SQLESettings.getInstance();

        HttpClientUtil client = new HttpClientUtil(settings);
        try {
            SQLEAuditResult result = client.AuditSQL(selectedText);
            SQLEAuditResultUI ui = new SQLEAuditResultUI(result);

            ApplicationManager.getApplication().invokeLater(() -> {
                DialogBuilder builder = new DialogBuilder();
                builder.setTitle("SQLE");
                builder.centerPanel(ui.getRootPanel());
                builder.show();
            });
        } catch (Exception exception) {
            String errMessage = NotifyUtil.getExceptionMessage(exception);
            NotifyUtil.showErrorMessageDialog("Audit SQL Failed", errMessage);
        }
    }
}
