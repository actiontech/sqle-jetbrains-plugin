package com.actiontech.sqle.action;

import com.actiontech.sqle.config.SQLEAuditResult;
import com.actiontech.sqle.config.SQLESettings;
import com.actiontech.sqle.from.SQLEAuditResultUI;
import com.actiontech.sqle.util.HttpClientUtil;
import com.actiontech.sqle.util.NotifyUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogBuilder;

public class Audit {
    public static void Audit(AnActionEvent e, String sql, HttpClientUtil.AuditType type) {

        SQLESettings settings = SQLESettings.getInstance();

        HttpClientUtil client = new HttpClientUtil(settings);
        try {
            SQLEAuditResult result = client.AuditSQL(sql, type);
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
